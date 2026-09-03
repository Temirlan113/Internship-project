package com.kz.internship_project.repository;

import com.kz.internship_project.entity.Chapter;
import com.kz.internship_project.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByChapterIdOrderByLessonOrderAsc(Long chapterId);

    Optional<Lesson> findFirstByChapterIdOrderByLessonOrderDesc(Long chapterId);

    boolean existsByChapterId(Long chapterId);

}
