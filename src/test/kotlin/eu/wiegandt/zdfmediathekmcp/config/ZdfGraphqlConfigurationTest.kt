package eu.wiegandt.zdfmediathekmcp.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.graphql.client.HttpGraphQlClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import reactor.core.publisher.Mono
import java.time.Instant

@SpringBootTest
@ExtendWith(OutputCaptureExtension::class)
@TestPropertySource(properties = [
    "zdf.url=http://localhost:8080",
    "zdf.client.id=test-id",
    "zdf.client.secret=test-secret",
    "logging.level.eu.wiegandt.zdfmediathekmcp.config=DEBUG"
])
class ZdfGraphqlConfigurationTest {

    @Autowired
    private lateinit var zdfGraphQlClient: HttpGraphQlClient

    @MockitoBean
    private lateinit var authorizedClientManager: ReactiveOAuth2AuthorizedClientManager

    @Test
    fun `should redact authorization header in logs`(output: CapturedOutput) {
        // Mock the authorized client to return a dummy token
        val clientRegistration = ClientRegistration.withRegistrationId("zdf")
            .clientId("test-id")
            .clientSecret("test-secret")
            .tokenUri("http://localhost/token")
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .build()

        val accessToken = OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "dummy-token",
            Instant.now(),
            Instant.now().plusSeconds(3600)
        )

        val authorizedClient = OAuth2AuthorizedClient(clientRegistration, "test-principal", accessToken)

        `when`(authorizedClientManager.authorize(any())).thenReturn(Mono.just(authorizedClient))

        // We can't easily intercept the real WebClient call here without a full server,
        // but we can test the logging logic by inspecting the bean or forcing a log.
        // However, since the logging is inside a private method added as a filter,
        // the easiest integration way is to make a call that will be logged.
        
        // This call will fail because localhost:8080 probably isn't running the GraphQL endpoint,
        // but the request logging happens BEFORE the response.
        try {
            zdfGraphQlClient.document("{ test }").retrieve("test").toEntity(String::class.java).block()
        } catch (e: Exception) {
            // Expected failure
        }

        // Verify the log output
        assertThat(output.out).contains("Header: Authorization = [REDACTED]")
        assertThat(output.out).doesNotContain("Bearer dummy-token")
    }
}
