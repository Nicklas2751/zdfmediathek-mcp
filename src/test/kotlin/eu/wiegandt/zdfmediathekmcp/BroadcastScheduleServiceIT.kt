package eu.wiegandt.zdfmediathekmcp

import com.github.tomakehurst.wiremock.client.WireMock.*
import eu.wiegandt.zdfmediathekmcp.model.ZdfBroadcast
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock
import java.time.OffsetDateTime

@SpringBootTest(
    properties = [
        "zdf.clientId=test-client",
        "zdf.clientSecret=test-secret"
    ]
)
@EnableWireMock(
    ConfigureWireMock(
        baseUrlProperties = ["zdf.url"]
    )
)
class BroadcastScheduleServiceIT {

    @Autowired
    lateinit var broadcastScheduleService: BroadcastScheduleService

    @Test
    fun `getBroadcastSchedule with valid parameters returns schedule`() {
        // given
        val from = "2025-12-27T00:00:00+01:00"
        val to = "2025-12-27T23:59:59+01:00"
        val tvService = "ZDF"

        val expectedBroadcasts = listOf(
            ZdfBroadcast(
                airtimeBegin = OffsetDateTime.parse("2025-12-27T19:15:00Z"),
                airtimeEnd = OffsetDateTime.parse("2025-12-27T20:45:00Z"),
                duration = 5400,
                tvService = "ZDF",
                title = "Tatort",
                subtitle = "Der letzte Schrei",
                text = "Kriminalfilm aus Hamburg",
                programmeItem = "/cmdm/epg/programme-items/POS_12345"
            ),
            ZdfBroadcast(
                airtimeBegin = OffsetDateTime.parse("2025-12-27T20:45:00Z"),
                airtimeEnd = OffsetDateTime.parse("2025-12-27T21:15:00Z"),
                duration = 1800,
                tvService = "ZDF",
                title = "heute journal",
                subtitle = null,
                text = "Die Nachrichtensendung des ZDF",
                programmeItem = "/cmdm/epg/programme-items/POS_67890"
            )
        )

        // Mock OAuth token
        stubFor(
            post(urlPathEqualTo("/oauth/token"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"access_token":"test-token","token_type":"Bearer","expires_in":3600}""")
                )
        )

        // Mock broadcast schedule endpoint
        stubFor(
            get(urlPathEqualTo("/cmdm/epg/broadcasts"))
                .withQueryParam("from", equalTo(from))
                .withQueryParam("to", equalTo(to))
                .withQueryParam("tvService", equalTo(tvService))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("broadcast_schedule_response.json")
                )
        )

        // when
        val response = broadcastScheduleService.getBroadcastSchedule(from, to, tvService)

        // then
        assertThat(response.broadcasts).usingRecursiveComparison()
            .isEqualTo(expectedBroadcasts)
    }

    @Test
    fun `getBroadcastSchedule throws exception for invalid from parameter`() {
        // when / then
        val exception = assertThrows<IllegalArgumentException> {
            broadcastScheduleService.getBroadcastSchedule("invalid-date", "2025-12-27T23:59:59+01:00", "ZDF")
        }

        assertThat(exception.message).contains("ISO 8601")
    }
}

