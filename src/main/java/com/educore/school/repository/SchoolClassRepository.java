package com.educore.school.repository;

import com.educore.school.model.SchoolClass;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SchoolClassRepository extends MongoRepository<SchoolClass, String> {
    Optional<SchoolClass> findByNameIgnoreCase(String name);
}
