package eu.wiegandt.zdfmediathekmcp.mcp.tools

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.client.WireMock.*
import eu.wiegandt.zdfmediathekmcp.model.CurrentBroadcastResponse
import eu.wiegandt.zdfmediathekmcp.model.ZdfBroadcast
import io.modelcontextprotocol.client.McpAsyncClient
import io.modelcontextprotocol.client.McpClient
import io.modelcontextprotocol.client.transport.WebClientStreamableHttpTransport
import io.modelcontextprotocol.spec.McpSchema
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.web.reactive.function.client.WebClient
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock
import reactor.core.publisher.Mono
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.ai.mcp.client.enabled=true",
        "spring.ai.mcp.client.type=async",
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

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var webClientBuilder: WebClient.Builder

    private lateinit var mcpClient: McpAsyncClient

    @BeforeEach
    fun setUp() {
        val transport = WebClientStreamableHttpTransport.builder(
            webClientBuilder.baseUrl("http://localhost:$port")
        )
            .endpoint("/")
            .build()

        mcpClient = McpClient.async(transport).build()
        mcpClient.initialize().block()
    }

    @AfterEach
    fun tearDown() {
        mcpClient.closeGracefully().block()
    }

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
        val response = parseTextContent(
            mcpClient.callTool(
                McpSchema.CallToolRequest(
                    "get_current_broadcast",
                    mapOf<String, String>(
                        Pair("tvService", tvService)
                    )
                )
            )
        )

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
        assertThat(response.queriedAt.toString()).isNotNull()
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
        val response = parseTextContent(
            mcpClient.callTool(
                McpSchema.CallToolRequest(
                    "get_current_broadcast",
                    mapOf<String, String>(
                        Pair("tvService", tvService)
                    )
                )
            )
        )

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

        // when
        val response = mcpClient.callTool(
            McpSchema.CallToolRequest(
                "get_current_broadcast",
                mapOf<String, String>(
                    Pair("tvService", tvService)
                )
            )
        ).block()!!

        // then
        assertThat(response.isError).isTrue()
        assertThat(
            (response.content().first() as McpSchema.TextContent).text()
        ).contains("404 Not Found from GET")
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

        // when
        val response = mcpClient.callTool(
            McpSchema.CallToolRequest(
                "get_current_broadcast",
                mapOf<String, String>(
                    Pair("tvService", tvService)
                )
            )
        ).block()!!

        // then
        assertThat(response.isError).isTrue()
        assertThat(
            (response.content().first() as McpSchema.TextContent).text()
        ).contains("500 Internal Server Error from GET")
    }

    private fun parseTextContent(result: Mono<McpSchema.CallToolResult>): CurrentBroadcastResponse {
        return objectMapper.readValue<CurrentBroadcastResponse>(
            (result.block()!!
                .content()
                .first() as McpSchema.TextContent).text()
        )
    }
}

