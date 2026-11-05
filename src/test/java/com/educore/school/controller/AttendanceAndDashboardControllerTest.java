package com.educore.school.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.educore.school.dto.AttendanceRequest;
import com.educore.school.dto.AttendanceStats;
import com.educore.school.dto.DashboardSummary;
import com.educore.school.exception.DuplicateResourceException;
import com.educore.school.model.AttendanceRecord;
import com.educore.school.model.AttendanceStatus;
import com.educore.school.service.AttendanceService;
import com.educore.school.service.DashboardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({AttendanceController.class, DashboardController.class})
class AttendanceAndDashboardControllerTest {

    private static final LocalDate DAY = LocalDate.of(2026, 3, 2);

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockBean AttendanceService attendanceService;
    @MockBean DashboardService dashboardService;

    @Test
    void mark_returns201() throws Exception {
        AttendanceRecord saved = new AttendanceRecord();
        saved.setId("a1");
        saved.setStatus(AttendanceStatus.PRESENT);
        when(attendanceService.mark(any(AttendanceRequest.class))).thenReturn(saved);

        mvc.perform(post("/api/attendance").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new AttendanceRequest("s1", DAY, AttendanceStatus.PRESENT))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PRESENT"));
    }

    @Test
    void mark_duplicate_returns409() throws Exception {
        when(attendanceService.mark(any(AttendanceRequest.class)))
                .thenThrow(new DuplicateResourceException("Attendance already recorded"));

        mvc.perform(post("/api/attendance").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new AttendanceRequest("s1", DAY, AttendanceStatus.PRESENT))))
                .andExpect(status().isConflict());
    }

    @Test
    void mark_unknownStatus_returns400() throws Exception {
        String body = "{\"studentId\":\"s1\",\"date\":\"2026-03-02\",\"status\":\"SLEEPING\"}";

        mvc.perform(post("/api/attendance").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void stats_returnsPercentage() throws Exception {
        when(attendanceService.stats(any(), any(), any())).thenReturn(new AttendanceStats(3, 1, 0, 4, 75.0));

        mvc.perform(get("/api/attendance/stats").param("date", "2026-03-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.percentage").value(75.0))
                .andExpect(jsonPath("$.total").value(4));
    }

    @Test
    void dashboardSummary_returns200() throws Exception {
        when(dashboardService.summary(any())).thenReturn(new DashboardSummary(
                20, 4, 6, DAY, 15, 3, 2, 85.0, List.of(), List.of(), List.of()));

        mvc.perform(get("/api/dashboard/summary").param("date", "2026-03-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudents").value(20))
                .andExpect(jsonPath("$.attendancePercentage").value(85.0));
    }
}
