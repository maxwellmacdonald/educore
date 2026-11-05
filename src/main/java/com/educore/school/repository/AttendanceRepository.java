package com.educore.school.repository;

import com.educore.school.model.AttendanceRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AttendanceRepository extends MongoRepository<AttendanceRecord, String> {
    boolean existsByStudentIdAndDate(String studentId, LocalDate date);
    Optional<AttendanceRecord> findByStudentIdAndDate(String studentId, LocalDate date);
    List<AttendanceRecord> findByDate(LocalDate date);
    List<AttendanceRecord> findByDateAndClassName(LocalDate date, String className);
    List<AttendanceRecord> findByStudentId(String studentId);
    void deleteByStudentId(String studentId);
}
