package com.educore.school.service;

import com.educore.school.dto.AttendanceRequest;
import com.educore.school.dto.AttendanceStats;
import com.educore.school.dto.BulkAttendanceRequest;
import com.educore.school.exception.DuplicateResourceException;
import com.educore.school.exception.ResourceNotFoundException;
import com.educore.school.model.AttendanceRecord;
import com.educore.school.model.AttendanceStatus;
import com.educore.school.model.Student;
import com.educore.school.repository.AttendanceRepository;
import com.educore.school.repository.StudentRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    public AttendanceService(AttendanceRepository attendanceRepository, StudentRepository studentRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
    }

    /** Creates one entry; rejects a second entry for the same student and date. */
    public AttendanceRecord mark(AttendanceRequest request) {
        Student student = findStudent(request.studentId());
        if (attendanceRepository.existsByStudentIdAndDate(student.getId(), request.date())) {
            throw new DuplicateResourceException(
                    "Attendance already recorded for " + student.getFullName() + " on " + request.date());
        }
        return attendanceRepository.save(toRecord(student, request.date(), request.status()));
    }

    public AttendanceRecord updateStatus(String id, AttendanceStatus status) {
        AttendanceRecord record = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found: " + id));
        record.setStatus(status);
        return attendanceRepository.save(record);
    }

    /** Saves a whole roster: creates missing entries and updates existing ones (never duplicates). */
    public List<AttendanceRecord> markBulk(BulkAttendanceRequest request) {
        List<AttendanceRecord> saved = new ArrayList<>();
        for (BulkAttendanceRequest.Entry entry : request.entries()) {
            Student student = findStudent(entry.studentId());
            AttendanceRecord record = attendanceRepository
                    .findByStudentIdAndDate(student.getId(), request.date())
                    .orElseGet(() -> toRecord(student, request.date(), entry.status()));
            record.setStatus(entry.status());
            saved.add(attendanceRepository.save(record));
        }
        return saved;
    }

    public List<AttendanceRecord> list(LocalDate date, String className) {
        if (StringUtils.hasText(className)) {
            return attendanceRepository.findByDateAndClassName(date, className.trim());
        }
        return attendanceRepository.findByDate(date);
    }

    /** Stats for one student (all dates), or for a date (optionally narrowed to a class). */
    public AttendanceStats stats(LocalDate date, String className, String studentId) {
        if (StringUtils.hasText(studentId)) {
            return AttendanceStats.from(attendanceRepository.findByStudentId(studentId));
        }
        return AttendanceStats.from(list(date != null ? date : LocalDate.now(), className));
    }

    private Student findStudent(String id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
    }

    private static AttendanceRecord toRecord(Student student, LocalDate date, AttendanceStatus status) {
        AttendanceRecord r = new AttendanceRecord();
        r.setStudentId(student.getId());
        r.setStudentName(student.getFullName());
        r.setAdmissionNumber(student.getAdmissionNumber());
        r.setClassName(student.getClassName());
        r.setDate(date);
        r.setStatus(status);
        return r;
    }
}
