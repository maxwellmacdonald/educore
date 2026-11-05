package com.educore.school.service;

import com.educore.school.dto.ClassRequest;
import com.educore.school.exception.DuplicateResourceException;
import com.educore.school.exception.ResourceNotFoundException;
import com.educore.school.model.SchoolClass;
import com.educore.school.repository.SchoolClassRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ClassService {

    private final SchoolClassRepository repository;

    public ClassService(SchoolClassRepository repository) {
        this.repository = repository;
    }

    public List<SchoolClass> list() {
        return repository.findAll(Sort.by("name"));
    }

    public SchoolClass get(String id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Class not found: " + id));
    }

    public SchoolClass create(ClassRequest request) {
        String name = request.name().trim();
        if (repository.findByNameIgnoreCase(name).isPresent()) {
            throw new DuplicateResourceException("Class name already exists: " + name);
        }
        SchoolClass c = new SchoolClass();
        apply(c, request, name);
        return repository.save(c);
    }

    public SchoolClass update(String id, ClassRequest request) {
        SchoolClass c = get(id);
        String name = request.name().trim();
        boolean taken = repository.findByNameIgnoreCase(name).filter(other -> !other.getId().equals(id)).isPresent();
        if (taken) {
            throw new DuplicateResourceException("Class name already exists: " + name);
        }
        apply(c, request, name);
        return repository.save(c);
    }

    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Class not found: " + id);
        }
        repository.deleteById(id);
    }

    private static void apply(SchoolClass c, ClassRequest r, String name) {
        c.setName(name);
        c.setLevel(r.level().trim());
        c.setClassTeacher(r.classTeacher().trim());
        c.setCapacity(r.capacity());
    }
}
