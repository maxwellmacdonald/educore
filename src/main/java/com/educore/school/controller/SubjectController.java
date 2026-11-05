package com.educore.school.controller;

import com.educore.school.dto.SubjectRequest;
import com.educore.school.model.Subject;
import com.educore.school.service.SubjectService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService service;

    public SubjectController(SubjectService service) {
        this.service = service;
    }

    @GetMapping
    public List<Subject> list(@RequestParam(required = false) String className) {
        return service.list(className);
    }

    @GetMapping("/{id}")
    public Subject get(@PathVariable String id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Subject create(@Valid @RequestBody SubjectRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public Subject update(@PathVariable String id, @Valid @RequestBody SubjectRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
