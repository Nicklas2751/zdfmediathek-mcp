package eu.wiegandt.zdfmediathekmcp.mcp.tools

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.client.WireMock.*
import eu.wiegandt.zdfmediathekmcp.model.McpPagedResult
import eu.wiegandt.zdfmediathekmcp.model.SeasonSummary
import eu.wiegandt.zdfmediathekmcp.model.SeriesSummary
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
class ListSeasonsServiceIT {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var webClientBuilder: WebClient.Builder

    private lateinit var mcpClient: McpAsyncClient

    @BeforeEach
    fun setUp() {
        // Prepare Authentication Mock
        stubFor(
            post(urlPathEqualTo("/oauth/token"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"access_token":"test-token","token_type":"Bearer","expires_in":3600}""")
                )
        )

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
    fun `list_seasons tool calls API and returns mapped seasons`() {
        // given
        stubFor(
            get(urlPathEqualTo("/cmdm/seasons"))
                .withQueryParam("limit", equalTo("4"))
                .withQueryParam("page", equalTo("1"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("seasons_response.json")
                )
        )

        val expectedSeason = SeasonSummary(
            seasonUuid = "1e2b3feb-b2b5-4506-ad7c-46a3e28bcfcb",
            seasonNumber = 1,
            title = "Staffel 1",
            brandId = "d7bd086c-16da-4bcb-a630-1c69fc8b4bfb",
            series = SeriesSummary(
                seriesUuid = "6053fe3c-968d-486e-8aea-ce035f58b90b",
                title = "Sketch History",
                description = null, // JSON has seriesDescription: null
                brandId = "d7bd086c-16da-4bcb-a630-1c69fc8b4bfb",
                imdbUrl = null,
                url = "https://www.zdf.de/sketch-history-104"
            )
        )

        // when
        val result = parsePagedResult(
            mcpClient.callTool(
                McpSchema.CallToolRequest(
                    "list_seasons",
                    mapOf<String, Any>("limit" to 4)
                )
            )
        )

        // then
        assertThat(result.resources.first())
            .usingRecursiveComparison()
            .isEqualTo(expectedSeason)
    }

    private fun parsePagedResult(result: Mono<McpSchema.CallToolResult>): McpPagedResult<SeasonSummary> {
        return objectMapper.readValue(
            (result.block()!!
                .content()
                .first() as McpSchema.TextContent).text()
        )
    }
}
