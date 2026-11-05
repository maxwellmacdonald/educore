package com.educore.school.model;

import java.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "attendance")
@CompoundIndex(name = "student_date_unique", def = "{'studentId': 1, 'date': 1}", unique = true)
public class AttendanceRecord {
    @Id private String id;
    private String studentId;
    private String studentName;
    private String admissionNumber;
    @Indexed private String className;
    @Indexed private LocalDate date;
    private AttendanceStatus status;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String v) { this.studentId = v; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String v) { this.studentName = v; }
    public String getAdmissionNumber() { return admissionNumber; }
    public void setAdmissionNumber(String v) { this.admissionNumber = v; }
    public String getClassName() { return className; }
    public void setClassName(String v) { this.className = v; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate v) { this.date = v; }
    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus v) { this.status = v; }
}
