package eu.wiegandt.zdfmediathekmcp.config

import eu.wiegandt.zdfmediathekmcp.ZdfMediathekService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient


@Configuration
class HttpServicesConfiguration {

    @Bean
    fun proxyFactory(
        clientBuilder: WebClient.Builder,
        @Value($$"${zdf.url}") baseUrl: String
    ): HttpServiceProxyFactory {
        val client = clientBuilder
            .baseUrl(baseUrl)
            .defaultHeader("Api-Auth", "Bearer aa3noh4ohz9eeboo8shiesheec9ciequ9Quah7el")
            .build()
        return HttpServiceProxyFactory.builderFor(WebClientAdapter.create(client)).build()
    }

    @Bean
    fun zdfMediathekService(proxyFactory: HttpServiceProxyFactory): ZdfMediathekService {
        return proxyFactory.createClient<ZdfMediathekService>()
    }

}

