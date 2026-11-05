package com.educore.school.repository;

import com.educore.school.model.Subject;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SubjectRepository extends MongoRepository<Subject, String> {
    Optional<Subject> findByCode(String code);
    List<Subject> findByClassNameOrderByNameAsc(String className);
}
