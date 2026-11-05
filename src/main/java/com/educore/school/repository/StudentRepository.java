package com.educore.school.repository;

import com.educore.school.model.Student;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StudentRepository extends MongoRepository<Student, String> {
    boolean existsByAdmissionNumber(String admissionNumber);
    long countByClassName(String className);
    List<Student> findTop5ByOrderByRegistrationDateDescIdDesc();
}
