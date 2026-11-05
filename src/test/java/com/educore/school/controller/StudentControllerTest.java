package com.educore.school.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.educore.school.dto.PageResponse;
import com.educore.school.dto.StudentRequest;
import com.educore.school.exception.DuplicateResourceException;
import com.educore.school.exception.ResourceNotFoundException;
import com.educore.school.model.Student;
import com.educore.school.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockBean StudentService service;

    private static StudentRequest validRequest() {
        return new StudentRequest("ADM-001", "Ada Obi", "ada@school.test", "Female", "Grade 7A", 12, null);
    }

    private static Student student() {
        Student s = new Student();
        s.setId("1");
        s.setAdmissionNumber("ADM-001");
        s.setFullName("Ada Obi");
        return s;
    }

    @Test
    void create_validPayload_returns201() throws Exception {
        when(service.create(any(StudentRequest.class))).thenReturn(student());

        mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.fullName").value("Ada Obi"));
    }

    @Test
    void create_invalidPayload_returns400WithFieldErrors() throws Exception {
        StudentRequest bad = new StudentRequest("ADM-002", "", "not-an-email", "Male", "Grade 7A", 2, null);

        mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.fullName").value("Full name is required"))
                .andExpect(jsonPath("$.fieldErrors.email").value("Email must be a valid address"))
                .andExpect(jsonPath("$.fieldErrors.age").value("Age must be at least 3"));
    }

    @Test
    void create_malformedJson_returns400() throws Exception {
        mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_duplicateAdmissionNumber_returns409() throws Exception {
        when(service.create(any(StudentRequest.class)))
                .thenThrow(new DuplicateResourceException("Admission number already exists: ADM-001"));

        mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Admission number already exists: ADM-001"));
    }

    @Test
    void get_missingStudent_returns404() throws Exception {
        when(service.get("nope")).thenThrow(new ResourceNotFoundException("Student not found: nope"));

        mvc.perform(get("/api/students/nope"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void update_validPayload_returns200() throws Exception {
        when(service.update(eq("1"), any(StudentRequest.class))).thenReturn(student());

        mvc.perform(put("/api/students/1").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.admissionNumber").value("ADM-001"));
    }

    @Test
    void delete_existingStudent_returns204() throws Exception {
        mvc.perform(delete("/api/students/1")).andExpect(status().isNoContent());
    }

    @Test
    void delete_missingStudent_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Student not found: nope")).when(service).delete("nope");

        mvc.perform(delete("/api/students/nope")).andExpect(status().isNotFound());
    }

    @Test
    void list_returnsPagedResponse() throws Exception {
        when(service.search(any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>(List.of(student()), 0, 10, 1, 1));

        mvc.perform(get("/api/students").param("search", "ada").param("className", "Grade 7A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fullName").value("Ada Obi"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
