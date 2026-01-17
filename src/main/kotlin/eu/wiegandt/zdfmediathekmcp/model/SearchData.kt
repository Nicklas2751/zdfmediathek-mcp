package eu.wiegandt.zdfmediathekmcp.model

data class SearchDocumentsResult(
    val results: List<SearchResultItemWrapper>
)

data class SearchResultItemWrapper(
    val item: SearchResultItem
)
