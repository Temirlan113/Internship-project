package com.kz.internship_project.repository;

import com.kz.internship_project.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {

}
