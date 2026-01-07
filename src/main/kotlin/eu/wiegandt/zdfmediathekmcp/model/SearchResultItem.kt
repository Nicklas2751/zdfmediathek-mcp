package eu.wiegandt.zdfmediathekmcp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "__typename",
    visible = true,
    defaultImpl = UnknownItem::class
)
@JsonSubTypes(
    JsonSubTypes.Type(value = SeriesSmartCollection::class, names = ["SeasonSeriesSmartCollection", "DefaultNoSectionsSmartCollection", "MiniSeriesSmartCollection", "ISeriesSmartCollection", "DefaultWithSectionsSmartCollection", "EndlessSeriesSmartCollection"]),
    JsonSubTypes.Type(value = VideoItem::class, names = ["Video"])
)
@JsonIgnoreProperties(ignoreUnknown = true)
interface SearchResultItem {
    val title: String
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class UnknownItem(
    override val title: String = "Unknown",
    val type: String = "Unknown"
) : SearchResultItem
