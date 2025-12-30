package eu.wiegandt.zdfmediathekmcp.mcp.tools

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.client.WireMock.*
import eu.wiegandt.zdfmediathekmcp.model.ZdfBroadcast
import eu.wiegandt.zdfmediathekmcp.model.ZdfBroadcastScheduleResponse
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
class BroadcastScheduleServiceIT {

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
        val response = parseTextContent(
            mcpClient.callTool(
                McpSchema.CallToolRequest(
                    "get_broadcast_schedule",
                    mapOf<String, String>(
                        Pair("from", from),
                        Pair("to", to),
                        Pair("tvService", tvService)
                    )
                )
            )
        )

        // then
        assertThat(response.broadcasts).usingRecursiveComparison()
            .isEqualTo(expectedBroadcasts)
    }

    @Test
    fun `getBroadcastSchedule throws exception for invalid from parameter`() {
        // when
        val result = mcpClient.callTool(
            McpSchema.CallToolRequest(
                "get_broadcast_schedule",
                mapOf<String, String>(
                    Pair("from", "invalid-date"),
                    Pair("to", "2025-12-27T23:59:59+01:00"),
                    Pair("tvService", "ZDF")
                )
            )
        ).block()!!

        // then
        assertThat(result.isError).isTrue()
        assertThat(
            (result.content().first() as McpSchema.TextContent).text()
        ).contains("ISO 8601")
    }

    private fun parseTextContent(result: Mono<McpSchema.CallToolResult>): ZdfBroadcastScheduleResponse {
        return objectMapper.readValue<ZdfBroadcastScheduleResponse>(
            (result.block()!!
                .content()
                .first() as McpSchema.TextContent).text()
        )
    }
}

