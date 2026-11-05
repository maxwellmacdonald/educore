package com.educore.school.service;

import com.educore.school.dto.AttendanceStats;
import com.educore.school.dto.DashboardSummary;
import com.educore.school.dto.DashboardSummary.AttendanceTrendPoint;
import com.educore.school.dto.DashboardSummary.ClassEnrollment;
import com.educore.school.model.SchoolClass;
import com.educore.school.repository.AttendanceRepository;
import com.educore.school.repository.SchoolClassRepository;
import com.educore.school.repository.StudentRepository;
import com.educore.school.repository.SubjectRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private static final int TREND_DAYS = 7;

    private final StudentRepository studentRepository;
    private final SchoolClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final AttendanceRepository attendanceRepository;

    public DashboardService(StudentRepository studentRepository, SchoolClassRepository classRepository,
            SubjectRepository subjectRepository, AttendanceRepository attendanceRepository) {
        this.studentRepository = studentRepository;
        this.classRepository = classRepository;
        this.subjectRepository = subjectRepository;
        this.attendanceRepository = attendanceRepository;
    }

    public DashboardSummary summary(LocalDate requestedDate) {
        LocalDate date = requestedDate != null ? requestedDate : LocalDate.now();
        AttendanceStats today = AttendanceStats.from(attendanceRepository.findByDate(date));

        List<ClassEnrollment> enrollment = new ArrayList<>();
        for (SchoolClass c : classRepository.findAll()) {
            enrollment.add(new ClassEnrollment(c.getName(),
                    c.getCapacity() == null ? 0 : c.getCapacity(),
                    studentRepository.countByClassName(c.getName())));
        }

        List<AttendanceTrendPoint> trend = new ArrayList<>();
        for (int i = TREND_DAYS - 1; i >= 0; i--) {
            LocalDate d = date.minusDays(i);
            AttendanceStats s = AttendanceStats.from(attendanceRepository.findByDate(d));
            trend.add(new AttendanceTrendPoint(d, s.present(), s.absent(), s.late(), s.percentage()));
        }

        return new DashboardSummary(
                studentRepository.count(),
                classRepository.count(),
                subjectRepository.count(),
                date,
                today.present(),
                today.absent(),
                today.late(),
                today.percentage(),
                studentRepository.findTop5ByOrderByRegistrationDateDescIdDesc(),
                enrollment,
                trend);
    }
}
