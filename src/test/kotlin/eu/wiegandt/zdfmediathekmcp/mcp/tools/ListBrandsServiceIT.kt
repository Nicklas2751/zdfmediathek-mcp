package eu.wiegandt.zdfmediathekmcp.mcp.tools

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.client.WireMock.*
import eu.wiegandt.zdfmediathekmcp.model.BrandApiResponse
import eu.wiegandt.zdfmediathekmcp.model.BrandSummary
import eu.wiegandt.zdfmediathekmcp.model.McpPagedResult
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
class ListBrandsServiceIT {

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
    fun `listBrands returns mapped brands from API`() {
        // given: OAuth und Brands-API werden gemockt
        stubFor(
            post(urlPathEqualTo("/oauth/token"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"access_token":"test-token","token_type":"Bearer","expires_in":3600}""")
                )
        )

        stubFor(
            get(urlPathEqualTo("/cmdm/brands"))
                .withQueryParam("limit", equalTo("10"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("brands_real_example.json")
                )
        )
        val expected = BrandApiResponse(
            listOf(
                BrandSummary(
                    brandUuid = "87b853d7-78ca-43a3-9c20-383774d1cbad",
                    brandName = "GIRLS",
                    brandDescription = null
                ),
                BrandSummary(
                    brandUuid = "56eb6908-b786-4990-abba-393e6a78f9fe",
                    brandName = "Der Geschichts-Check",
                    brandDescription = null
                ),
                BrandSummary(
                    brandUuid = "e775e35e-004c-4df2-a83b-44a13c077273",
                    brandName = "der-satirische-jahresrueckblick-2013",
                    brandDescription = null
                ),
                BrandSummary(
                    brandUuid = "4d789834-7e6e-4bd1-ba7e-e096c7063f7a",
                    brandName = "Und dann noch Paula",
                    brandDescription = null
                ),
                BrandSummary(
                    brandUuid = "3876f7c9-65f7-45cd-8ad1-e38678b30a47",
                    brandName = "astrid-lindgren",
                    brandDescription = null
                ),
                BrandSummary(
                    brandUuid = "8a603cd4-1bec-4b16-817c-f614d1c649ef",
                    brandName = "ZDF Wintersport",
                    brandDescription = null
                ),
                BrandSummary(
                    brandUuid = "e0649b80-8a17-4579-bd42-764241e84aa7",
                    brandName = "der-kleine-drache-kokosnuss",
                    brandDescription = null
                ),
                BrandSummary(
                    brandUuid = "0349a7ad-4590-427d-8c77-956263e6b224",
                    brandName = "Udo Jürgens - Mitten im Leben",
                    brandDescription = null
                ),
                BrandSummary(
                    brandUuid = "554e767c-010d-36a6-9be7-ceec661f1b5a",
                    brandName = "Missing Link",
                    brandDescription = null
                ),
                BrandSummary(
                    brandUuid = "6cb9a8a5-3fea-4beb-bc6a-fa222536969f",
                    brandName = "Ein Fall fürs All",
                    brandDescription = null
                )
            )
        )

        // when: Service wird aufgerufen
        val result = parseTextContent(
            mcpClient.callTool(
                McpSchema.CallToolRequest(
                    "list_brands",
                    mapOf<String, String>(
                        Pair("limit", "10")
                    )
                )
            )
        )

        // then: Ergebnis entspricht Erwartung, Request wurde korrekt abgesetzt
        assertThat(result.resources).usingRecursiveComparison().isEqualTo(expected.brands)
        // If the API returned a full page (10 items), we expect a nextCursor to be provided
        assertThat(result.nextCursor).isNotNull()
        // Verify the cursor decodes to page=2 and limit=10
        val decoded = eu.wiegandt.zdfmediathekmcp.mcp.pagination.McpPaginationPayloadHandler.decode(result.nextCursor!!)
        assertThat(decoded.page).isEqualTo(2)
        assertThat(decoded.limit).isEqualTo(10)
        verify(
            getRequestedFor(urlPathEqualTo("/cmdm/brands"))
                .withQueryParam("limit", equalTo("10"))
        )
    }

    @Test
    fun `listBrands with limit parameter passes limit`() {
        // given
        stubFor(
            post(urlPathEqualTo("/oauth/token"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"access_token":"test-token","token_type":"Bearer","expires_in":3600}""")
                )
        )
        stubFor(
            get(urlPathEqualTo("/cmdm/brands"))
                .withQueryParam("limit", equalTo("5"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"brands":[]}""")
                )
        )
        val expected = BrandApiResponse(emptyList())

        // when
        val result = parseTextContent(
            mcpClient.callTool(
                McpSchema.CallToolRequest(
                    "list_brands",
                    mapOf<String, String>(
                        Pair("limit", "5")
                    )
                )
            )
        )

        // then
        assertThat(result.resources).usingRecursiveComparison().isEqualTo(expected.brands)
        assertThat(result.nextCursor).isNull()
        verify(
            getRequestedFor(urlPathEqualTo("/cmdm/brands"))
                .withQueryParam("limit", equalTo("5"))
        )
    }

    @Test
    fun `listBrands when API returns 401 throws exception`() {
        // given
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
        val result = mcpClient.callTool(
            McpSchema.CallToolRequest(
                "list_brands",
                emptyMap<String, String>()
            )
        ).block()!!

        // then
        assertThat(result.isError).isTrue()
        assertThat((result.content().first() as McpSchema.TextContent).text()).contains("404 Not Found from GET")
    }

    @Test
    fun `listBrands when API returns 500 throws exception`() {
        // given
        stubFor(
            post(urlPathEqualTo("/oauth/token"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"access_token":"test-token","token_type":"Bearer","expires_in":3600}""")
                )
        )
        stubFor(
            get(urlPathEqualTo("/cmdm/brands"))
                .withQueryParam("limit", equalTo("10"))
                .willReturn(
                    aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"error":"Internal Server Error"}""")
                )
        )


        // when
        val result = mcpClient.callTool(
            McpSchema.CallToolRequest(
                "list_brands",
                emptyMap<String, String>()
            )
        ).block()!!

        // then
        assertThat(result.isError).isTrue()
        assertThat(
            (result.content().first() as McpSchema.TextContent).text()
        ).contains("500 Internal Server Error from GET")
    }

    private fun parseTextContent(result: Mono<McpSchema.CallToolResult>): McpPagedResult<BrandSummary> {
        return objectMapper.readValue(
            (result.block()!!
                .content()
                .first() as McpSchema.TextContent).text()
        )
    }
}
