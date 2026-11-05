package com.educore.school.dto;

import com.educore.school.model.AttendanceRecord;
import com.educore.school.model.AttendanceStatus;
import java.util.List;

/** Percentage = (present + late) / total * 100, rounded to one decimal place. 0 when there are no records. */
public record AttendanceStats(long present, long absent, long late, long total, double percentage) {

    public static AttendanceStats from(List<AttendanceRecord> records) {
        long present = records.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        long absent = records.stream().filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();
        long late = records.stream().filter(r -> r.getStatus() == AttendanceStatus.LATE).count();
        long total = present + absent + late;
        double pct = total == 0 ? 0.0 : Math.round((present + late) * 1000.0 / total) / 10.0;
        return new AttendanceStats(present, absent, late, total, pct);
    }
}
