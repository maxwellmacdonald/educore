package com.educore.school.controller;

import com.educore.school.dto.AttendanceRequest;
import com.educore.school.dto.AttendanceStats;
import com.educore.school.dto.BulkAttendanceRequest;
import com.educore.school.dto.StatusRequest;
import com.educore.school.model.AttendanceRecord;
import com.educore.school.service.AttendanceService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService service;

    public AttendanceController(AttendanceService service) {
        this.service = service;
    }

    @GetMapping
    public List<AttendanceRecord> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String className) {
        return service.list(date != null ? date : LocalDate.now(), className);
    }

    @GetMapping("/stats")
    public AttendanceStats stats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String studentId) {
        return service.stats(date, className, studentId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceRecord mark(@Valid @RequestBody AttendanceRequest request) {
        return service.mark(request);
    }

    @PostMapping("/bulk")
    public List<AttendanceRecord> markBulk(@Valid @RequestBody BulkAttendanceRequest request) {
        return service.markBulk(request);
    }

    @PutMapping("/{id}")
    public AttendanceRecord updateStatus(@PathVariable String id, @Valid @RequestBody StatusRequest request) {
        return service.updateStatus(id, request.status());
    }
}
