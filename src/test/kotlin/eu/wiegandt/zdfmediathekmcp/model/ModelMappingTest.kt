package eu.wiegandt.zdfmediathekmcp.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ModelMappingTest {

    private val objectMapper = ObjectMapper().findAndRegisterModules()

    @Test
    fun `unknown typename maps to UnknownItem`() {
        // given
        val json = """{ "__typename": "WhateverType", "title": "Mystery", "type": "X" }"""
        val expected = UnknownItem(title = "Mystery", type = "X")

        // when
        val item: SearchResultItem = objectMapper.readValue(json)

        // then
        assertThat(item).usingRecursiveComparison().isEqualTo(expected)
    }

    @Test
    fun `video typename maps to VideoItem with episodeInfo`() {
        // given
        val json = """
            {
              "__typename": "Video",
              "title": "Sample Video",
              "episodeInfo": { "seasonNumber": 1, "episodeNumber": 2 },
              "editorialDate": "2025-01-01T00:00:00Z",
              "sharingUrl": "https://zdf.de/video/1"
            }
        """
        val expected = VideoItem(
            title = "Sample Video",
            editorialDate = "2025-01-01T00:00:00Z",
            sharingUrl = "https://zdf.de/video/1",
            episodeInfo = EpisodeInfo(1, 2)
        )

        // when
        val item: SearchResultItem = objectMapper.readValue(json)

        // then
        assertThat(item).usingRecursiveComparison().isEqualTo(expected)
    }

}
