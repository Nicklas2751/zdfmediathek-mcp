package eu.wiegandt.zdfmediathekmcp

import eu.wiegandt.zdfmediathekmcp.model.BrandApiResponse
import eu.wiegandt.zdfmediathekmcp.model.BrandSummary
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.*
import org.springframework.web.reactive.function.client.WebClientResponseException

class SearchBrandsServiceTest {
    private val zdfMediathekService = mock(ZdfMediathekService::class.java)
    private val searchBrandsService = SearchBrandsService(zdfMediathekService)

    @Test
    fun `listBrands returns expected brands`() {
        // Given
        val brands = listOf(
            BrandSummary("id1", "Terra X", "Doku-Reihe"),
            BrandSummary("id2", "Tatort", "Krimi")
        )
        given(zdfMediathekService.listBrands(10)).willReturn(BrandApiResponse(brands))
        // When
        val result = searchBrandsService.listBrands()
        // Then
        assertThat(result).usingRecursiveComparison().isEqualTo(brands)
    }

    @Test
    fun `listBrands with empty result returns empty list`() {
        // Given
        given(zdfMediathekService.listBrands(10)).willReturn(BrandApiResponse(emptyList()))
        // When
        val result = searchBrandsService.listBrands()
        // Then
        assertThat(result).usingRecursiveComparison().isEqualTo(emptyList<BrandSummary>())
    }

    @Test
    fun `listBrands with limit parameter passes limit`() {
        // Given
        given(zdfMediathekService.listBrands(5)).willReturn(BrandApiResponse(emptyList()))
        // When
        searchBrandsService.listBrands(5)
        // Then
        verify(zdfMediathekService).listBrands(5)
    }

    @Test
    fun `listBrands throws on API error`() {
        // Given
        doThrow(
            WebClientResponseException(
                500,
                "Server Error",
                null,
                null,
                null
            )
        ).`when`(zdfMediathekService).listBrands(10)
        // When/Then
        assertThatThrownBy { searchBrandsService.listBrands() }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("Failed to list brands: 500 Server Error")
    }
}
