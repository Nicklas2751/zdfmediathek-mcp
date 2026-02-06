package eu.wiegandt.zdfmediathekmcp.mcp.tools

import eu.wiegandt.zdfmediathekmcp.ZdfMediathekClient
import eu.wiegandt.zdfmediathekmcp.mcp.pagination.McpPaginationPayloadHandler
import eu.wiegandt.zdfmediathekmcp.model.BrandApiResponse
import eu.wiegandt.zdfmediathekmcp.model.BrandSummary
import eu.wiegandt.zdfmediathekmcp.model.McpPagedResult
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.springframework.web.reactive.function.client.WebClientResponseException

class ListBrandsServiceTest {
    private val zdfMediathekClient = Mockito.mock(ZdfMediathekClient::class.java)
    private val listBrandsService = ListBrandsService(zdfMediathekClient)

    @Test
    fun `listBrands returns expected brands`() {
        // given
        val brands = BrandApiResponse(
            listOf(
                BrandSummary("id1", "Terra X", "Doku-Reihe"),
                BrandSummary("id2", "Tatort", "Krimi")
            )
        )
        doReturn(brands).`when`(zdfMediathekClient).listBrands(10, 1)

        // when
        val result: McpPagedResult<BrandSummary> = listBrandsService.listBrands()

        // then
        assertThat(result.resources).usingRecursiveComparison().isEqualTo(brands.brands)
        assertThat(result.nextCursor).isNull()
    }

    @Test
    fun `listBrands with empty result returns empty list`() {
        // given
        doReturn(BrandApiResponse()).`when`(zdfMediathekClient).listBrands(10, 1)

        // when
        val result: McpPagedResult<BrandSummary> = listBrandsService.listBrands()

        // then
        assertThat(result.resources).isEmpty()
    }

    @Test
    fun `listBrands with limit parameter passes limit`() {
        // given
        doReturn(BrandApiResponse()).`when`(zdfMediathekClient).listBrands(5, 1)

        // when
        listBrandsService.listBrands(5)

        // then
        verify(zdfMediathekClient).listBrands(5, 1)
    }

    @Test
    fun `listBrands throws on API error`() {
        // given
        doThrow(
            WebClientResponseException(
                500,
                "Server Error",
                null,
                null,
                null
            )
        ).`when`(zdfMediathekClient).listBrands(10, 1)

        // when/then
        assertThatThrownBy { listBrandsService.listBrands() }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("Failed to list brands: 500 Server Error")
    }

    @Test
    fun `listBrands with cursor decodes and calls client with correct page`() {
        // given: cursor encodes page=2 and limit=5
        val cursor = McpPaginationPayloadHandler.encode(2, 5)
        doReturn(BrandApiResponse()).`when`(zdfMediathekClient).listBrands(5, 2)

        // when
        listBrandsService.listBrands(null, cursor)

        // then
        verify(zdfMediathekClient).listBrands(5, 2)
    }

    @Test
    fun `listBrands with invalid cursor throws IllegalArgumentException`() {
        // given
        val invalidCursor = "not-base64"

        // when/then
        assertThatThrownBy { listBrandsService.listBrands(null, invalidCursor) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Invalid cursor")
    }
}