package com.educore.school.service;

import com.educore.school.dto.PageResponse;
import com.educore.school.dto.StudentRequest;
import com.educore.school.exception.DuplicateResourceException;
import com.educore.school.exception.ResourceNotFoundException;
import com.educore.school.model.Student;
import com.educore.school.repository.AttendanceRepository;
import com.educore.school.repository.StudentRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class StudentService {

    private final StudentRepository repository;
    private final AttendanceRepository attendanceRepository;
    private final MongoTemplate mongoTemplate;

    public StudentService(StudentRepository repository, AttendanceRepository attendanceRepository,
            MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.attendanceRepository = attendanceRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public PageResponse<Student> search(String search, String className, int page, int size) {
        List<Criteria> criteria = new ArrayList<>();
        if (StringUtils.hasText(search)) {
            Pattern pattern = Pattern.compile(Pattern.quote(search.trim()), Pattern.CASE_INSENSITIVE);
            criteria.add(new Criteria().orOperator(
                    Criteria.where("fullName").regex(pattern),
                    Criteria.where("admissionNumber").regex(pattern)));
        }
        if (StringUtils.hasText(className)) {
            criteria.add(Criteria.where("className").is(className.trim()));
        }
        Query query = new Query();
        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }
        long total = mongoTemplate.count(query, Student.class);

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200),
                Sort.by(Sort.Direction.DESC, "registrationDate").and(Sort.by("fullName")));
        List<Student> content = mongoTemplate.find(query.with(pageable), Student.class);
        int totalPages = (int) Math.ceil(total / (double) pageable.getPageSize());
        return new PageResponse<>(content, pageable.getPageNumber(), pageable.getPageSize(), total, totalPages);
    }

    public Student get(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
    }

    public Student create(StudentRequest request) {
        String admission = normalize(request.admissionNumber());
        if (repository.existsByAdmissionNumber(admission)) {
            throw new DuplicateResourceException("Admission number already exists: " + admission);
        }
        Student student = new Student();
        apply(student, request, admission);
        student.setRegistrationDate(request.registrationDate() != null ? request.registrationDate() : LocalDate.now());
        return repository.save(student);
    }

    public Student update(String id, StudentRequest request) {
        Student student = get(id);
        String admission = normalize(request.admissionNumber());
        if (!admission.equals(student.getAdmissionNumber()) && repository.existsByAdmissionNumber(admission)) {
            throw new DuplicateResourceException("Admission number already exists: " + admission);
        }
        apply(student, request, admission);
        if (request.registrationDate() != null) {
            student.setRegistrationDate(request.registrationDate());
        }
        return repository.save(student);
    }

    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Student not found: " + id);
        }
        attendanceRepository.deleteByStudentId(id);
        repository.deleteById(id);
    }

    private static String normalize(String admissionNumber) {
        return admissionNumber.trim().toUpperCase();
    }

    private static void apply(Student s, StudentRequest r, String admission) {
        s.setAdmissionNumber(admission);
        s.setFullName(r.fullName().trim());
        s.setEmail(r.email().trim().toLowerCase());
        s.setGender(r.gender());
        s.setClassName(r.className().trim());
        s.setAge(r.age());
    }
}
