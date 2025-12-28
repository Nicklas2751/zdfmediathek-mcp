package eu.wiegandt.zdfmediathekmcp.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Response wrapper for broadcast schedule queries.
 *
 * Maps to ZDF API's HAL+JSON response structure.
 *
 * @property broadcasts List of broadcasts
 * @property nextArchive Link to next page (pagination)
 */
data class ZdfBroadcastScheduleResponse(
    @field:JsonProperty("http://zdf.de/rels/cmdm/broadcasts")
    val broadcasts: List<ZdfBroadcast>,
    @field:JsonProperty("next-archive")
    val nextArchive: String?
)
