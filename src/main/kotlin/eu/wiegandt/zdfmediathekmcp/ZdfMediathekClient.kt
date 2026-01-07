package eu.wiegandt.zdfmediathekmcp

import eu.wiegandt.zdfmediathekmcp.model.*
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange

@Service
interface ZdfMediathekClient {

    @GetExchange("/search/documents")
    fun searchDocuments(@RequestParam("q") q: String, @RequestParam("limit") limit: Int = 5): ZdfSearchResponse

    @GetExchange("/cmdm/epg/broadcasts")
    fun getBroadcastSchedule(
        @RequestParam("from") from: String,
        @RequestParam("to") to: String,
        @RequestParam("tvService") tvService: String?,
        @RequestParam("limit") limit: Int = 10
    ): ZdfBroadcastScheduleResponse

    @GetExchange("/cmdm/epg/broadcasts/pf")
    fun getCurrentBroadcastSchedule(
        @RequestParam("tvService") tvService: String?
    ): ZdfBroadcastScheduleResponse

    @GetExchange("/cmdm/brands")
    fun listBrands(
        @RequestParam("limit") limit: Int = 10
    ): BrandApiResponse

    @GetExchange("/cmdm/series")
    fun listSeries(
        @RequestParam("limit") limit: Int = 4
    ): ZdfSeriesResponse

    @GetExchange("/cmdm/seasons")
    fun listSeasons(
        @RequestParam("limit") limit: Int = 4
    ): ZdfSeasonResponse
}
