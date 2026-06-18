package com.salary.repository;

import com.salary.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String> {

    Optional<Department> findByDeptName(String deptName);

    boolean existsByDeptName(String deptName);
}
