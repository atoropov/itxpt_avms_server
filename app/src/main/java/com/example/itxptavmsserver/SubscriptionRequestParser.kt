package com.example.itxptavmsserver

import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.anyName

object SubscriptionRequestParser {

    private const val CLIENT_IP_ADDRESS = "Client-IP-Address"
    private const val REPLY_PORT = "ReplyPort"
    private const val REPLY_PATH = "ReplyPath"

    fun fromXml(k: Konsumer): SubscriptionRequest {
        var clientIpAddress: String? = null
        var replyPort: Int? = null
        var replyPath: String? = null
        k.children(anyName) {
            when (localName) {
                CLIENT_IP_ADDRESS -> clientIpAddress = text()
                REPLY_PORT -> replyPort = text().toInt()
                REPLY_PATH -> replyPath = text()
                else -> skipContents() // skip all unknown elements
            }
        }
        return SubscriptionRequest(clientIpAddress!!, replyPort!!, replyPath!!)
    }
}