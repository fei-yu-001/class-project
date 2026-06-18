package com.salary.service;

import com.salary.dto.DepartmentRequest;
import com.salary.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DepartmentService {

    Department create(DepartmentRequest request);

    Department update(String deptCode, DepartmentRequest request);

    void delete(String deptCode);

    Department getById(String deptCode);

    List<Department> listAll();

    Page<Department> listPage(Pageable pageable);
}
