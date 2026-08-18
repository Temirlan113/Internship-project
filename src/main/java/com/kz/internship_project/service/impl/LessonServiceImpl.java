package com.kz.internship_project.service.impl;

import com.kz.internship_project.dto.CourseResponseDto;
import com.kz.internship_project.dto.LessonCreateDto;
import com.kz.internship_project.dto.LessonResponseDto;
import com.kz.internship_project.entity.Chapter;
import com.kz.internship_project.entity.Course;
import com.kz.internship_project.entity.Lesson;
import com.kz.internship_project.mapper.LessonMapper;
import com.kz.internship_project.repository.ChapterRepository;
import com.kz.internship_project.repository.LessonRepository;
import com.kz.internship_project.service.LessonService;
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
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final ChapterRepository chapterRepository;
    private final LessonMapper lessonMapper;

    @Override
    @Transactional
    public LessonResponseDto create(LessonCreateDto dto) {

        log.info("Создание нового урока");
        log.debug("Создание урока с данными: {}", dto);

        Chapter chapter = chapterRepository.findById(dto.chapterId()).orElseThrow(() -> new EntityNotFoundException("Глава с id " + dto.chapterId() + " не найдена"));
        Lesson lesson = lessonMapper.toEntity(dto);
        Integer maxOrder = lessonRepository.findFirstByChapterIdOrderByLessonOrderDesc(dto.chapterId()).map(Lesson::getLessonOrder).orElse(0);
        lesson.setLessonOrder(maxOrder + 1);
        lesson.setChapter(chapter);
        Lesson savedLesson = lessonRepository.save(lesson);

        LessonResponseDto responseDto = lessonMapper.toDto(savedLesson);

        log.debug("Урок успешно сохранен: {}", responseDto);
        return responseDto;


    }

    @Override
    public LessonResponseDto getById(Long id) {

        log.info("Получение урока по id: {}", id);

        Lesson lesson = lessonRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Урок с id " + id + " не найден"));

        return lessonMapper.toDto(lesson);
    }

    @Override
    @Transactional
    public LessonResponseDto update(Long id, LessonCreateDto dto) {

        log.info("Обновление урока по id: {}", id);
        log.debug("Обновление урока с данными: {}", dto);


        Chapter chapter = chapterRepository.findById(dto.chapterId()).orElseThrow(() -> new EntityNotFoundException("Глава с id " + dto.chapterId() + " не найдена"));
        Lesson existingLesson = lessonRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Урок с id " + id + " не найден"));
        existingLesson.setName(dto.name());
        existingLesson.setDescription(dto.description());
        existingLesson.setContent(dto.content());
        existingLesson.setChapter(chapter);
        Lesson updatedLesson = lessonRepository.save(existingLesson);

        LessonResponseDto lessonResponseDto = lessonMapper.toDto(updatedLesson);

        log.debug("Успешное обновление урока: {}", lessonResponseDto);
        return lessonResponseDto;
    }

    @Override
    @Transactional
    public void delete(Long id) {

        log.info("Удаление урока по id: {}", id);

        if (!lessonRepository.existsById(id)) {
            throw new EntityNotFoundException("Урок с id " + id + " не найден");
        }
        lessonRepository.deleteById(id);
    }

    @Override
    public List<LessonResponseDto> getByChapterId(Long chapterId) {

        log.info("Получение списка уроков по id главы: {}", chapterId);

        if (chapterRepository.existsById(chapterId)) {
            throw new EntityNotFoundException("Глава с id " + chapterId + " не найдена");

        }

        return lessonRepository.findByChapterIdOrderByLessonOrderAsc(chapterId).stream().map(lessonMapper::toDto).toList();

    }


}
