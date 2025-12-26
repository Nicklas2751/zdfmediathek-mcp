package eu.wiegandt.zdfmediathekmcp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.OffsetDateTime


@JsonIgnoreProperties(ignoreUnknown = true)
data class ZdfDocument(
    val id: String = "",
    val externalId: String? = null,
    val title: String? = null,
    val teasertext: String? = null,
    val editorialDate: OffsetDateTime? = null,
    val contentType: String? = null,
    val hasVideo: Boolean = false,
    val webCanonical: String? = null,
    val tvService: String? = null,
    val endDate: OffsetDateTime? = null
)




