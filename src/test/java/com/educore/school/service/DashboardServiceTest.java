package com.educore.school.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.educore.school.dto.DashboardSummary;
import com.educore.school.model.AttendanceRecord;
import com.educore.school.model.AttendanceStatus;
import com.educore.school.model.SchoolClass;
import com.educore.school.model.Student;
import com.educore.school.repository.AttendanceRepository;
import com.educore.school.repository.SchoolClassRepository;
import com.educore.school.repository.StudentRepository;
import com.educore.school.repository.SubjectRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    private static final LocalDate DAY = LocalDate.of(2026, 3, 2);

    @Mock StudentRepository studentRepository;
    @Mock SchoolClassRepository classRepository;
    @Mock SubjectRepository subjectRepository;
    @Mock AttendanceRepository attendanceRepository;
    @InjectMocks DashboardService service;

    private static AttendanceRecord record(AttendanceStatus status) {
        AttendanceRecord r = new AttendanceRecord();
        r.setStatus(status);
        return r;
    }

    @Test
    void summary_aggregatesRealRepositoryData() {
        SchoolClass grade7 = new SchoolClass();
        grade7.setName("Grade 7A");
        grade7.setCapacity(30);
        Student recent = new Student();
        recent.setFullName("Ada Obi");

        when(studentRepository.count()).thenReturn(20L);
        when(classRepository.count()).thenReturn(4L);
        when(subjectRepository.count()).thenReturn(6L);
        when(classRepository.findAll()).thenReturn(List.of(grade7));
        when(studentRepository.countByClassName("Grade 7A")).thenReturn(18L);
        when(studentRepository.findTop5ByOrderByRegistrationDateDescIdDesc()).thenReturn(List.of(recent));
        when(attendanceRepository.findByDate(DAY)).thenReturn(List.of(
                record(AttendanceStatus.PRESENT), record(AttendanceStatus.PRESENT),
                record(AttendanceStatus.PRESENT), record(AttendanceStatus.ABSENT)));

        DashboardSummary summary = service.summary(DAY);

        assertThat(summary.totalStudents()).isEqualTo(20);
        assertThat(summary.totalClasses()).isEqualTo(4);
        assertThat(summary.totalSubjects()).isEqualTo(6);
        assertThat(summary.presentCount()).isEqualTo(3);
        assertThat(summary.absentCount()).isEqualTo(1);
        assertThat(summary.attendancePercentage()).isEqualTo(75.0);
        assertThat(summary.recentStudents()).hasSize(1);
        assertThat(summary.enrollmentByClass()).singleElement().satisfies(e -> {
            assertThat(e.className()).isEqualTo("Grade 7A");
            assertThat(e.students()).isEqualTo(18);
            assertThat(e.capacity()).isEqualTo(30);
        });
        assertThat(summary.attendanceTrend()).hasSize(7);
        assertThat(summary.attendanceTrend().get(6).date()).isEqualTo(DAY);
    }
}
