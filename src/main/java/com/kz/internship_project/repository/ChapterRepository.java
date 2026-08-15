package com.kz.internship_project.repository;

import com.kz.internship_project.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findByCourseIdOrderByChapterOrderAsc(Long courseId);

    Integer findMaxOrderByCourseId(Long courseId);
}
