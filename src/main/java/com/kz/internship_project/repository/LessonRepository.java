package com.kz.internship_project.repository;

import com.kz.internship_project.entity.Chapter;
import com.kz.internship_project.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByChapterIdOrderByLessonOrderAsc(Long chapterId);

    Optional<Lesson> findFirstByChapterIdOrderByLessonOrderDesc(Long chapterId);

    boolean existsByChapterId(Long chapterId);

    @Modifying
    @Query(value = """
INSERT INTO lessons (name, description, content, chapter_id, lesson_order, created_time, updated_time)
VALUES (
    :name,
    :description,
    :content,
    :chapter_id,
    (SELECT COALESCE(MAX(lesson_order), 0) + 1 FROM lessons WHERE chapter_id = :chapterId),
    NOW(),
    NOW()
    )
RETURNING *
""", nativeQuery = true)
    void insertNextLesson(
            @Param("name") String name,
            @Param("description") String description,
            @Param("content") String content,
            @Param("courseId") Long chapterId
    );

}
