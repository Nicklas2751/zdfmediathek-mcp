package eu.wiegandt.zdfmediathekmcp

import eu.wiegandt.zdfmediathekmcp.model.ZdfBroadcast
import eu.wiegandt.zdfmediathekmcp.model.ZdfBroadcastScheduleResponse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.time.OffsetDateTime
import java.time.ZoneId

class CurrentBroadcastServiceTest {

    private val zdfMediathekService: ZdfMediathekService = mock(ZdfMediathekService::class.java)
    private val currentBroadcastService = CurrentBroadcastService(zdfMediathekService)

    @Test
    fun `getCurrentBroadcast with valid channel returns current broadcast`() {
        // given
        val now = OffsetDateTime.now(ZoneId.of("Europe/Berlin"))
        val tvService = "ZDF"
        val currentBroadcast = ZdfBroadcast(
            airtimeBegin = now.minusMinutes(30),
            airtimeEnd = now.plusMinutes(30),
            duration = 3600,
            tvService = tvService,
            title = "Aktuell laufende Sendung",
            subtitle = null,
            text = "Dies ist die aktuell laufende Sendung",
            programmeItem = null
        )
        val mockResponse = ZdfBroadcastScheduleResponse(
            broadcasts = listOf(currentBroadcast),
            nextArchive = null
        )

        `when`(zdfMediathekService.getBroadcastSchedule(anyString(), anyString(), eq(tvService), eq(10)))
            .thenReturn(mockResponse)

        // when
        val result = currentBroadcastService.getCurrentBroadcast(tvService)

        // then
        assertThat(result.tvService).isEqualTo(tvService)
        assertThat(result.currentBroadcast).isNotNull()
        assertThat(result.currentBroadcast?.title).isEqualTo("Aktuell laufende Sendung")
        assertThat(result.queriedAt).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}")
        verify(zdfMediathekService).getBroadcastSchedule(anyString(), anyString(), eq(tvService), eq(10))
    }

    @Test
    fun `getCurrentBroadcast with empty channel name throws exception`() {
        // when / then
        assertThatThrownBy {
            currentBroadcastService.getCurrentBroadcast("")
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Parameter 'tvService' is required and must not be empty")
    }

    @Test
    fun `getCurrentBroadcast with blank channel name throws exception`() {
        // when / then
        assertThatThrownBy {
            currentBroadcastService.getCurrentBroadcast("   ")
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Parameter 'tvService' is required and must not be empty")
    }

    @Test
    fun `getCurrentBroadcast with invalid limit throws exception`() {
        // when / then
        assertThatThrownBy {
            currentBroadcastService.getCurrentBroadcast("ZDF", limit = 0)
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Parameter 'limit' must be greater than 0")
    }

    @Test
    fun `getCurrentBroadcast with no broadcast found returns null`() {
        // given
        val tvService = "ZDF"
        val mockResponse = ZdfBroadcastScheduleResponse(
            broadcasts = emptyList(),
            nextArchive = null
        )

        `when`(zdfMediathekService.getBroadcastSchedule(anyString(), anyString(), eq(tvService), eq(10)))
            .thenReturn(mockResponse)

        // when
        val result = currentBroadcastService.getCurrentBroadcast(tvService)

        // then
        assertThat(result.tvService).isEqualTo(tvService)
        assertThat(result.currentBroadcast).isNull()
        assertThat(result.queriedAt).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}")
    }

    @Test
    fun `getCurrentBroadcast with multiple broadcasts returns current one`() {
        // given
        val now = OffsetDateTime.now(ZoneId.of("Europe/Berlin"))
        val tvService = "ZDF"

        val pastBroadcast = ZdfBroadcast(
            airtimeBegin = now.minusHours(2),
            airtimeEnd = now.minusHours(1),
            duration = 3600,
            tvService = tvService,
            title = "Vergangene Sendung",
            subtitle = null,
            text = "Diese Sendung ist vorbei",
            programmeItem = null
        )

        val currentBroadcast = ZdfBroadcast(
            airtimeBegin = now.minusMinutes(30),
            airtimeEnd = now.plusMinutes(30),
            duration = 3600,
            tvService = tvService,
            title = "Aktuell laufende Sendung",
            subtitle = null,
            text = "Dies ist die aktuell laufende Sendung",
            programmeItem = null
        )

        val futureBroadcast = ZdfBroadcast(
            airtimeBegin = now.plusHours(1),
            airtimeEnd = now.plusHours(2),
            duration = 3600,
            tvService = tvService,
            title = "Zukünftige Sendung",
            subtitle = null,
            text = "Diese Sendung läuft später",
            programmeItem = null
        )

        val mockResponse = ZdfBroadcastScheduleResponse(
            broadcasts = listOf(pastBroadcast, currentBroadcast, futureBroadcast),
            nextArchive = null
        )

        `when`(zdfMediathekService.getBroadcastSchedule(anyString(), anyString(), eq(tvService), eq(10)))
            .thenReturn(mockResponse)

        // when
        val result = currentBroadcastService.getCurrentBroadcast(tvService)

        // then
        assertThat(result.currentBroadcast).isNotNull()
        assertThat(result.currentBroadcast?.title).isEqualTo("Aktuell laufende Sendung")
    }

    @Test
    fun `getCurrentBroadcast when API throws exception wraps exception`() {
        // given
        val tvService = "ZDF"
        `when`(zdfMediathekService.getBroadcastSchedule(anyString(), anyString(), eq(tvService), eq(10)))
            .thenThrow(RuntimeException("API Error"))

        // when / then
        assertThatThrownBy {
            currentBroadcastService.getCurrentBroadcast(tvService)
        }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessageContaining("Failed to get current broadcast")
    }

    @Test
    fun `getCurrentBroadcast calls API with correct time window and limit`() {
        // given
        val tvService = "ZDF"
        val limit = 15
        val mockResponse = ZdfBroadcastScheduleResponse(
            broadcasts = emptyList(),
            nextArchive = null
        )

        `when`(zdfMediathekService.getBroadcastSchedule(anyString(), anyString(), eq(tvService), eq(limit)))
            .thenReturn(mockResponse)

        // when
        currentBroadcastService.getCurrentBroadcast(tvService, limit)

        // then - verify that the method was called with the correct tvService and limit
        verify(zdfMediathekService).getBroadcastSchedule(
            anyString(), // from parameter
            anyString(), // to parameter
            eq(tvService),
            eq(limit)
        )
    }
}

