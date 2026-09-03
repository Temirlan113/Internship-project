package com.kz.internship_project.controller;

import com.kz.internship_project.config.ApiCommonResponses;
import com.kz.internship_project.dto.course.CourseCreateDto;
import com.kz.internship_project.dto.course.CourseResponseDto;
import com.kz.internship_project.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/courses")
@ApiCommonResponses
@Tag(name = "Курсы", description = "Управление курсами")
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @Operation(summary = "Создать новый курс", description = "Добавляет курс в базу")
    @ApiResponse(responseCode = "201", description = "Курс успешно создан")

    public ResponseEntity<CourseResponseDto> create(@Valid @RequestBody CourseCreateDto dto) {
        CourseResponseDto response = courseService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Найти курс по id", description = "Возвращает данные одного курса. Если id не существует, бросает 404.")
    @ApiResponse(responseCode = "200", description = "Курс найден")

    public ResponseEntity<CourseResponseDto> getById(@Parameter(description = "id Курса", example = "1")
                                                     @PathVariable Long id) {
        CourseResponseDto response = courseService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить курс", description = "Полное обновление данных курса по его id")
    @ApiResponse(responseCode = "200", description = "Курс успешно обновлен")

    public ResponseEntity<CourseResponseDto> update(@Parameter(description = "id курса", example = "1")
                                                    @PathVariable Long id, @Valid @RequestBody CourseCreateDto dto) {
        CourseResponseDto response = courseService.update(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить курс", description = "Удаляет запись о курсе из базы данных")
    @ApiResponse(responseCode = "204", description = "Курс успешно удален")

    public ResponseEntity<Void> delete(@Parameter(description = "id курса", example = "1") @PathVariable Long id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Получить список всех курсов", description = "Возвращает список курсов")
    @ApiResponse(responseCode = "200")

    public Page<CourseResponseDto> getAll(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          @RequestParam(defaultValue = "id") String sortBy,
                                          @RequestParam(defaultValue = "asc") String sortDir){
        return courseService.getAll(page, size, sortBy, sortDir);
    }
}
