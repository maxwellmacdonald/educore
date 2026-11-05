package com.educore.school.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.educore.school.dto.ClassRequest;
import com.educore.school.dto.SubjectRequest;
import com.educore.school.exception.DuplicateResourceException;
import com.educore.school.model.SchoolClass;
import com.educore.school.service.ClassService;
import com.educore.school.service.SubjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({ClassController.class, SubjectController.class})
class ClassAndSubjectControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockBean ClassService classService;
    @MockBean SubjectService subjectService;

    @Test
    void createClass_returns201() throws Exception {
        SchoolClass saved = new SchoolClass();
        saved.setId("c1");
        saved.setName("Grade 7A");
        when(classService.create(any(ClassRequest.class))).thenReturn(saved);

        mvc.perform(post("/api/classes").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ClassRequest("Grade 7A", "Grade 7", "Mrs. A", 30))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Grade 7A"));
    }

    @Test
    void createClass_invalidCapacity_returns400() throws Exception {
        mvc.perform(post("/api/classes").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ClassRequest("Grade 7A", "Grade 7", "Mrs. A", 0))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.capacity").value("Capacity must be at least 1"));
    }

    @Test
    void createClass_duplicateName_returns409() throws Exception {
        when(classService.create(any(ClassRequest.class)))
                .thenThrow(new DuplicateResourceException("Class name already exists: Grade 7A"));

        mvc.perform(post("/api/classes").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ClassRequest("Grade 7A", "Grade 7", "Mrs. A", 30))))
                .andExpect(status().isConflict());
    }

    @Test
    void createSubject_duplicateCode_returns409() throws Exception {
        when(subjectService.create(any(SubjectRequest.class)))
                .thenThrow(new DuplicateResourceException("Subject code already exists: MTH7"));

        mvc.perform(post("/api/subjects").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new SubjectRequest("Mathematics", "MTH7", "Grade 7A", "Mr. Lawal"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Subject code already exists: MTH7"));
    }

    @Test
    void createSubject_missingFields_returns400() throws Exception {
        mvc.perform(post("/api/subjects").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new SubjectRequest("", "", "Grade 7A", "Mr. Lawal"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.code").value("Subject code is required"));
    }
}
