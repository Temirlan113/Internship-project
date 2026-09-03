package com.kz.internship_project.controller;

import com.kz.internship_project.config.ApiCommonResponses;
import com.kz.internship_project.dto.lesson.LessonCreateDto;
import com.kz.internship_project.dto.lesson.LessonResponseDto;
import com.kz.internship_project.service.LessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lessons")
@ApiCommonResponses
@Tag(name = "Уроки", description = "Управление уроками")

public class LessonController {

    private final LessonService lessonService;

    @PostMapping
    @Operation(summary = "Создать новый урок", description = "Добавляет урок в базу")
    @ApiResponse(responseCode = "201", description = "Урок успешно создан")

    public ResponseEntity<LessonResponseDto> create(@Valid @RequestBody LessonCreateDto dto) {
        LessonResponseDto response = lessonService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Найти урок по id", description = "Возвращает данные одного урока. Если id не существует, бросает 404.")
    @ApiResponse(responseCode = "200", description = "Урок найден")

    public ResponseEntity<LessonResponseDto> getById(@Parameter(description = "id урока", example = "1") @PathVariable Long id) {
        LessonResponseDto response = lessonService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить урок", description = "Полное обновление данных урока по его id")
    @ApiResponse(responseCode = "200", description = "Урок успешно обновлен")

    public ResponseEntity<LessonResponseDto> update(@Parameter(description = "id урока", example = "1") @PathVariable Long id, @Valid @RequestBody LessonCreateDto dto) {
        LessonResponseDto response = lessonService.update(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить урок", description = "Удаляет запись об уроке из базы данных")
    @ApiResponse(responseCode = "204", description = "Урок успешно удален")

    public ResponseEntity<Void> delete(@Parameter(description = "id урока", example = "1") @PathVariable Long id) {
        lessonService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Получить список уроков по id главы курса", description = "Возвращает список уроков, отсортированных по порядковому номеру. Если id не существует, бросает 404.")
    @ApiResponse(responseCode = "200", description = "Список уроков успешно получен")

    public ResponseEntity<List<LessonResponseDto>> getByChapterId(@Parameter(description = "id главы курса", example = "1") @RequestParam Long chapterId) {
        List<LessonResponseDto> response = lessonService.getByChapterId(chapterId);
        return ResponseEntity.ok(response);
    }


}
