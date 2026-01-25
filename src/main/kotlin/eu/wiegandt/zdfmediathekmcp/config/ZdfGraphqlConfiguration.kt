package eu.wiegandt.zdfmediathekmcp.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.client.HttpGraphQlClient
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
private const val OAUTH2_CLIENT_REGISTRATION_ID = "zdf"

@Configuration
class ZdfGraphqlConfiguration {

    private val logger = LoggerFactory.getLogger(ZdfGraphqlConfiguration::class.java)

    @Bean
    fun zdfGraphQlClient(
        @Value("\${zdf.url}") zdfUrl: String,
        authorizedClientManager: ReactiveOAuth2AuthorizedClientManager
    ): HttpGraphQlClient {
        val oAuth2filter = ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
        oAuth2filter.setDefaultClientRegistrationId(OAUTH2_CLIENT_REGISTRATION_ID)
        oAuth2filter.setDefaultOAuth2AuthorizedClient(true)

        val webClient = WebClient.builder()
            .baseUrl("$zdfUrl/graphql")
            .filter(oAuth2filter)
            .filter(logGraphQlRequest())
            .filter(logGraphQlResponse())
            .build()

        return HttpGraphQlClient.builder(webClient).build()
    }

    private fun logGraphQlRequest(): ExchangeFilterFunction {
        return ExchangeFilterFunction.ofRequestProcessor { request ->
            logger.debug("GraphQL Request: {} {}", request.method(), request.url())
            request.headers().forEach { name, values ->
                if (name.equals("Authorization", ignoreCase = true)) {
                    logger.debug("  Header: {} = [REDACTED]", name)
                } else {
                    logger.debug("  Header: {} = {}", name, values)
                }
            }
            // Log request body if available
            request.body()?.let { body ->
                logger.debug("  Request Body: {}", body)
            }
            Mono.just(request)
        }
    }

    private fun logGraphQlResponse(): ExchangeFilterFunction {
        return ExchangeFilterFunction.ofResponseProcessor { response ->
            logger.debug("GraphQL Response: {}", response.statusCode())
            response.headers().asHttpHeaders().forEach { name, values ->
                logger.debug("  Header: {} = {}", name, values)
            }
            if (response.statusCode().isError) {
                logger.error("GraphQL Error Response: Status {}", response.statusCode())
            }
            Mono.just(response)
        }
    }
}
