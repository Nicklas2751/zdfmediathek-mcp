package eu.wiegandt.zdfmediathekmcp.config

import eu.wiegandt.zdfmediathekmcp.ZdfMediathekService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient


private const val OAUTH2_CLIENT_REGISTRATION_ID = "zdf"

@Configuration
class HttpServicesConfiguration {

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
        val authorizedClientProvider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
            .clientCredentials()
            .build()
        val authorizedClientManager =
            AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                clientRegistrationRepository, authorizedClientService
            )
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
        return authorizedClientManager
    }

    @Bean
    fun proxyFactory(
        clientBuilder: WebClient.Builder,
        authorizedClientManager: ReactiveOAuth2AuthorizedClientManager,
        @Value($$"${zdf.url}") baseUrl: String
    ): HttpServiceProxyFactory {
        val oAuth2filter = ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
        oAuth2filter.setDefaultClientRegistrationId(OAUTH2_CLIENT_REGISTRATION_ID)
        oAuth2filter.setDefaultOAuth2AuthorizedClient(true)

        val client = clientBuilder
            .baseUrl(baseUrl)
            .filter(oAuth2filter)
            .build()
        return HttpServiceProxyFactory.builderFor(WebClientAdapter.create(client)).build()
    }

    @Bean
    fun zdfMediathekService(proxyFactory: HttpServiceProxyFactory): ZdfMediathekService {
        return proxyFactory.createClient<ZdfMediathekService>()
    }

}
