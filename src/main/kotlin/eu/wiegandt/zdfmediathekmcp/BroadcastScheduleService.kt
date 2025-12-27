package eu.wiegandt.zdfmediathekmcp

import eu.wiegandt.zdfmediathekmcp.model.ZdfBroadcastScheduleResponse
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
    private val zdfMediathekService: ZdfMediathekService
) {

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
        description = "Get the TV broadcast schedule for ZDF channels within a specific time range. " +
                "Parameters: " +
                "- from: Start time in ISO 8601 format with timezone (e.g., 2025-12-27T00:00:00+01:00) " +
                "- to: End time in ISO 8601 format with timezone (e.g., 2025-12-27T23:59:59+01:00) " +
                "- tvService: Optional channel name (e.g., ZDF, ZDFneo, 3sat). If omitted, returns all channels. " +
                "Common channels: ZDF, ZDFneo, ZDFinfo, 3sat, PHOENIX, KIKA. " +
                "Timezone: Use +01:00 (CET) or +02:00 (CEST) for German time. " +
                "Returns a list of programs with title, time, description, and channel info."
    )
    fun getBroadcastSchedule(
        from: String,
        to: String,
        tvService: String? = null
    ): ZdfBroadcastScheduleResponse {
        // Validate required parameters
        require(from.isNotBlank()) {
            "Parameter 'from' is required and must not be empty"
        }
        require(to.isNotBlank()) {
            "Parameter 'to' is required and must not be empty"
        }

        // Validate ISO 8601 format with timezone
        val fromDateTime = parseIso8601OrThrow(from, "from")
        val toDateTime = parseIso8601OrThrow(to, "to")

        // Validate time range
        require(fromDateTime.isBefore(toDateTime)) {
            "Parameter 'from' must be before 'to'"
        }

        // Delegate to ZDF API service
        return zdfMediathekService.getBroadcastSchedule(from, to, tvService)
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

