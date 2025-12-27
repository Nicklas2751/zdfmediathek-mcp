package eu.wiegandt.zdfmediathekmcp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

/**
 * Security configuration for the MCP server.
 *
 * This configuration disables authentication for all incoming HTTP requests,
 * as the MCP server does not require authentication for its endpoints.
 *
 * Spring Security is only used for the OAuth2 client functionality
 * (to authenticate outgoing requests to the ZDF API).
 */
@Configuration
@EnableWebFluxSecurity
class SecurityConfiguration {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .authorizeExchange { authorize ->
                authorize.anyExchange().permitAll()
            }
            .csrf { it.disable() }
            .build()
    }
}

