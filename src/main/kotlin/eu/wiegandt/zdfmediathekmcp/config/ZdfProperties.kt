package eu.wiegandt.zdfmediathekmcp.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "zdf")
data class ZdfProperties(
    val clientId: String = "",
    val clientSecret: String = ""
)

