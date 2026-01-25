package eu.wiegandt.zdfmediathekmcp.mcp.pagination

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

object McpPaginationPayloadHandler {
    private val objectMapper = jacksonObjectMapper()

    fun encode(page: Int, limit: Int?): String {
        val payload = CursorPayload(page = page, limit = limit)
        val json = objectMapper.writeValueAsString(payload)
        return java.util.Base64.getEncoder().encodeToString(json.toByteArray())
    }

    @Throws(IllegalArgumentException::class)
    fun decode(cursor: String): CursorPayload {
        try {
            val decoded = String(java.util.Base64.getDecoder().decode(cursor))
            return objectMapper.readValue(decoded)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid cursor", e)
        }
    }
}
