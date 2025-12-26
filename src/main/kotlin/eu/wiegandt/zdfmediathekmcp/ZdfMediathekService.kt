package eu.wiegandt.zdfmediathekmcp

import eu.wiegandt.zdfmediathekmcp.model.ZdfSearchResponse
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange

@Service
interface ZdfMediathekService {

    @GetExchange("/search/documents")
    fun searchDocuments(@RequestParam("q") q: String, @RequestParam("limit") limit: Int = 10): ZdfSearchResponse

}