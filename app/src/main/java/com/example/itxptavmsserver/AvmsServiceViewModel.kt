package com.example.itxptavmsserver

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitlab.mvysny.konsumexml.anyName
import com.gitlab.mvysny.konsumexml.konsumeXml
import io.ktor.application.call
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.stop
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import kotlinx.coroutines.*
import org.redundent.kotlin.xml.xml
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

private const val SERVICE_NAME = "testapp_avms"
private const val SERVICE_TYPE = "_itxpt_http._tcp"

private const val SERVICE_ATTR_KEY_TXT_VERS = "txtvers"
private const val SERVICE_ATTR_KEY_VERSION = "version"
private const val SERVICE_ATTR_KEY_SW_VERS = "swvers"
private const val SERVICE_ATTR_KEY_PATH = "path"
private const val SERVICE_ATTR_KEY_OPERATIONS = "operations"

private const val SERVICE_TXT_VERS = "1"
private const val SERVICE_VERSION = "2.1"
private const val SERVICE_PATH = "avms/"

private const val DELIVERY_CHECK_TIMEOUT_IN_MS = 5000L
private const val DELIVERY_REPEAT_TIMEOUT_IN_MS = 60000L
private const val DELIVERY_FAILURE_COUNT_LIMIT = 5

private const val TAG = "AVMS"

class AvmsServiceViewModel(context: Context) : ViewModel() {

    private val isAvmsServiceEnabled = true
    private val avmsServicePort = 8010

    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

    private val registrationListener: AvmsRegistrationListener = AvmsRegistrationListener()
    private val serviceInfo = NsdServiceInfo().apply {
        serviceName = SERVICE_NAME
        serviceType = SERVICE_TYPE
        port = avmsServicePort
        setAttribute(SERVICE_ATTR_KEY_TXT_VERS, SERVICE_TXT_VERS)
        setAttribute(SERVICE_ATTR_KEY_VERSION, SERVICE_VERSION)
        setAttribute(SERVICE_ATTR_KEY_SW_VERS, "1.0")
        setAttribute(SERVICE_ATTR_KEY_PATH, SERVICE_PATH)
        setAttribute(SERVICE_ATTR_KEY_OPERATIONS, SubscriptionType.values().joinToString(";") { it.subpath })
    }
    private var serviceName: String? = null
    private var isServiceRegistered = AtomicBoolean(false)
    private var isServiceRegistering = AtomicBoolean(false)

    private var server : NettyApplicationEngine? = null

    private val subscribers = HashMap<SubscriptionType, MutableSet<SubscriptionRequest>>().also { map ->
        SubscriptionType.values().forEach { map[it] = mutableSetOf() }
    }
    private val hasSubscribers: Boolean
        get() = subscribers.values.find { it.isNotEmpty() } != null

    private val subscribersLock = Any()

    private val lastSuccessfulDeliveryTimestamps = HashMap<SubscriptionRequest, Long>()
    private var deliveryJob: Job? = null
    private val missedAcknowledgementCounter = HashMap<SubscriptionRequest, Int>()

    private val networkClient = AvmsDeliveryNetworkClient()

    fun registerService() {
        Log.d(TAG, "registerService")
        if (isAvmsServiceEnabled && !isServiceRegistered.get() && !isServiceRegistering.get()) {
            isServiceRegistering.set(true)
            nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        }
    }

    private fun unregisterService() {
        Log.d(TAG, "unregisterService")
        if (isServiceRegistered.get()) {
            nsdManager.unregisterService(registrationListener)
        }
    }

