package com.example.itxptavmsserver

interface IAvmsDelivery {
    fun toXml(): String
}

class RunMonitoringDelivery : IAvmsDelivery {

    override fun toXml(): String {
        return """
            <RunMonitoringDelivery version="v2.1.0">
              <MonitoredRunState>
                <RecordedAtTime>2021-04-27T09:32:30.2629641Z</RecordedAtTime>
                <MonitoredBlockRef>   72-08 (1231368)</MonitoredBlockRef>
                <CurrentRunInfo>
                  <RunState>RunToPattern</RunState>
                  <PatternRunType>DeadRunPattern</PatternRunType>
                  <JourneyPatternRef>  72.241</JourneyPatternRef>
                  <VehicleJourneyRef> 72-08P2</VehicleJourneyRef>
                </CurrentRunInfo>
                <NextRunInfo>
                  <RunState>RunToPattern</RunState>
                  <PatternRunType>DeadRunPattern</PatternRunType>
                  <JourneyPatternRef>  72.192</JourneyPatternRef>
                  <VehicleJourneyRef> 72-09P1</VehicleJourneyRef>
                </NextRunInfo>
                <Extensions>
                  <RunMonitoringLogonInfo>
                    <DriverNumber>123456</DriverNumber>
                  </RunMonitoringLogonInfo>
                  <RunMonitoringFareCollection>
                    <Modes>
                      <Mode>On</Mode>
                      <Mode>On</Mode>
                      <Mode>On</Mode>
                      <Mode>On</Mode>
                    </Modes>
                  </RunMonitoringFareCollection>
                </Extensions>
              </MonitoredRunState>
            </RunMonitoringDelivery>
        """.trimIndent().replace("[\t\n]+".toRegex(), "")
    }
}

class PlannedPatternDelivery : IAvmsDelivery {

    override fun toXml(): String {
        return """
            <PlannedPatternDelivery version="v2.1.0">
              <PlannedPattern>
                <RecordedAtTime>2021-04-27T09:32:30.2639612Z</RecordedAtTime>
                <PatternRef>  72.241</PatternRef>
                <VehicleJourneyRef> 72-08P2</VehicleJourneyRef>
                <OperatingDayDate>2021-04-27</OperatingDayDate>
                <RouteRef>  72</RouteRef>
                <LineRef>  72</LineRef>
                <PublishedLineLabel>Oakland Park Blvd Local</PublishedLineLabel>
                <PublishedTtsLineLabel>COPANS ROAD GARAGE</PublishedTtsLineLabel>
                <DirectionRef>1</DirectionRef>
                <ExternalLineRef>  72</ExternalLineRef>
                <OriginName>A1A/NE 36 S</OriginName>
                <DestinationName>COPANS RD GARAGE</DestinationName>
                <DestinationTtsName>COPANS ROAD GARAGE</DestinationTtsName>
                <Extensions>
                  <PlannedPatternFareCollection>
                    <TransportMode>BUS</TransportMode>
                    <TransportModeId>2</TransportModeId>
                    <DriverNumber>123456</DriverNumber>
                  </PlannedPatternFareCollection>
                  <PlannedPatternPassengerInfo>
                    <ForegroundColor>FFFFFF</ForegroundColor>
                    <BackgroundColor>B95915</BackgroundColor>
                  </PlannedPatternPassengerInfo>
                  <PlannedPatternPriorityRequest>
                    <LineNumber>72</LineNumber>
                  </PlannedPatternPriorityRequest>
                </Extensions>
                <PatternStops>
                  <PatternStop>
                    <StopPointRef>2396</StopPointRef>
                    <VisitNumber>0</VisitNumber>
                    <Order>1</Order>
                    <StopPointName>A1A/NE 36 S</StopPointName>
                    <StopPointTtsName>A.1.A AND NORTHEAST 36TH STREET</StopPointTtsName>
                    <DestinationDisplay>NOT IN SERVICE</DestinationDisplay>
                    <Extensions>
                      <PatternStopFareCollection>
                        <RegionCode>RegioYY</RegionCode>
                        <RegionNumber>999</RegionNumber>
                        <FareStopCode>?</FareStopCode>
                        <FareStopShortname>?</FareStopShortname>
                        <FareStopShortDescription>?</FareStopShortDescription>
                      </PatternStopFareCollection>
                      <PatternStopPassengerInfo />
                    </Extensions>
                  </PatternStop>
                </PatternStops>
              </PlannedPattern>
            </PlannedPatternDelivery>
        """.trimIndent().replace("[\t\n]+".toRegex(), "")
    }
}

class JourneyMonitoringDelivery : IAvmsDelivery {

