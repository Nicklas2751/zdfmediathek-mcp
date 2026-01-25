package eu.wiegandt.zdfmediathekmcp.mcp.tools

import eu.wiegandt.zdfmediathekmcp.ZdfMediathekClient
import eu.wiegandt.zdfmediathekmcp.mcp.pagination.McpPaginationPayloadHandler
import eu.wiegandt.zdfmediathekmcp.model.McpPagedResult
import eu.wiegandt.zdfmediathekmcp.model.ZdfBroadcast
import eu.wiegandt.zdfmediathekmcp.model.ZdfBroadcastScheduleResponse
import org.slf4j.LoggerFactory
import org.springaicommunity.mcp.annotation.McpTool
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

/**
 * MCP tool service for retrieving TV broadcast schedules from ZDF Mediathek.
 *
 * Provides the `get_broadcast_schedule` tool that allows AI assistants to query
 * TV program schedules for ZDF channels using flexible time ranges.
 */
@Service
class BroadcastScheduleService(
    private val zdfMediathekClient: ZdfMediathekClient
) {

    private val logger = LoggerFactory.getLogger(BroadcastScheduleService::class.java)

    /**
     * Retrieves the TV broadcast schedule for a specific time range and optional channel.
     *
     * @param from Start time in ISO 8601 format with timezone (e.g., "2025-12-27T00:00:00+01:00")
     * @param to End time in ISO 8601 format with timezone (e.g., "2025-12-27T23:59:59+01:00")
     * @param tvService Optional channel name (e.g., "ZDF", "ZDFneo"). If null, returns all channels.
     * @return The broadcast schedule response containing a list of programs.
     * @throws IllegalArgumentException if parameters are invalid or time format is wrong.
     */
    @McpTool(
        name = "get_broadcast_schedule",
        description = """Get the TV broadcast schedule for ZDF channels within a specific time range. 
            Parameters: 
            - from: Start time in ISO 8601 format with timezone (e.g., 2025-12-27T00:00:00+01:00) 
            - to: End time in ISO 8601 format with timezone (e.g., 2025-12-27T23:59:59+01:00) 
            - tvService: Optional channel name (e.g., ZDF, ZDFneo, 3sat). If omitted, returns all channels. 
            - limit: Maximum number of broadcasts to return (default: 10). 
            - cursor: Optional MCP pagination cursor (Base64-encoded JSON {"page":X,"limit":Y})
            Returns a paged result with broadcasts and an optional nextCursor.
            """
    )
    fun getBroadcastSchedule(
        from: String,
        to: String,
        tvService: String? = null,
        limit: Int? = 10,
        cursor: String? = null
    ): McpPagedResult<ZdfBroadcast> {
        val actualLimit = limit ?: 10
        var page = 1

        logger.info(
            "MCP Tool 'get_broadcast_schedule' called with from='{}', to='{}', tvService='{}', limit={}, cursorPresent={}",
            from, to, tvService ?: "all", actualLimit, !cursor.isNullOrBlank()
        )

        try {
            // Validate required parameters
            require(from.isNotBlank()) { "Parameter 'from' is required and must not be empty" }
            require(to.isNotBlank()) { "Parameter 'to' is required and must not be empty" }
            require(actualLimit > 0) { "Parameter 'limit' must be greater than 0" }

            val fromDateTime = parseIso8601OrThrow(from, "from")
            val toDateTime = parseIso8601OrThrow(to, "to")

            require(fromDateTime.isBefore(toDateTime)) { "Parameter 'from' must be before 'to'" }

            if (!cursor.isNullOrBlank()) {
                try {
                    val payload = McpPaginationPayloadHandler.decode(cursor)
                    page = payload.page
                } catch (e: IllegalArgumentException) {
                    logger.warn("Invalid pagination cursor provided for get_broadcast_schedule: {}", e.message)
                    throw e
                }
            }

            logger.debug("Calling ZDF API to get broadcast schedule (limit={}, page={})", actualLimit, page)
            val response: ZdfBroadcastScheduleResponse = zdfMediathekClient.getBroadcastSchedule(from, to, tvService, actualLimit, page)

            logger.info(
                "Successfully retrieved {} broadcasts for time range {} to {}",
                response.broadcasts.size, from, to
            )

            val nextCursor = if (response.broadcasts.size >= actualLimit) {
                McpPaginationPayloadHandler.encode(page + 1, actualLimit)
            } else {
                null
            }

            return McpPagedResult(resources = response.broadcasts, nextCursor = nextCursor)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid parameter for get_broadcast_schedule: {}", e.message)
            throw e
        } catch (e: Exception) {
            logger.error(
                "Error executing get_broadcast_schedule for time range {} to {}: {}",
                from, to, e.message, e
            )
            throw RuntimeException("Failed to get broadcast schedule: ${e.message}", e)
        }
    }

    /**
     * Parses an ISO 8601 datetime string with timezone or throws a descriptive exception.
     *
     * @param value The datetime string to parse
     * @param parameterName The parameter name for error messages
     * @return Parsed OffsetDateTime
     * @throws IllegalArgumentException if the format is invalid
     */
    private fun parseIso8601OrThrow(value: String, parameterName: String): OffsetDateTime {
        return try {
            OffsetDateTime.parse(value)
        } catch (_: Exception) {
            throw IllegalArgumentException(
                "Parameter '$parameterName' must be in ISO 8601 format with timezone, e.g., 2025-12-27T00:00:00+01:00"
            )
        }
    }
}