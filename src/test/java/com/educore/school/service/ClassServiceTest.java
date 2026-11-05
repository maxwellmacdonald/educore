package com.educore.school.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.educore.school.dto.ClassRequest;
import com.educore.school.exception.DuplicateResourceException;
import com.educore.school.exception.ResourceNotFoundException;
import com.educore.school.model.SchoolClass;
import com.educore.school.repository.SchoolClassRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClassServiceTest {

    @Mock SchoolClassRepository repository;
    @InjectMocks ClassService service;

    private static ClassRequest request(String name) {
        return new ClassRequest(name, "Grade 7", "Mrs. Adebayo", 30);
    }

    private static SchoolClass classWithId(String id, String name) {
        SchoolClass c = new SchoolClass();
        c.setId(id);
        c.setName(name);
        return c;
    }

    @Test
    void create_savesNewClass() {
        when(repository.findByNameIgnoreCase("Grade 7A")).thenReturn(Optional.empty());
        when(repository.save(any(SchoolClass.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        SchoolClass created = service.create(request(" Grade 7A "));

        assertThat(created.getName()).isEqualTo("Grade 7A");
        assertThat(created.getCapacity()).isEqualTo(30);
    }

    @Test
    void create_duplicateName_throwsConflict() {
        when(repository.findByNameIgnoreCase("Grade 7A")).thenReturn(Optional.of(classWithId("1", "Grade 7A")));

        assertThatThrownBy(() -> service.create(request("Grade 7A")))
                .isInstanceOf(DuplicateResourceException.class);
        verify(repository, never()).save(any(SchoolClass.class));
    }

    @Test
    void update_toNameUsedByAnotherClass_throwsConflict() {
        when(repository.findById("1")).thenReturn(Optional.of(classWithId("1", "Grade 7A")));
        when(repository.findByNameIgnoreCase("Grade 8A")).thenReturn(Optional.of(classWithId("2", "Grade 8A")));

        assertThatThrownBy(() -> service.update("1", request("Grade 8A")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void update_keepingOwnName_isAllowed() {
        SchoolClass current = classWithId("1", "Grade 7A");
        when(repository.findById("1")).thenReturn(Optional.of(current));
        when(repository.findByNameIgnoreCase("Grade 7A")).thenReturn(Optional.of(current));
        when(repository.save(any(SchoolClass.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        SchoolClass updated = service.update("1", new ClassRequest("Grade 7A", "Grade 7", "New Teacher", 35));

        assertThat(updated.getClassTeacher()).isEqualTo("New Teacher");
        assertThat(updated.getCapacity()).isEqualTo(35);
    }

    @Test
    void delete_missingClass_throwsNotFound() {
        when(repository.existsById("nope")).thenReturn(false);
        assertThatThrownBy(() -> service.delete("nope")).isInstanceOf(ResourceNotFoundException.class);
    }
}
