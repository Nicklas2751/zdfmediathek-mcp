package eu.wiegandt.zdfmediathekmcp.config

import eu.wiegandt.zdfmediathekmcp.ZdfMediathekService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient
import reactor.core.publisher.Mono


private const val OAUTH2_CLIENT_REGISTRATION_ID = "zdf"

@Configuration
class HttpServicesConfiguration {

    private val logger = LoggerFactory.getLogger(HttpServicesConfiguration::class.java)

    /**
     * Configures a [ReactiveOAuth2AuthorizedClientManager] to handle OAuth2 client credentials flow. <br/>
     * This is needed because we have no `ServerWebExchange`. The spring documentation says:
     * > The DefaultReactiveOAuth2AuthorizedClientManager is designed to be used within the context of a ServerWebExchange. When operating outside of a ServerWebExchange context, use AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager instead.
     * @see <a href="https://docs.spring.io/spring-security/reference/reactive/oauth2/client/core.html#oauth2Client-authorized-manager-provider">Spring Security Documentation</a
     */
    @Bean
    fun authorizedClientManager(
        clientRegistrationRepository: ReactiveClientRegistrationRepository,
        authorizedClientService: ReactiveOAuth2AuthorizedClientService
    ): ReactiveOAuth2AuthorizedClientManager {
        logger.info("Configuring OAuth2 client manager for ZDF API")
        val authorizedClientProvider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
            .clientCredentials()
            .build()
        val authorizedClientManager =
            AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                clientRegistrationRepository, authorizedClientService
            )
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
        logger.debug("OAuth2 client manager configured with client credentials flow")
        return authorizedClientManager
    }

    @Bean
    fun proxyFactory(
        clientBuilder: WebClient.Builder,
        authorizedClientManager: ReactiveOAuth2AuthorizedClientManager,
        @Value($$"${zdf.url}") baseUrl: String
    ): HttpServiceProxyFactory {
        logger.info("Configuring WebClient for ZDF API at: {}", baseUrl)

        val oAuth2filter = ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
        oAuth2filter.setDefaultClientRegistrationId(OAUTH2_CLIENT_REGISTRATION_ID)
        oAuth2filter.setDefaultOAuth2AuthorizedClient(true)

        val client = clientBuilder
            .baseUrl(baseUrl)
            .filter(oAuth2filter)
            .filter(logRequest())
            .filter(logResponse())
            .build()

        logger.debug("WebClient configured with OAuth2 filter and logging")
        return HttpServiceProxyFactory.builderFor(WebClientAdapter.create(client)).build()
    }

    @Bean
    fun zdfMediathekService(proxyFactory: HttpServiceProxyFactory): ZdfMediathekService {
        logger.info("Creating ZDF Mediathek Service proxy")
        return proxyFactory.createClient<ZdfMediathekService>()
    }

    private fun logRequest(): ExchangeFilterFunction {
        return ExchangeFilterFunction.ofRequestProcessor { request ->
            logger.debug("HTTP Request: {} {}", request.method(), request.url())
            logger.debug("Request headers: {}", request.headers())
            Mono.just(request)
        }
    }

    private fun logResponse(): ExchangeFilterFunction {
        return ExchangeFilterFunction.ofResponseProcessor { response ->
            logger.debug("HTTP Response: {} {}", response.statusCode(), response.statusCode().value())
            logger.debug("Response headers: {}", response.headers().asHttpHeaders())
            Mono.just(response)
        }
    }

}
