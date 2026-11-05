package com.educore.school.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.educore.school.dto.PageResponse;
import com.educore.school.dto.StudentRequest;
import com.educore.school.exception.DuplicateResourceException;
import com.educore.school.exception.ResourceNotFoundException;
import com.educore.school.model.Student;
import com.educore.school.repository.AttendanceRepository;
import com.educore.school.repository.StudentRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock StudentRepository repository;
    @Mock AttendanceRepository attendanceRepository;
    @Mock MongoTemplate mongoTemplate;
    @InjectMocks StudentService service;

    private static StudentRequest request(String admission) {
        return new StudentRequest(admission, "Ada Obi", "Ada@School.test", "Female", "Grade 7A", 12, null);
    }

    private static Student existing() {
        Student s = new Student();
        s.setId("1");
        s.setAdmissionNumber("ADM-001");
        s.setFullName("Old Name");
        s.setEmail("old@school.test");
        s.setGender("Female");
        s.setClassName("Grade 7A");
        s.setAge(11);
        s.setRegistrationDate(LocalDate.of(2025, 1, 10));
        return s;
    }

    @Test
    void create_normalizesAdmissionNumberAndDefaultsRegistrationDate() {
        when(repository.existsByAdmissionNumber("ADM-001")).thenReturn(false);
        when(repository.save(any(Student.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        Student created = service.create(request(" adm-001 "));

        assertThat(created.getAdmissionNumber()).isEqualTo("ADM-001");
        assertThat(created.getEmail()).isEqualTo("ada@school.test");
        assertThat(created.getRegistrationDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void create_duplicateAdmissionNumber_throwsAndDoesNotSave() {
        when(repository.existsByAdmissionNumber("ADM-001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("ADM-001")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ADM-001");
        verify(repository, never()).save(any(Student.class));
    }

    @Test
    void get_returnsStudent() {
        when(repository.findById("1")).thenReturn(Optional.of(existing()));
        assertThat(service.get("1").getAdmissionNumber()).isEqualTo("ADM-001");
    }

    @Test
    void get_missingStudent_throwsNotFound() {
        when(repository.findById("nope")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get("nope")).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_changesFieldsAndKeepsRegistrationDate() {
        when(repository.findById("1")).thenReturn(Optional.of(existing()));
        when(repository.save(any(Student.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        Student updated = service.update("1", request("ADM-001"));

        assertThat(updated.getFullName()).isEqualTo("Ada Obi");
        assertThat(updated.getAge()).isEqualTo(12);
        assertThat(updated.getRegistrationDate()).isEqualTo(LocalDate.of(2025, 1, 10));
        verify(repository, never()).existsByAdmissionNumber(any());
    }

    @Test
    void update_toAdmissionNumberOwnedByAnotherStudent_throwsDuplicate() {
        when(repository.findById("1")).thenReturn(Optional.of(existing()));
        when(repository.existsByAdmissionNumber("ADM-999")).thenReturn(true);

        assertThatThrownBy(() -> service.update("1", request("ADM-999")))
                .isInstanceOf(DuplicateResourceException.class);
        verify(repository, never()).save(any(Student.class));
    }

    @Test
    void update_missingStudent_throwsNotFound() {
        when(repository.findById("nope")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update("nope", request("ADM-001")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_removesStudentAndTheirAttendance() {
        when(repository.existsById("1")).thenReturn(true);

        service.delete("1");

        verify(attendanceRepository).deleteByStudentId("1");
        verify(repository).deleteById("1");
    }

    @Test
    void delete_missingStudent_throwsNotFound() {
        when(repository.existsById("nope")).thenReturn(false);

        assertThatThrownBy(() -> service.delete("nope")).isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }

    @Test
    void search_returnsPageMetadata() {
        when(mongoTemplate.count(any(Query.class), eq(Student.class))).thenReturn(25L);
        when(mongoTemplate.find(any(Query.class), eq(Student.class))).thenReturn(List.of(existing()));

        PageResponse<Student> page = service.search("old", "Grade 7A", 0, 10);

        assertThat(page.totalElements()).isEqualTo(25);
        assertThat(page.totalPages()).isEqualTo(3);
        assertThat(page.size()).isEqualTo(10);
        assertThat(page.content()).hasSize(1);
    }
}
