package com.educore.school.service;

import com.educore.school.dto.SubjectRequest;
import com.educore.school.exception.DuplicateResourceException;
import com.educore.school.exception.ResourceNotFoundException;
import com.educore.school.model.Subject;
import com.educore.school.repository.SubjectRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SubjectService {

    private final SubjectRepository repository;

    public SubjectService(SubjectRepository repository) {
        this.repository = repository;
    }

    public List<Subject> list(String className) {
        if (StringUtils.hasText(className)) {
            return repository.findByClassNameOrderByNameAsc(className.trim());
        }
        return repository.findAll(Sort.by("name"));
    }

    public Subject get(String id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + id));
    }

    public Subject create(SubjectRequest request) {
        String code = normalize(request.code());
        if (repository.findByCode(code).isPresent()) {
            throw new DuplicateResourceException("Subject code already exists: " + code);
        }
        Subject s = new Subject();
        apply(s, request, code);
        return repository.save(s);
    }

    public Subject update(String id, SubjectRequest request) {
        Subject s = get(id);
        String code = normalize(request.code());
        boolean taken = repository.findByCode(code).filter(other -> !other.getId().equals(id)).isPresent();
        if (taken) {
            throw new DuplicateResourceException("Subject code already exists: " + code);
        }
        apply(s, request, code);
        return repository.save(s);
    }

    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Subject not found: " + id);
        }
        repository.deleteById(id);
    }

    private static String normalize(String code) {
        return code.trim().toUpperCase();
    }

    private static void apply(Subject s, SubjectRequest r, String code) {
        s.setName(r.name().trim());
        s.setCode(code);
        s.setClassName(r.className().trim());
        s.setTeacherName(r.teacherName().trim());
    }
}
