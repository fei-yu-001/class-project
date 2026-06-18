package com.salary.service.impl;

import com.salary.dto.EmployeeRequest;
import com.salary.entity.Employee;
import com.salary.repository.DepartmentRepository;
import com.salary.repository.EmployeeRepository;
import com.salary.repository.PositionRepository;
import com.salary.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;

    @Override
    @Transactional
    public Employee create(EmployeeRequest request) {
        if (!positionRepository.existsById(request.getPosId())) {
            throw new IllegalArgumentException("职位不存在");
        }
        if (!departmentRepository.existsById(request.getDeptCode())) {
            throw new IllegalArgumentException("部门不存在");
        }

        Employee employee = Employee.builder()
                .empName(request.getEmpName())
                .gender(request.getGender())
                .birthDate(request.getBirthDate() != null ? LocalDate.parse(request.getBirthDate()) : null)
                .hireDate(request.getHireDate() != null ? LocalDate.parse(request.getHireDate()) : null)
                .posId(request.getPosId())
                .deptCode(request.getDeptCode())
                .empStatus(request.getEmpStatus() != null ? request.getEmpStatus() : "在职")
                .build();

        return employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public Employee update(Integer empId, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new IllegalArgumentException("员工不存在"));

        if (!positionRepository.existsById(request.getPosId())) {
            throw new IllegalArgumentException("职位不存在");
        }
        if (!departmentRepository.existsById(request.getDeptCode())) {
            throw new IllegalArgumentException("部门不存在");
        }

        employee.setEmpName(request.getEmpName());
        employee.setGender(request.getGender());
        employee.setBirthDate(request.getBirthDate() != null ? LocalDate.parse(request.getBirthDate()) : null);
        if (request.getHireDate() != null) {
            employee.setHireDate(LocalDate.parse(request.getHireDate()));
        }
        employee.setPosId(request.getPosId());
        employee.setDeptCode(request.getDeptCode());
        if (request.getEmpStatus() != null) {
            employee.setEmpStatus(request.getEmpStatus());
        }

        return employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public void delete(Integer empId) {
        if (!employeeRepository.existsById(empId)) {
            throw new IllegalArgumentException("员工不存在");
        }
        employeeRepository.deleteById(empId);
    }

    @Override
    public Employee getById(Integer empId) {
        return employeeRepository.findById(empId)
                .orElseThrow(() -> new IllegalArgumentException("员工不存在"));
    }

    @Override
    public Page<Employee> search(String empName, String deptCode, String empStatus, Pageable pageable) {
        return employeeRepository.search(empName, deptCode, empStatus, pageable);
    }

    @Override
    public long countActive() {
        return employeeRepository.countByEmpStatus("在职");
    }
}
