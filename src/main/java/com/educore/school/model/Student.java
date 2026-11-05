package com.educore.school.model;

import java.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "students")
public class Student {
    @Id private String id;
    @Indexed(unique = true) private String admissionNumber;
    private String fullName;
    private String email;
    private String gender;
    @Indexed private String className;
    private Integer age;
    private LocalDate registrationDate;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAdmissionNumber() { return admissionNumber; }
    public void setAdmissionNumber(String v) { this.admissionNumber = v; }
    public String getFullName() { return fullName; }
    public void setFullName(String v) { this.fullName = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getGender() { return gender; }
    public void setGender(String v) { this.gender = v; }
    public String getClassName() { return className; }
    public void setClassName(String v) { this.className = v; }
    public Integer getAge() { return age; }
    public void setAge(Integer v) { this.age = v; }
    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate v) { this.registrationDate = v; }
}
