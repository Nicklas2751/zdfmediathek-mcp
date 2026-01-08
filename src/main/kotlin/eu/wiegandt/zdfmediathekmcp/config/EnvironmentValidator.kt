package eu.wiegandt.zdfmediathekmcp.config

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Validates required environment variables on application startup.
 * Fails fast if required credentials are not configured.
 */
@Component
class EnvironmentValidator(
    @param:Value("\${zdf.client.id}") private val clientId: String,
    @param:Value("\${zdf.client.secret}") private val clientSecret: String
) {
    private val logger = LoggerFactory.getLogger(EnvironmentValidator::class.java)

    @PostConstruct
    fun validateEnvironment() {
        logger.info("Validating required environment variables...")

        val errors = mutableListOf<String>()

        // Validate ZDF Client ID
        if (clientId.isBlank() || clientId == "mediathek-search") {
            errors.add("ZDF_CLIENT_ID is not configured or using default value. Please set a valid client ID from https://developer.zdf.de/limited-access")
        }

        // Validate ZDF Client Secret
        if (clientSecret.isBlank() || clientSecret == "ZDFmediathekSearchClientSecret") {
            errors.add("ZDF_CLIENT_SECRET is not configured or using default value. Please set a valid client secret from https://developer.zdf.de/limited-access")
        }

        if (errors.isNotEmpty()) {
            logger.error("Environment validation failed!")
            errors.forEach { logger.error("  - {}", it) }
            throw IllegalStateException(
                "Required environment variables are not configured:\n${errors.joinToString("\n")}\n" +
                        "Please set ZDF_CLIENT_ID and ZDF_CLIENT_SECRET environment variables."
            )
        }

        logger.info("Environment validation successful")
    }
}