    override fun toXml(): String {
        return """
            <JourneyMonitoringDelivery xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" version="v2.1.0">
              <MonitoredJourney>
                <RecordedAtTime>2021-04-27T10:32:32.0260327Z</RecordedAtTime>
                <ItemIdentifier>0</ItemIdentifier>
                <PatternRef>  72.241</PatternRef>
                <JourneyRef> 72-08P2</JourneyRef>
                <OriginPlannedDepartureTime>2021-04-27T19:38:00Z</OriginPlannedDepartureTime>
                <DestinationPlannedArrivalTime>2021-04-27T20:08:00Z</DestinationPlannedArrivalTime>
                <Occupancy>standingAvailable</Occupancy>
                <Delay>-36329</Delay>
                <PreviousCalls>
                  <PreviousCall>
                    <StopPointRef>2396</StopPointRef>
                    <VisitNumber>0</VisitNumber>
                    <Order>1</Order>
                    <ActualArrivalTime>2021-04-27T09:32:30.2639612Z</ActualArrivalTime>
                    <ActualDepartureTime>0001-01-01T00:00:00</ActualDepartureTime>
                  </PreviousCall>
                </PreviousCalls>
                <MonitoredCall>
                  <StopPointRef>2396</StopPointRef>
                  <VisitNumber>0</VisitNumber>
                  <Order>1</Order>
                  <VehicleAtStop>true</VehicleAtStop>
                  <PlannedArrivalTime>2021-04-27T19:38:00Z</PlannedArrivalTime>
                  <ExpectedArrivalTime>2021-04-27T19:38:00Z</ExpectedArrivalTime>
                  <ActualArrivalTime>2021-04-27T09:32:30.2639612Z</ActualArrivalTime>
                  <PlannedDepartureTime>2021-04-27T19:38:00Z</PlannedDepartureTime>
                  <ExpectedDepartureTime>2021-04-27T19:38:00Z</ExpectedDepartureTime>
                </MonitoredCall>
                <OnwardCalls />
              </MonitoredJourney>
            </JourneyMonitoringDelivery>
        """.trimIndent().replace("[\t\n]+".toRegex(), "")
    }
}

class PatternMonitoringDelivery : IAvmsDelivery {

    override fun toXml(): String {
        return """
            <PatternMonitoringDelivery xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" version="v2.1.0">
              <MonitoredPattern>
                <RecordedAtTime>2021-07-01T10:26:00.2269703Z</RecordedAtTime>
                <ItemIdentifier>0</ItemIdentifier>
                <PatternRef>7.736</PatternRef>
                <MonitoredCallRef>
                  <StopPointRef>49110020</StopPointRef>
                  <VisitNumber>0</VisitNumber>
                  <Order>1</Order>
                  <VehicleAtStop>true</VehicleAtStop>
                </MonitoredCallRef>
                <OnwardCalls>
                  <OnwardCallRef>
                    <StopPointRef>49110340</StopPointRef>
                    <VisitNumber>0</VisitNumber>
                    <Order>3</Order>
                  </OnwardCallRef>
                  <OnwardCallRef>
                    <StopPointRef>49110310</StopPointRef>
                    <VisitNumber>0</VisitNumber>
                    <Order>4</Order>
                  </OnwardCallRef>
                  <OnwardCallRef>
                    <StopPointRef>49110330</StopPointRef>
                    <VisitNumber>0</VisitNumber>
                    <Order>5</Order>
                  </OnwardCallRef>
                  <OnwardCallRef>
                    <StopPointRef>49110240</StopPointRef>
                    <VisitNumber>0</VisitNumber>
                    <Order>6</Order>
                  </OnwardCallRef>
                  <OnwardCallRef>
                    <StopPointRef>49009090</StopPointRef>
                    <VisitNumber>0</VisitNumber>
                    <Order>8</Order>
                  </OnwardCallRef>
                  <OnwardCallRef>
                    <StopPointRef>49004340</StopPointRef>
                    <VisitNumber>0</VisitNumber>
                    <Order>9</Order>
                  </OnwardCallRef>
                  <OnwardCallRef>
                    <StopPointRef>49004240</StopPointRef>
                    <VisitNumber>0</VisitNumber>
                    <Order>11</Order>
                  </OnwardCallRef>
                  <OnwardCallRef>
                    <StopPointRef>49004120</StopPointRef>
                    <VisitNumber>0</VisitNumber>
                    <Order>12</Order>
                  </OnwardCallRef>
                  <OnwardCallRef>
                    <StopPointRef>49000080</StopPointRef>
                    <VisitNumber>0</VisitNumber>
                    <Order>13</Order>
                  </OnwardCallRef>
                  <OnwardCallRef>
                    <StopPointRef>49000040</StopPointRef>
                    <VisitNumber>0</VisitNumber>
                    <Order>14</Order>
                  </OnwardCallRef>
                  <OnwardCallRef>
                    <StopPointRef>49005050</StopPointRef>
                    <VisitNumber>0</VisitNumber>
                    <Order>16</Order>
                  </OnwardCallRef>
                </OnwardCalls>
              </MonitoredPattern>
            </PatternMonitoringDelivery>
        """.trimIndent().replace("[\t\n]+".toRegex(), "")
    }
}