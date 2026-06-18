package com.salary.service;

import com.salary.dto.EmployeeRequest;
import com.salary.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {

    Employee create(EmployeeRequest request);

    Employee update(Integer empId, EmployeeRequest request);

    void delete(Integer empId);

    Employee getById(Integer empId);

    Page<Employee> search(String empName, String deptCode, String empStatus, Pageable pageable);

    long countActive();
}
