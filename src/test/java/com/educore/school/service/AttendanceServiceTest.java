package com.educore.school.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    private static final LocalDate DAY = LocalDate.of(2026, 3, 2);

    @Mock AttendanceRepository attendanceRepository;
    @Mock StudentRepository studentRepository;
    @InjectMocks AttendanceService service;

    private static Student student(String id) {
        Student s = new Student();
        s.setId(id);
        s.setFullName("Ada Obi");
        s.setAdmissionNumber("ADM-001");
        s.setClassName("Grade 7A");
        return s;
    }

    private static AttendanceRecord record(AttendanceStatus status) {
        AttendanceRecord r = new AttendanceRecord();
        r.setStatus(status);
        return r;
    }

    @Test
    void mark_createsRecordUsingStudentsClass() {
        when(studentRepository.findById("s1")).thenReturn(Optional.of(student("s1")));
        when(attendanceRepository.existsByStudentIdAndDate("s1", DAY)).thenReturn(false);
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        AttendanceRecord saved = service.mark(new AttendanceRequest("s1", DAY, AttendanceStatus.LATE));

        assertThat(saved.getClassName()).isEqualTo("Grade 7A");
        assertThat(saved.getStudentName()).isEqualTo("Ada Obi");
        assertThat(saved.getStatus()).isEqualTo(AttendanceStatus.LATE);
        assertThat(saved.getDate()).isEqualTo(DAY);
    }

    @Test
    void mark_duplicateForSameStudentAndDate_throwsConflict() {
        when(studentRepository.findById("s1")).thenReturn(Optional.of(student("s1")));
        when(attendanceRepository.existsByStudentIdAndDate("s1", DAY)).thenReturn(true);

        assertThatThrownBy(() -> service.mark(new AttendanceRequest("s1", DAY, AttendanceStatus.PRESENT)))
                .isInstanceOf(DuplicateResourceException.class);
        verify(attendanceRepository, never()).save(any(AttendanceRecord.class));
    }

    @Test
    void mark_unknownStudent_throwsNotFound() {
        when(studentRepository.findById("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.mark(new AttendanceRequest("ghost", DAY, AttendanceStatus.PRESENT)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateStatus_changesExistingRecord() {
        when(attendanceRepository.findById("a1")).thenReturn(Optional.of(record(AttendanceStatus.ABSENT)));
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        assertThat(service.updateStatus("a1", AttendanceStatus.PRESENT).getStatus())
                .isEqualTo(AttendanceStatus.PRESENT);
    }

    @Test
    void markBulk_updatesExistingAndCreatesMissingWithoutDuplicates() {
        AttendanceRecord existing = record(AttendanceStatus.ABSENT);
        existing.setId("a1");
        when(studentRepository.findById("s1")).thenReturn(Optional.of(student("s1")));
        when(studentRepository.findById("s2")).thenReturn(Optional.of(student("s2")));
        when(attendanceRepository.findByStudentIdAndDate("s1", DAY)).thenReturn(Optional.of(existing));
        when(attendanceRepository.findByStudentIdAndDate("s2", DAY)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        List<AttendanceRecord> saved = service.markBulk(new BulkAttendanceRequest(DAY, List.of(
                new BulkAttendanceRequest.Entry("s1", AttendanceStatus.PRESENT),
                new BulkAttendanceRequest.Entry("s2", AttendanceStatus.LATE))));

        assertThat(saved).hasSize(2);
        assertThat(saved.get(0).getId()).isEqualTo("a1");
        assertThat(saved.get(0).getStatus()).isEqualTo(AttendanceStatus.PRESENT);
        assertThat(saved.get(1).getStatus()).isEqualTo(AttendanceStatus.LATE);
    }

    @Test
    void stats_countsLateAsAttendedAndRoundsToOneDecimal() {
        when(attendanceRepository.findByDateAndClassName(DAY, "Grade 7A")).thenReturn(List.of(
                record(AttendanceStatus.PRESENT), record(AttendanceStatus.PRESENT),
                record(AttendanceStatus.LATE), record(AttendanceStatus.ABSENT),
                record(AttendanceStatus.PRESENT), record(AttendanceStatus.ABSENT)));

        AttendanceStats stats = service.stats(DAY, "Grade 7A", null);

        assertThat(stats.present()).isEqualTo(3);
        assertThat(stats.late()).isEqualTo(1);
        assertThat(stats.absent()).isEqualTo(2);
        assertThat(stats.total()).isEqualTo(6);
        assertThat(stats.percentage()).isEqualTo(66.7);
    }

    @Test
    void stats_withNoRecords_isZeroPercentNotNaN() {
        when(attendanceRepository.findByDate(DAY)).thenReturn(List.of());

        AttendanceStats stats = service.stats(DAY, null, null);

        assertThat(stats.total()).isZero();
        assertThat(stats.percentage()).isEqualTo(0.0);
    }

    @Test
    void stats_forStudent_usesAllOfThatStudentsRecords() {
        when(attendanceRepository.findByStudentId("s1")).thenReturn(List.of(
                record(AttendanceStatus.PRESENT), record(AttendanceStatus.ABSENT),
                record(AttendanceStatus.PRESENT), record(AttendanceStatus.PRESENT)));

        assertThat(service.stats(null, null, "s1").percentage()).isEqualTo(75.0);
    }
}
