package com.kz.internship_project.service.impl;

import com.kz.internship_project.dto.ChapterCreateDto;
import com.kz.internship_project.dto.ChapterResponseDto;
import com.kz.internship_project.dto.LessonResponseDto;
import com.kz.internship_project.entity.Chapter;
import com.kz.internship_project.entity.Course;
import com.kz.internship_project.entity.Lesson;
import com.kz.internship_project.mapper.ChapterMapper;
import com.kz.internship_project.repository.ChapterRepository;
import com.kz.internship_project.repository.CourseRepository;
import com.kz.internship_project.repository.LessonRepository;
import com.kz.internship_project.service.ChapterService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChapterServiceImpl implements ChapterService {

    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final ChapterMapper chapterMapper;
    private final LessonRepository lessonRepository;


    @Override
    @Transactional
    public ChapterResponseDto create(ChapterCreateDto dto) {
        log.info("Создание новой главы");
        log.debug("Создание главы с данными: {}", dto);

        Course course = courseRepository.findById(dto.courseId()).orElseThrow(() -> new EntityNotFoundException("Курс с id " + dto.courseId() + " не найден"));
        Chapter chapter = chapterMapper.toEntity(dto);
        Integer maxOrder = chapterRepository.findFirstByCourseIdOrderByChapterOrderDesc(dto.courseId()).map(Chapter::getChapterOrder).orElse(0);
        chapter.setChapterOrder(maxOrder + 1);
        chapter.setCourse(course);
        Chapter savedChapter = chapterRepository.save(chapter);

        ChapterResponseDto chapterResponseDto = chapterMapper.toDto(savedChapter);

        log.debug("Глава успешно сохранена: {}", chapterResponseDto);
        return chapterResponseDto;
    }

    @Override
    public ChapterResponseDto getById(Long id) {

        log.info("Получение главы по id: {}", id);

        Chapter chapter = chapterRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Глава с id " + id + " не найдена"));
        return chapterMapper.toDto(chapter);
    }

    @Override
    @Transactional
    public ChapterResponseDto update(Long id, ChapterCreateDto dto) {

        log.info("Обновление главы по id: {}", id);
        log.debug("Обновление главы с данными: {}", dto);

        Course course = courseRepository.findById(dto.courseId()).orElseThrow(() -> new EntityNotFoundException("Курс с id " + dto.courseId() + " не найден"));
        Chapter existingChapter = chapterRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Глава с id " + id + " не найдена"));
        existingChapter.setName(dto.name());
        existingChapter.setDescription(dto.description());
        existingChapter.setCourse(course);
        Chapter updatedChapter = chapterRepository.save(existingChapter);

        ChapterResponseDto chapterResponseDto = chapterMapper.toDto(updatedChapter);

        log.debug("Успешное обновление главы: {}", chapterResponseDto);
        return chapterResponseDto;

    }

    @Override
    @Transactional
    public void delete(Long id) {

        log.info("Удаление главы по id: {}", id);

        if (!chapterRepository.existsById(id)) {
            throw new EntityNotFoundException("Глава с id " + id + " не найдена");
        }

        if (lessonRepository.existsById(id)){
            throw new IllegalArgumentException("Главу нельзя удалить, пока в ней есть уроки!");
        }

        chapterRepository.deleteById(id);
    }

    @Override
    public List<ChapterResponseDto> getByCourseId(Long courseId) {

        log.info("Получение списка глав по id курса: {}", courseId);

        if (!courseRepository.existsById(courseId)) {
            throw new EntityNotFoundException("Курс с id " + courseId + " не найден");
        }
        return chapterRepository.findByCourseIdOrderByChapterOrderAsc(courseId).stream().map(chapterMapper::toDto).toList();

    }

}
