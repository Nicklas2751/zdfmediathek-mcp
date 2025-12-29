package eu.wiegandt.zdfmediathekmcp.model

import java.time.OffsetDateTime

/**
 * Response for the get_current_broadcast MCP tool.
 * Contains information about the currently airing program on a ZDF channel.
 */
data class CurrentBroadcastResponse(
    val tvService: String,
    val currentBroadcast: ZdfBroadcast?,
    val queriedAt: OffsetDateTime?
)

