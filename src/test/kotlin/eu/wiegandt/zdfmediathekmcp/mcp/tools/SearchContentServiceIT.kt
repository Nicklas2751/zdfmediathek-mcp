package eu.wiegandt.zdfmediathekmcp.mcp.tools

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.client.WireMock.*
import eu.wiegandt.zdfmediathekmcp.model.McpPagedResult
import eu.wiegandt.zdfmediathekmcp.model.ZdfDocument
import eu.wiegandt.zdfmediathekmcp.model.ZdfSearchResult
import io.modelcontextprotocol.client.McpAsyncClient
import io.modelcontextprotocol.client.McpClient
import io.modelcontextprotocol.client.transport.WebClientStreamableHttpTransport
import io.modelcontextprotocol.spec.McpSchema
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
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
class SearchContentServiceIT {
    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var webClientBuilder: WebClient.Builder

    @Autowired
    lateinit var authorizedClientService: ReactiveOAuth2AuthorizedClientService

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
    fun `search_content tool is available`() {
        // given

        // when
        val availableTools = mcpClient.listTools().block()!!.tools()

        // then
        Assertions.assertThat(availableTools).anyMatch { tool -> tool.name == "search_content" }
    }


    @Test
    fun `search_content valid Query returns expected result`() {
        // given
        val expectedFirst = ZdfSearchResult(
            38.0424,
            "SCMS_index-page-ard-collection_ard_dxjuomfyzdpzag93ojvindnjy2njm2q5oddinde-",
            "page-index",
            "tagesschau",
            "default",
            ZdfDocument(
                id = "collection-index-page-ard-collection-ard-dxjuomfyzdpzag93ojvindnjy2njm2q5oddinde-1042",
                externalId = "SCMS_index-page-ard-collection_ard_dxjuomfyzdpzag93ojvindnjy2njm2q5oddinde-",
                title = null,
                teasertext = "Die Tagesschau ist die älteste und meistgesehene Nachrichtensendung im deutschen Fernsehen. Bis heute der Inbegriff für aktuelle Nachrichten. Seriös und auf den Punkt.",
                editorialDate = OffsetDateTime.parse("2025-12-26T13:37:23.220Z"),
                contentType = "brand",
                hasVideo = false,
                webCanonical = "https://www.zdf.de/magazine/collection-index-page-ard-collection-ard-dxjuomfyzdpzag93ojvindnjy2njm2q5oddinde-1042"
            )
        )

        // Mock OAuth token endpoint
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
            get(urlPathEqualTo("/search/documents"))
                .withQueryParam("q", equalTo("Tagesschau"))
                .withQueryParam("limit", equalTo("2"))
                .withQueryParam("page", equalTo("1"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("search_documents.json")
                )
        )


        // when
        val result = parsePagedResult(
            mcpClient.callTool(
                McpSchema.CallToolRequest(
                    "search_content",
                    mapOf<String, Any>(
                        Pair("query", "Tagesschau"),
                        Pair("limit", 2)
                    )
                )
            )
        )

        // then
        assertThat(result.resources.first()).usingRecursiveComparison().isEqualTo(expectedFirst)
        // ensure nextCursor is present (search_documents.json contains more results than limit)
        assertThat(result.nextCursor).isNotNull()
    }

    @Test
    fun searchDocuments_withOAuth2_sendsAuthorizationHeader() {
        // Clear any cached OAuth2 token to ensure a fresh token request
        authorizedClientService.removeAuthorizedClient("zdf", "anonymousUser").block()

        // given
        stubFor(
            post(urlPathEqualTo("/oauth/token"))
                .withRequestBody(containing("grant_type=client_credentials&client_id=test-client&client_secret=test-secret"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"access_token":"test-token","token_type":"Bearer","expires_in":3600}""")
                )
        )

        stubFor(
            get(urlPathEqualTo("/search/documents"))
                .withHeader("Authorization", equalTo("Bearer test-token"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("search_documents.json")
                )
        )

        // when
        val result = parsePagedResult(
            mcpClient.callTool(
                McpSchema.CallToolRequest(
                    "search_content",
                    mapOf<String, Any>(
                        Pair("query", "Tagesschau"),
                        Pair("limit", 2)
                    )
                )
            )
        )

        // then
        assertThat(result).isNotNull()
        verify(postRequestedFor(urlPathEqualTo("/oauth/token")))
        verify(
            getRequestedFor(urlPathEqualTo("/search/documents"))
                .withHeader("Authorization", equalTo("Bearer test-token"))
        )
    }

    private fun parsePagedResult(result: Mono<McpSchema.CallToolResult>): McpPagedResult<ZdfSearchResult> {
        return objectMapper.readValue(
            (result.block()!!
                .content()
                .first() as McpSchema.TextContent).text()
        )
    }
}