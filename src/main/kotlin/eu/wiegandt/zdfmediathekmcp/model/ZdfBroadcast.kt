package eu.wiegandt.zdfmediathekmcp.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

/**
 * Represents a single broadcast/program in the TV schedule.
 *
 * Maps to ZDF API's broadcast object structure from /cmdm/epg/broadcasts endpoint.
 *
 * @property airtimeBegin Start time with timezone
 * @property airtimeEnd End time with timezone
 * @property duration Duration in seconds
 * @property tvService Channel identifier (e.g., "ZDF")
 * @property title Program title
 * @property subtitle Episode/subtitle (optional)
 * @property text Program description
 * @property programmeItem Link to detailed programme item
 */
data class ZdfBroadcast(
    val airtimeBegin: OffsetDateTime,
    val airtimeEnd: OffsetDateTime,
    val duration: Int,
    val tvService: String,
    val title: String,
    val subtitle: String?,
    val text: String?,
    @JsonProperty("http://zdf.de/rels/cmdm/programme-item")
    val programmeItem: String?
)

