package eu.wiegandt.zdfmediathekmcp.mcp.tools

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.client.WireMock.*
import eu.wiegandt.zdfmediathekmcp.model.EpisodeNode
import io.modelcontextprotocol.client.McpAsyncClient
import io.modelcontextprotocol.client.McpClient
import io.modelcontextprotocol.client.transport.WebClientStreamableHttpTransport
import io.modelcontextprotocol.spec.McpSchema
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
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

    // TODO: Re-enable once proper GraphQL schema is implemented
    /*
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
            episodeInfo = eu.wiegandt.zdfmediathekmcp.model.EpisodeInfo(
                seasonNumber = 2023,
                episodeNumber = 1
            )
        )

        // when
        val result = parseTextContent(
            mcpClient.callTool(
                McpSchema.CallToolRequest(
                    "get_series_episodes",
                    mapOf<String, Any>(
                        "seriesName" to "heute-show",
                        "limit" to 10
                    )
                )
            )
        )

        // then
        assertThat(result.first())
            .usingRecursiveComparison()
            .isEqualTo(expectedEpisode)
    }
    */


    @Test
    fun `get_series_episodes tool returns episodes filtered by season`() {
        // given
        stubFor(
            post(urlEqualTo("/graphql"))
                .withHeader("Authorization", equalTo("Bearer test-token"))
                .withRequestBody(containing("Sketch History"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("get_series_episodes_by_season_response.json")
                )
        )
    // @Test
    // fun `get_series_episodes tool returns episodes filtered by season`() {
    //     // Season filtering not yet implemented
        // when
        val result = parseTextContent(
            mcpClient.callTool(
                McpSchema.CallToolRequest(
                    "get_series_episodes",
                    mapOf<String, Any>(
                        "seriesName" to "Sketch History",
                        "seasonNumber" to 1,
                        "limit" to 10
                    )
                )
            )
        )

        // then
        assertThat(result).isNotEmpty
        assertThat(result.first().episodeInfo?.seasonNumber).isEqualTo(1)
    }
                .content()
                .first() as McpSchema.TextContent).text()
        )
    }
}

