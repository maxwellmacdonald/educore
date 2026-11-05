package com.educore.school.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "subjects")
public class Subject {
    @Id private String id;
    private String name;
    @Indexed(unique = true) private String code;
    private String className;
    private String teacherName;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getCode() { return code; }
    public void setCode(String v) { this.code = v; }
    public String getClassName() { return className; }
    public void setClassName(String v) { this.className = v; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String v) { this.teacherName = v; }
}
