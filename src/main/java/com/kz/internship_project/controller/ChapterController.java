package com.kz.internship_project.controller;

import com.kz.internship_project.config.ApiCommonResponses;
import com.kz.internship_project.dto.chapter.ChapterCreateDto;
import com.kz.internship_project.dto.chapter.ChapterResponseDto;
import com.kz.internship_project.service.ChapterService;
import com.kz.internship_project.service.impl.ChapterServiceImpl;
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
@RequestMapping("/api/v1/chapters")
@ApiCommonResponses
@Tag(name = "Главы курса", description = "Управление главами")
public class ChapterController {

    private final ChapterService chapterService;

    @PostMapping
    @Operation(summary = "Создать новую главу", description = "Добавляет главу в базу")
    @ApiResponse(responseCode = "201", description = "Глава успешно создана")

    public ResponseEntity<ChapterResponseDto> create(@Valid @RequestBody ChapterCreateDto dto) {
        ChapterResponseDto response = chapterService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Найти главу по id", description = "Возвращает данные одной главы. Если id не существует, бросает 404.")
    @ApiResponse(responseCode = "200", description = "Глава найдена")

    public ResponseEntity<ChapterResponseDto> getById(@Parameter(description = "id главы", example = "1") @PathVariable Long id) {
        ChapterResponseDto response = chapterService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить главу", description = "Полное обновление данных главы по его id")
    @ApiResponse(responseCode = "200", description = "Глава успешно обновлена")

    public ResponseEntity<ChapterResponseDto> update(@Parameter(description = "id главы", example = "1") @PathVariable Long id, @Valid @RequestBody ChapterCreateDto dto) {
        ChapterResponseDto response = chapterService.update(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить главу", description = "Удаляет запись о главе из базы данных")
    @ApiResponse(responseCode = "204", description = "Глава успешно удалена")

    public ResponseEntity<Void> delete(@Parameter(description = "id главы", example = "1") @PathVariable Long id) {
        chapterService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Получить список глав по id курса", description = "Возвращает список глав, отсортированных по порядковому номеру. Если id не существует, бросает 404.")
    @ApiResponse(responseCode = "200", description = "Список глав успешно получен")

    public ResponseEntity<List<ChapterResponseDto>> getByCourseId(@Parameter(description = "id курса", example = "1") @RequestParam Long courseId) {
        List<ChapterResponseDto> response = chapterService.getByCourseId(courseId);
        return ResponseEntity.ok(response);
    }

}
