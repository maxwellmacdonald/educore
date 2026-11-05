package com.educore.school.dto;

import com.educore.school.model.Student;
import java.time.LocalDate;
import java.util.List;

public record DashboardSummary(
        long totalStudents,
        long totalClasses,
        long totalSubjects,
        LocalDate date,
        long presentCount,
        long absentCount,
        long lateCount,
        double attendancePercentage,
        List<Student> recentStudents,
        List<ClassEnrollment> enrollmentByClass,
        List<AttendanceTrendPoint> attendanceTrend) {

    public record ClassEnrollment(String className, int capacity, long students) { }

    public record AttendanceTrendPoint(LocalDate date, long present, long absent, long late, double percentage) { }
}
