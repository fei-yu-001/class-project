package com.salary.service;

import com.salary.dto.SalaryBatchGenerateRequest;
import com.salary.dto.SalaryCalculationPreview;
import com.salary.dto.SalaryRequest;
import com.salary.entity.Salary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SalaryService {

    Salary create(SalaryRequest request);

    Salary update(Integer salaryId, SalaryRequest request);

    void delete(Integer salaryId);

    Salary getById(Integer salaryId);

    Page<Salary> search(Integer empId, String payPeriod, Pageable pageable);

    List<SalaryCalculationPreview> preview(String payPeriod);

    List<Salary> generateBatch(SalaryBatchGenerateRequest request);

    Salary approve(Integer salaryId);

    Salary pay(Integer salaryId);
}
