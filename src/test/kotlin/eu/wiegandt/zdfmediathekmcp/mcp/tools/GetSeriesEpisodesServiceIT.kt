package eu.wiegandt.zdfmediathekmcp.mcp.tools

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.client.WireMock.*
import eu.wiegandt.zdfmediathekmcp.model.EpisodeInfo
import eu.wiegandt.zdfmediathekmcp.model.EpisodeNode
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
class GetSeriesEpisodesServiceIT {

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
    fun `get_series_episodes tool returns episodes for a series`() {
        // given
        stubFor(
            post(urlEqualTo("/graphql"))
                .withHeader("Authorization", equalTo("Bearer test-token"))
                .withRequestBody(containing("heute-show"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("get_series_episodes_response.json")
                )
        )

        val expectedEpisode = EpisodeNode(
            title = "Episode 1",
            editorialDate = "2023-10-27T20:00:00Z",
            sharingUrl = "https://zdf.de/comedy/heute-show/videos/episode-1",
            episodeInfo = EpisodeInfo(
                seasonNumber = 2023,
                episodeNumber = 1
            )
        )

        // when
        val callResult = mcpClient.callTool(
            McpSchema.CallToolRequest(
                "get_series_episodes",
                mapOf<String, Any>(
                    "seriesName" to "heute-show",
                    "limit" to 10
                )
            )
        ).block()

        assertThat(callResult).isNotNull
        val result = parseTextContent(callResult!!)

        // then
        assertThat(result)
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(expectedEpisode)
    }


    private fun parseTextContent(result: McpSchema.CallToolResult): List<EpisodeNode> {
        return objectMapper.readValue(
            (result.content().first() as McpSchema.TextContent).text()
        )
    }
}

