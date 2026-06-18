package com.salary.service.impl;

import com.salary.dto.DepartmentRequest;
import com.salary.entity.Department;
import com.salary.repository.DepartmentRepository;
import com.salary.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public Department create(DepartmentRequest request) {
        if (departmentRepository.existsById(request.getDeptCode())) {
            throw new IllegalArgumentException("部门代码已存在");
        }
        if (departmentRepository.existsByDeptName(request.getDeptName())) {
            throw new IllegalArgumentException("部门名称已存在");
        }
        Department dept = Department.builder()
                .deptCode(request.getDeptCode())
                .deptName(request.getDeptName())
                .parentDeptCode(request.getParentDeptCode())
                .deptManagerEmp(request.getDeptManagerEmp())
                .build();
        return departmentRepository.save(dept);
    }

    @Override
    @Transactional
    public Department update(String deptCode, DepartmentRequest request) {
        Department dept = departmentRepository.findById(deptCode)
                .orElseThrow(() -> new IllegalArgumentException("部门不存在"));

        if (!dept.getDeptName().equals(request.getDeptName()) && departmentRepository.existsByDeptName(request.getDeptName())) {
            throw new IllegalArgumentException("部门名称已存在");
        }

        dept.setDeptName(request.getDeptName());
        dept.setParentDeptCode(request.getParentDeptCode());
        dept.setDeptManagerEmp(request.getDeptManagerEmp());
        return departmentRepository.save(dept);
    }

    @Override
    @Transactional
    public void delete(String deptCode) {
        if (!departmentRepository.existsById(deptCode)) {
            throw new IllegalArgumentException("部门不存在");
        }
        departmentRepository.deleteById(deptCode);
    }

    @Override
    public Department getById(String deptCode) {
        return departmentRepository.findById(deptCode)
                .orElseThrow(() -> new IllegalArgumentException("部门不存在"));
    }

    @Override
    public List<Department> listAll() {
        return departmentRepository.findAll();
    }

    @Override
    public Page<Department> listPage(Pageable pageable) {
        return departmentRepository.findAll(pageable);
    }
}
