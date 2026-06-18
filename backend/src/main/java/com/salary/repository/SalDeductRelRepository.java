package com.salary.repository;

import com.salary.entity.SalDeductRel;
import com.salary.entity.SalDeductRel.SalDeductRelId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalDeductRelRepository extends JpaRepository<SalDeductRel, SalDeductRelId> {
    List<SalDeductRel> findBySalaryId(Integer salaryId);
    void deleteBySalaryId(Integer salaryId);
}
