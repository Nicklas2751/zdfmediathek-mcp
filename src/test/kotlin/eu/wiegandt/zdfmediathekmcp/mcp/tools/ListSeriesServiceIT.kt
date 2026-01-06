package eu.wiegandt.zdfmediathekmcp.mcp.tools

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.client.WireMock.*
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
class ListSeriesServiceIT {

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
    fun `list_series tool calls API and returns mapped series`() {
        // given
        stubFor(
            get(urlPathEqualTo("/cmdm/series"))
                .withQueryParam("limit", equalTo("4"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("series_response.json")
                )
        )

        val expectedSeries = SeriesSummary(
            seriesUuid = "4f0f18f3-f69f-4979-b96a-8876e019cf12",
            title = "heute-show",
            description = null,
            brandId = "3a844b94-6760-3991-aa58-a323ae4fad8e",
            imdbUrl = "https://www.imdb.com/title/tt1441143/",
            url = "https://www.zdf.de/heute-show-104"
        )


        // when
        val result = parseTextContent(
            mcpClient.callTool(
                McpSchema.CallToolRequest(
                    "list_series",
                    mapOf<String, Any>("limit" to 4)
                )
            )
        )

        // then
        assertThat(result).isNotEmpty
        assertThat(result.first())
            .usingRecursiveComparison()
            .isEqualTo(expectedSeries)
    }

    private fun parseTextContent(result: Mono<McpSchema.CallToolResult>): List<SeriesSummary> {
        return objectMapper.readValue(
            (result.block()!!
                .content()
                .first() as McpSchema.TextContent).text()
        )
    }
}
