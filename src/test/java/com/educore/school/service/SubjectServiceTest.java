package com.educore.school.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.educore.school.dto.SubjectRequest;
import com.educore.school.exception.DuplicateResourceException;
import com.educore.school.exception.ResourceNotFoundException;
import com.educore.school.model.Subject;
import com.educore.school.repository.SubjectRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

    @Mock SubjectRepository repository;
    @InjectMocks SubjectService service;

    private static SubjectRequest request(String code) {
        return new SubjectRequest("Mathematics", code, "Grade 7A", "Mr. Lawal");
    }

    @Test
    void create_normalizesCodeToUpperCase() {
        when(repository.findByCode("MTH7")).thenReturn(Optional.empty());
        when(repository.save(any(Subject.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        assertThat(service.create(request(" mth7 ")).getCode()).isEqualTo("MTH7");
    }

    @Test
    void create_duplicateCode_throwsConflict() {
        when(repository.findByCode("MTH7")).thenReturn(Optional.of(new Subject()));

        assertThatThrownBy(() -> service.create(request("mth7"))).isInstanceOf(DuplicateResourceException.class);
        verify(repository, never()).save(any(Subject.class));
    }

    @Test
    void list_withClassName_usesClassQuery() {
        when(repository.findByClassNameOrderByNameAsc("Grade 7A")).thenReturn(List.of(new Subject()));
        assertThat(service.list("Grade 7A")).hasSize(1);
    }

    @Test
    void delete_missingSubject_throwsNotFound() {
        when(repository.existsById("nope")).thenReturn(false);
        assertThatThrownBy(() -> service.delete("nope")).isInstanceOf(ResourceNotFoundException.class);
    }
}