    private fun startServer() {
        Log.d(TAG, "startServer")
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                server = embeddedServer(Netty, avmsServicePort) {
                    routing {
                        SubscriptionType.values().forEach { type ->
                            post(type.path) {
                                processSubscriptionRequest(call.receiveText(), type)?.let {
                                    call.respondText(SubscribeResponse(it.isSubscribed).toXml(), ContentType.Application.Xml, HttpStatusCode.OK)
                                    if (it.isSubscribed) {
                                        onSubscribe(it.subscriptionRequest, type)
                                    } else {
                                        onUnsubscribe(it.subscriptionRequest, type)
                                    }
                                }
                            }
                        }
                    }
                }.start(wait = true)
            }
        }
    }

    private fun stopServer() {
        Log.d(TAG, "stopServer")
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                server?.stop(0, 0, TimeUnit.MILLISECONDS)
            }
        }
    }

    private fun processSubscriptionRequest(subscriptionRequestString: String, type: SubscriptionType): RequestResult? {
        Log.d(TAG, "$type: $subscriptionRequestString")
        var result: RequestResult? = null
        subscriptionRequestString.konsumeXml().children(anyName) {
            when(localName) {
                SUBSCRIBE_REQUEST -> {
                    result = RequestResult(SubscriptionRequestParser.fromXml(this), true)
                }
                UNSUBSCRIBE_REQUEST -> {
                    result = RequestResult(SubscriptionRequestParser.fromXml(this), false)
                }
                else -> skipContents()
            }
        }
        return result
    }

    private fun onSubscribe(subscriptionRequest: SubscriptionRequest, type: SubscriptionType) {
        Log.d(TAG, "onSubscribe :: $type :: $subscriptionRequest")
        synchronized(subscribersLock) {
            subscribers[type]?.add(subscriptionRequest)
        }
        sendDelivery(subscriptionRequest, type)
        startDeliveryJobIfNeeded()
    }

    private fun onUnsubscribe(subscriptionRequest: SubscriptionRequest, type: SubscriptionType) {
        Log.d(TAG, "onUnsubscribe :: $type :: $subscriptionRequest")
        synchronized(subscribersLock) {
            subscribers[type]?.remove(subscriptionRequest)
        }
        missedAcknowledgementCounter.remove(subscriptionRequest)
        lastSuccessfulDeliveryTimestamps.remove(subscriptionRequest)
        if (!hasSubscribers) {
            stopDeliveryJob()
        }
    }

    // job checks every 5 sec if there is any subscriber who wasn't updated for 1min+ and sends an update
    private fun startDeliveryJobIfNeeded() {
        if (isAvmsServiceEnabled && hasSubscribers && deliveryJob == null) {
            Log.d(TAG, "startDeliveryJob")
            deliveryJob = viewModelScope.launch(Dispatchers.IO) {
                while (hasSubscribers) { // if there is any subscriber
                    val currentTimeMs = System.currentTimeMillis()
                    synchronized(subscribersLock) {
                        // go over all subscribers and check if they need an update
                        subscribers.entries.forEach { entry ->
                            entry.value.forEach {
                                val lastDeliveryTimestamp = lastSuccessfulDeliveryTimestamps[it]
                                // if last successful delivery not exist or it was > 1 min ago, send a delivery
                                if (lastDeliveryTimestamp == null || currentTimeMs - lastDeliveryTimestamp > DELIVERY_REPEAT_TIMEOUT_IN_MS) {
                                    sendDelivery(it, entry.key)
                                }
                            }
                        }
                    }
                    // sleep for 5 sec until make another run over subscribers
                    delay(DELIVERY_CHECK_TIMEOUT_IN_MS)
                }
                deliveryJob = null
            }
        }
    }

    private fun stopDeliveryJob() {
        deliveryJob?.let {
            Log.d(TAG, "stopDeliveryJob")
            it.cancel()
            deliveryJob = null
        }
    }

    private fun sendDelivery(subscriptionRequest: SubscriptionRequest, type: SubscriptionType) {
        generateDelivery(type)?.let {
            val isReceived = networkClient.sendDeliveryAndWaitForResult(subscriptionRequest.url, it)
            processDeliveryResult(subscriptionRequest, type, isReceived)
            Log.d(TAG, "sendDelivery :: isReceived = $isReceived")
        }
    }

    private fun processDeliveryResult(subscriptionRequest: SubscriptionRequest, type: SubscriptionType, isReceived: Boolean) {
        if (isReceived) {
            missedAcknowledgementCounter.remove(subscriptionRequest)
            lastSuccessfulDeliveryTimestamps[subscriptionRequest] = System.currentTimeMillis()
        } else {
            val count = missedAcknowledgementCounter.getOrElse(subscriptionRequest, { 0 }) + 1
            if (count >= DELIVERY_FAILURE_COUNT_LIMIT) {
                Log.d(TAG, "Subscriber not acknowledged $subscriptionRequest")
                onUnsubscribe(subscriptionRequest, type)
            }
            missedAcknowledgementCounter[subscriptionRequest] = count
        }
    }

    private fun generateDelivery(type: SubscriptionType): IAvmsDelivery? =
            when (type) {
                SubscriptionType.RUN_MONITORING -> generateRunMonitoringDelivery()
                SubscriptionType.PLANNED_PATTERN -> generatePlannedPatternDelivery()
                SubscriptionType.JOURNEY_MONITORING -> generateJourneyMonitoringDelivery()
                SubscriptionType.PATTERN_MONITORING -> generatePatternMonitoringDelivery()
            }

    private fun generateRunMonitoringDelivery(): RunMonitoringDelivery = RunMonitoringDelivery()
    private fun generatePlannedPatternDelivery(): PlannedPatternDelivery = PlannedPatternDelivery()
    private fun generateJourneyMonitoringDelivery(): JourneyMonitoringDelivery = JourneyMonitoringDelivery()
    private fun generatePatternMonitoringDelivery(): PatternMonitoringDelivery = PatternMonitoringDelivery()

    override fun onCleared() {
        unregisterService()
        stopServer()
        super.onCleared()
    }

    inner class AvmsRegistrationListener : NsdManager.RegistrationListener {

        override fun onServiceRegistered(NsdServiceInfo: NsdServiceInfo) {
            Log.d(TAG, "onServiceRegistered - ${NsdServiceInfo.serviceName}")
            // Save the service name. Android may have changed it in order to
            // resolve a conflict, so update the name you initially requested
            // with the name Android actually used.
            serviceName = NsdServiceInfo.serviceName
            isServiceRegistered.set(true)
            isServiceRegistering.set(false)
            startServer()
        }

        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.d(TAG, "Registration failed! errorCode = $errorCode")
            isServiceRegistering.set(false)
        }

        override fun onServiceUnregistered(arg0: NsdServiceInfo) {
            Log.d(TAG, "Service has been unregistered")
            serviceName = null
            isServiceRegistered.set(false)
            stopServer()
            stopDeliveryJob()
        }

        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.d(TAG, "Unregistration failed. errorCode = $errorCode")
        }
    }

    companion object {
        private const val SUBSCRIBE_REQUEST = "SubscribeRequest"
        private const val UNSUBSCRIBE_REQUEST = "UnsubscribeRequest"
    }
}

data class RequestResult(val subscriptionRequest: SubscriptionRequest, val isSubscribed: Boolean)

enum class SubscriptionType(val subpath: String) {
    RUN_MONITORING("runmonitoring"),
    PLANNED_PATTERN("plannedpattern"),
    JOURNEY_MONITORING("journeymonitoring"),
    PATTERN_MONITORING("patternmonitoring");

    val path: String
        get() = "/$SERVICE_PATH$subpath"
}

data class SubscriptionRequest(val clientIpAddress: String, val replyPort: Int, val replyPath: String) {
    val url: String
        get() = "http://$clientIpAddress:$replyPort$replyPath"
}

data class SubscribeResponse(val active: Boolean) {

    fun toXml(): String =
            xml("SubscribeResponse") {
                "Active" {
                    -"$active"
                }
            }.toTrimmedString()
}
