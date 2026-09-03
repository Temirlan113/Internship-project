package com.kz.internship_project.repository;

import com.kz.internship_project.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findByCourseIdOrderByChapterOrderAsc(Long courseId);

    Optional<Chapter> findFirstByCourseIdOrderByChapterOrderDesc(Long courseId);

    boolean existsByCourseId(Long courseId);

}
