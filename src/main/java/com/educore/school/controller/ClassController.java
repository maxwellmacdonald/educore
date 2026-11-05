package com.educore.school.controller;

import com.educore.school.dto.ClassRequest;
import com.educore.school.model.SchoolClass;
import com.educore.school.service.ClassService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/classes")
public class ClassController {

    private final ClassService service;

    public ClassController(ClassService service) {
        this.service = service;
    }

    @GetMapping
    public List<SchoolClass> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public SchoolClass get(@PathVariable String id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SchoolClass create(@Valid @RequestBody ClassRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public SchoolClass update(@PathVariable String id, @Valid @RequestBody ClassRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
