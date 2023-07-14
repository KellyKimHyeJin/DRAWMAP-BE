package com.umc.drawmap.repository;

import com.umc.drawmap.domain.UserCourse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
public interface UserCourseRepository extends JpaRepository<UserCourse, Long>{
    Optional<UserCourse> findById(Long uCourseId);
    List<UserCourse> findAll();
}