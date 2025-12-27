package eu.wiegandt.zdfmediathekmcp

import eu.wiegandt.zdfmediathekmcp.model.ZdfBroadcast
import eu.wiegandt.zdfmediathekmcp.model.ZdfBroadcastScheduleResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.time.OffsetDateTime

class BroadcastScheduleServiceTest {

    private val zdfMediathekService: ZdfMediathekService = mock(ZdfMediathekService::class.java)
    private val broadcastScheduleService = BroadcastScheduleService(zdfMediathekService)

    @Test
    fun `getBroadcastSchedule with valid parameters returns results`() {
        // given
        val from = "2025-12-27T00:00:00+01:00"
        val to = "2025-12-27T23:59:59+01:00"
        val tvService = "ZDF"
        val mockResponse = ZdfBroadcastScheduleResponse(
            broadcasts = listOf(
                ZdfBroadcast(
                    airtimeBegin = OffsetDateTime.parse("2025-12-27T20:15:00+01:00"),
                    airtimeEnd = OffsetDateTime.parse("2025-12-27T21:45:00+01:00"),
                    duration = 5400,
                    tvService = "ZDF",
                    title = "Test Program",
                    subtitle = null,
                    text = "Test Description",
                    programmeItem = null
                )
            ),
            nextArchive = null
        )

        `when`(zdfMediathekService.getBroadcastSchedule(from, to, tvService)).thenReturn(mockResponse)

        // when
        val result = broadcastScheduleService.getBroadcastSchedule(from, to, tvService)

        // then
        assertThat(result).isEqualTo(mockResponse)
        verify(zdfMediathekService).getBroadcastSchedule(from, to, tvService)
    }

    @Test
    fun `getBroadcastSchedule with blank from throws exception`() {
        // when / then
        val exception = assertThrows<IllegalArgumentException> {
            broadcastScheduleService.getBroadcastSchedule("", "2025-12-27T23:59:59+01:00", "ZDF")
        }

        assertThat(exception.message).isEqualTo("Parameter 'from' is required and must not be empty")
    }

    @Test
    fun `getBroadcastSchedule with blank to throws exception`() {
        // when / then
        val exception = assertThrows<IllegalArgumentException> {
            broadcastScheduleService.getBroadcastSchedule("2025-12-27T00:00:00+01:00", "", "ZDF")
        }

        assertThat(exception.message).isEqualTo("Parameter 'to' is required and must not be empty")
    }

    @Test
    fun `getBroadcastSchedule with invalid from format throws exception`() {
        // when / then
        val exception = assertThrows<IllegalArgumentException> {
            broadcastScheduleService.getBroadcastSchedule("2025-12-27", "2025-12-27T23:59:59+01:00", "ZDF")
        }

        assertThat(exception.message).isEqualTo("Parameter 'from' must be in ISO 8601 format with timezone, e.g., 2025-12-27T00:00:00+01:00")
    }

    @Test
    fun `getBroadcastSchedule with invalid to format throws exception`() {
        // when / then
        val exception = assertThrows<IllegalArgumentException> {
            broadcastScheduleService.getBroadcastSchedule("2025-12-27T00:00:00+01:00", "2025-12-27", "ZDF")
        }

        assertThat(exception.message).isEqualTo("Parameter 'to' must be in ISO 8601 format with timezone, e.g., 2025-12-27T00:00:00+01:00")
    }

    @Test
    fun `getBroadcastSchedule with null tvService passes null to service`() {
        // given
        val from = "2025-12-27T00:00:00+01:00"
        val to = "2025-12-27T23:59:59+01:00"
        val mockResponse = ZdfBroadcastScheduleResponse(broadcasts = emptyList(), nextArchive = null)

        `when`(zdfMediathekService.getBroadcastSchedule(from, to, null)).thenReturn(mockResponse)

        // when
        val result = broadcastScheduleService.getBroadcastSchedule(from, to, null)

        // then
        assertThat(result).isEqualTo(mockResponse)
        verify(zdfMediathekService).getBroadcastSchedule(from, to, null)
    }

    @Test
    fun `getBroadcastSchedule with from after to throws exception`() {
        // when / then
        val exception = assertThrows<IllegalArgumentException> {
            broadcastScheduleService.getBroadcastSchedule(
                "2025-12-27T23:59:59+01:00",
                "2025-12-27T00:00:00+01:00",
                "ZDF"
            )
        }
        
        assertThat(exception.message).isEqualTo("Parameter 'from' must be before 'to'")
    }
}

