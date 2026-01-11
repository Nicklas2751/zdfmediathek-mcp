package eu.wiegandt.zdfmediathekmcp.mcp.tools

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.client.WireMock.*
import eu.wiegandt.zdfmediathekmcp.model.ZdfSearchResponse
import io.modelcontextprotocol.client.McpAsyncClient
import io.modelcontextprotocol.client.McpClient
import io.modelcontextprotocol.client.transport.ServerParameters
import io.modelcontextprotocol.client.transport.StdioClientTransport
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper
import io.modelcontextprotocol.spec.McpSchema
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.wiremock.spring.EnableWireMock
import java.io.File
import java.time.Duration

/**
 * Integration test for stdio transport.
 *
 * This test starts the Spring Boot application as a subprocess with stdio transport enabled,
 * then connects to it using StdioClientTransport to verify the MCP tools work correctly over stdio.
 *
 * The test uses the compiled classes directly via classpath, which works both when run by
 * Gradle and in the IDE, without requiring a JAR to be built first.
 *
 * The application is started with the 'stdio' profile which:
 * - Disables console output (spring.main.banner-mode=off, logging.pattern.console="")
 * - Enables stdio transport (spring.ai.mcp.server.stdio=true)
 * - Redirects logs to file only
 *
 * This ensures clean stdin/stdout for MCP communication.
 *
 * WireMock is used to mock the ZDF API, avoiding real API calls in tests.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = [
        "zdf.client.id=test-client",
        "zdf.client.secret=test-secret"
    ]
)
@EnableWireMock
class SearchContentServiceStdioIT {
    @Value("\${wiremock.server.baseUrl}")
    private lateinit var wireMockUrl: String

    private lateinit var mcpClient: McpAsyncClient
    private val objectMapper = ObjectMapper().findAndRegisterModules()

    @BeforeEach
    fun setUp() {
        // Setup WireMock stubs for OAuth2 and search
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
            get(urlPathMatching("/search/documents.*"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("search_documents.json")
                )
        )

        // Build classpath from compiled classes and dependencies
        val classpath = buildClasspath()

        // Configure server parameters to start Spring Boot with stdio profile via classpath
        // Use WireMock URL instead of real ZDF API
        val serverParams = ServerParameters.builder("java")
            .args(
                "-cp", classpath,
                "-Dspring.profiles.active=stdio",
                "-Dzdf.client.id=test-client",
                "-Dzdf.client.secret=test-secret",
                "-Dzdf.url=${wireMockUrl}",
                "eu.wiegandt.zdfmediathekmcp.ZdfMediathekMcpApplicationKt"
            )
            .build()

        // Create stdio transport and client
        val jsonMapper = JacksonMcpJsonMapper(ObjectMapper().findAndRegisterModules())
        val transport = StdioClientTransport(serverParams, jsonMapper)

        mcpClient = McpClient.async(transport)
            .requestTimeout(Duration.ofSeconds(60))  // Longer timeout for server startup
            .build()

        // Initialize and wait for server to be ready
        // The initialize() call will block until the server responds
        mcpClient.initialize().block(Duration.ofSeconds(60))
    }

    @AfterEach
    fun tearDown() {
        mcpClient.closeGracefully().block()
    }

    @Test
    fun `search_content tool is available via stdio`() {
        // when
        val availableTools = mcpClient.listTools().block()!!.tools()

        // then
        assertThat(availableTools).anyMatch { tool -> tool.name == "search_content" }
    }

    @Test
    fun `can call search_content tool via stdio`() {
        // given
        val query = "Tagesschau"
        val limit = 2

        // when
        val result = mcpClient.callTool(
            McpSchema.CallToolRequest(
                "search_content",
                mapOf(
                    "query" to query,
                    "limit" to limit
                )
            )
        ).block()

        // then
        assertThat(result).isNotNull
        assertThat(result!!.content()).isNotEmpty

        val textContent = result.content().first() as McpSchema.TextContent
        val response = objectMapper.readValue<ZdfSearchResponse>(textContent.text())

        assertThat(response).isNotNull
        assertThat(response.totalResultsCount).isEqualTo(3104)
        assertThat(response.results).hasSize(2)
        assertThat(response.results[0].title).isEqualTo("tagesschau")

        // Verify WireMock was called with correct parameters
        verify(
            getRequestedFor(urlPathEqualTo("/search/documents"))
                .withQueryParam("q", equalTo("Tagesschau"))
                .withQueryParam("limit", equalTo("2"))
        )
    }

    /**
     * Builds the classpath from Gradle's build directories and dependencies.
     * Works for both Gradle runs and IDE runs.
     */
    private fun buildClasspath(): String {
        val classpathElements = mutableListOf<String>()

        // Add main classes
        val mainClassesDir = File("build/classes/kotlin/main")
        if (mainClassesDir.exists()) {
            classpathElements.add(mainClassesDir.absolutePath)
        }

        // Add main resources
        val mainResourcesDir = File("build/resources/main")
        if (mainResourcesDir.exists()) {
            classpathElements.add(mainResourcesDir.absolutePath)
        }

        // Add all dependencies from Gradle's runtime classpath
        // This reads from the same classpath that this test is using
        val runtimeClasspath = System.getProperty("java.class.path")
        classpathElements.add(runtimeClasspath)

        return classpathElements.joinToString(File.pathSeparator)
    }
}

