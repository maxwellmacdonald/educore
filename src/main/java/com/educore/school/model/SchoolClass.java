package com.educore.school.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "classes")
public class SchoolClass {
    @Id private String id;
    @Indexed(unique = true) private String name;
    private String level;
    private String classTeacher;
    private Integer capacity;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getLevel() { return level; }
    public void setLevel(String v) { this.level = v; }
    public String getClassTeacher() { return classTeacher; }
    public void setClassTeacher(String v) { this.classTeacher = v; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer v) { this.capacity = v; }
}
