package com.kz.internship_project.repository;

import com.kz.internship_project.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findByCourseIdOrderByChapterOrderAsc(Long courseId);

    Optional<Chapter> findFirstByCourseIdOrderByChapterOrderDesc(Long courseId);

    boolean existsByCourseId(Long courseId);

    @Modifying
    @Query(value = """
        INSERT INTO chapters (name, description,course_id, chapter_order, created_time, updated_time)
        VALUES (
            :name,
            :description,
            :course_id,
            (SELECT COALESCE(MAX(chapter_order), 0) + 1 FROM chapters WHERE course_id = :courseId),
            NOW(),
            NOW()
        )
        RETURNING *
        """, nativeQuery = true)
    void insertNextChapter(
      @Param("name") String name,
      @Param("description") String description,
      @Param("courseId") Long courseId
    );

}
