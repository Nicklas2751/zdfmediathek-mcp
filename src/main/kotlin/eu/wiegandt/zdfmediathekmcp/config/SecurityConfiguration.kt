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
 *
 * **Why CSRF is disabled:**
 * - CSRF protection is designed for browser-based applications with cookie sessions
 * - MCP clients (Claude, Copilot, etc.) are not browsers and don't use cookies
 * - MCP uses stateless HTTP requests without session-based authentication
 * - CSRF tokens would block legitimate MCP client requests
 * - There is no CSRF risk without browser-based cookie authentication
 * - This is standard practice for stateless REST APIs and MCP servers
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
            .csrf { it.disable() }  // CSRF not needed for stateless MCP server
            .build()
    }
}

