package eu.wiegandt.zdfmediathekmcp

import eu.wiegandt.zdfmediathekmcp.model.CurrentBroadcastResponse
import eu.wiegandt.zdfmediathekmcp.model.ZdfBroadcast
import org.slf4j.LoggerFactory
import org.springaicommunity.mcp.annotation.McpTool
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

/**
 * MCP tool service for retrieving the currently airing program on ZDF channels.
 *
 * Provides the `get_current_broadcast` tool that allows AI assistants to quickly
 * find out what is currently on air on a specific channel.
 */
@Service
class CurrentBroadcastService(
    private val zdfMediathekService: ZdfMediathekService
) {

    private val logger = LoggerFactory.getLogger(CurrentBroadcastService::class.java)

    /**
     * Retrieves the currently airing program on a specific ZDF channel.
     *
     * @param tvService Channel name (required, e.g., "ZDF", "ZDFneo", "3sat")
     * @param limit Maximum number of broadcasts to fetch from API (default: 10)
     * @return Response containing the current broadcast or null if none is airing
     * @throws IllegalArgumentException if parameters are invalid
     */
    @McpTool(
        name = "get_current_broadcast",
        description = "Get the currently airing program on a specific ZDF channel. " +
                "Returns the broadcast that is currently on air at the time of the request. " +
                "Parameters: " +
                "- tvService: Channel name (required, e.g., ZDF, ZDFneo, 3sat, ZDFinfo, PHOENIX, KIKA). " +
                "- limit: Maximum number of broadcasts to return (default: 10). " +
                "Common channels: ZDF, ZDFneo, ZDFinfo, 3sat, PHOENIX, KIKA. " +
                "Returns the current program with title, time, description, and channel info."
    )
    fun getCurrentBroadcast(tvService: String, limit: Int = 10): CurrentBroadcastResponse {
        logger.info(
            "MCP Tool 'get_current_broadcast' called with tvService='{}', limit={}",
            tvService, limit
        )

        try {
            // Validate required parameters
            require(tvService.isNotBlank()) {
                "Parameter 'tvService' is required and must not be empty"
            }
            require(limit > 0) {
                "Parameter 'limit' must be greater than 0"
            }

            // Call ZDF API
            val scheduleResponse = zdfMediathekService.getCurrentBroadcastSchedule(tvService, limit)

            logger.debug("Received {} broadcasts from API", scheduleResponse.broadcasts.size)

            // Find the currently airing broadcast
            val currentBroadcast = findCurrentBroadcast(scheduleResponse.broadcasts)
            val queriedAt = OffsetDateTime.now()

            if (currentBroadcast != null) {
                logger.info(
                    "Found current broadcast: '{}' on {} (started: {})",
                    currentBroadcast.title, tvService, currentBroadcast.airtimeBegin
                )
            } else {
                logger.info("No current broadcast found for {} at {}", tvService, queriedAt)
            }

            return CurrentBroadcastResponse(
                tvService = tvService,
                currentBroadcast = currentBroadcast,
                queriedAt = queriedAt
            )
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid parameter for get_current_broadcast: {}", e.message)
            throw e
        } catch (e: Exception) {
            logger.error(
                "Error executing get_current_broadcast for tvService '{}': {}",
                tvService, e.message, e
            )
            throw RuntimeException("Failed to get current broadcast: ${e.message}", e)
        }
    }

    /**
     * Finds the broadcast that is currently airing at the given time.
     *
     * @param broadcasts List of broadcasts to search through
     * @return The currently airing broadcast, or null if none found
     */
    private fun findCurrentBroadcast(
        broadcasts: List<ZdfBroadcast>
    ): ZdfBroadcast? {
        val now = OffsetDateTime.now()
        return broadcasts.firstOrNull { broadcast ->
            !broadcast.airtimeBegin.isAfter(now) && broadcast.airtimeEnd.isAfter(now)
        }
    }
}

