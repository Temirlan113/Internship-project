package com.kz.internship_project.repository;

import com.kz.internship_project.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByChapterIdOrderByLessonOrderAsc(Long chapterId);

    Integer findMaxOrderByChapterId(Long chapterId);
}
