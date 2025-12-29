package eu.wiegandt.zdfmediathekmcp

import com.github.tomakehurst.wiremock.client.WireMock.*
import eu.wiegandt.zdfmediathekmcp.model.CurrentBroadcastResponse
import eu.wiegandt.zdfmediathekmcp.model.ZdfBroadcast
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset

@SpringBootTest(
    properties = [
        "zdf.client.id=test-client",
        "zdf.client.secret=test-secret"
    ]
)
@EnableWireMock(
    ConfigureWireMock(
        baseUrlProperties = ["zdf.url"]
    )
)
class CurrentBroadcastServiceIT {

    @Autowired
    lateinit var currentBroadcastService: CurrentBroadcastService

    @Test
    fun `getCurrentBroadcast with real API returns current broadcast`() {
        // given
        val tvService = "ZDF"
        val now = OffsetDateTime.now(ZoneId.of("Europe/Berlin"))

        // Create a broadcast that is currently airing
        val currentBroadcast = ZdfBroadcast(
            airtimeBegin = now.minusMinutes(30),
            airtimeEnd = now.plusMinutes(30),
            duration = 3600,
            tvService = tvService,
            title = "Tagesschau",
            subtitle = null,
            text = "Die Nachrichtensendung",
            programmeItem = "/cmdm/epg/programme-items/POS_TAGESSCHAU"
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

        // Mock broadcast schedule endpoint - match any query params since they include timestamps
        stubFor(
            get(urlPathMatching("/cmdm/epg/broadcasts/pf"))
                .withQueryParam("tvService", equalTo(tvService))
                .withQueryParam("limit", equalTo("10"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                              "http://zdf.de/rels/cmdm/broadcasts": [
                                {
                                  "airtimeBegin": "${currentBroadcast.airtimeBegin}",
                                  "airtimeEnd": "${currentBroadcast.airtimeEnd}",
                                  "duration": ${currentBroadcast.duration},
                                  "tvService": "${currentBroadcast.tvService}",
                                  "title": "${currentBroadcast.title}",
                                  "subtitle": null,
                                  "text": "${currentBroadcast.text}",
                                  "http://zdf.de/rels/cmdm/programme-item": "${currentBroadcast.programmeItem}"
                                }
                              ],
                              "next-archive": null
                            }
                            """.trimIndent()
                        )
                )
        )

        // when
        val response = currentBroadcastService.getCurrentBroadcast(tvService)

        // then
        assertThat(response).usingRecursiveComparison()
            .withEqualsForType(
                { a, b ->
                    a.withOffsetSameInstant(ZoneOffset.UTC) == b.withOffsetSameInstant(ZoneOffset.UTC)
                },
                OffsetDateTime::class.java
            )
            .ignoringFields("queriedAt")
            .isEqualTo(
                CurrentBroadcastResponse(
                    tvService = tvService,
                    currentBroadcast = currentBroadcast,
                    queriedAt = null // ignored
                )
            )
        assertThat(response.queriedAt.toString()).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+[+-]\\d{2}:\\d{2}")
    }

    @Test
    fun `getCurrentBroadcast with custom limit calls API with correct limit`() {
        // given
        val tvService = "ZDFneo"
        val limit = 20

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
            get(urlPathEqualTo("/cmdm/epg/broadcasts/pf"))
                .withQueryParam("tvService", equalTo(tvService))
                .withQueryParam("limit", equalTo(limit.toString()))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                              "http://zdf.de/rels/cmdm/broadcasts": [],
                              "next-archive": null
                            }
                        """.trimIndent()
                        )
                )
        )

        // when
        val response = currentBroadcastService.getCurrentBroadcast(tvService, limit)

        // then
        assertThat(response.tvService).isEqualTo(tvService)
    }

    @Test
    fun `getCurrentBroadcast when API returns no results handles gracefully`() {
        // given
        val tvService = "UnknownChannel"

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

        // Mock broadcast schedule endpoint - empty results
        stubFor(
            get(urlPathEqualTo("/cmdm/epg/broadcasts/pf"))
                .withQueryParam("tvService", equalTo(tvService))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                              "http://zdf.de/rels/cmdm/broadcasts": [],
                              "next-archive": null
                            }
                        """.trimIndent()
                        )
                )
        )

        // when
        val response = currentBroadcastService.getCurrentBroadcast(tvService)

        // then
        assertThat(response).usingRecursiveComparison()
            .ignoringFields("queriedAt")
            .isEqualTo(
                CurrentBroadcastResponse(
                    tvService = tvService,
                    currentBroadcast = null,
                    queriedAt = null // ignored
                )
            )
        assertThat(response.queriedAt).isNotNull()
    }

    @Test
    fun `getCurrentBroadcast when API returns 401 throws exception`() {
        // given
        val tvService = "ZDF"

        // Mock OAuth token endpoint to fail
        stubFor(
            post(urlPathEqualTo("/oauth/token"))
                .willReturn(
                    aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"error":"invalid_client"}""")
                )
        )

        // when / then
        assertThatThrownBy {
            currentBroadcastService.getCurrentBroadcast(tvService)
        }.isInstanceOf(RuntimeException::class.java)
    }

    @Test
    fun `getCurrentBroadcast when API returns 500 throws exception`() {
        // given
        val tvService = "ZDF"

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

        // Mock broadcast schedule endpoint - server error
        stubFor(
            get(urlPathEqualTo("/cmdm/epg/broadcasts/pf"))
                .withQueryParam("tvService", equalTo(tvService))
                .willReturn(
                    aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"error":"Internal Server Error"}""")
                )
        )

        // when / then
        assertThatThrownBy {
            currentBroadcastService.getCurrentBroadcast(tvService)
        }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessageContaining("Failed to get current broadcast")
    }
}

