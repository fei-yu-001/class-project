package com.salary.repository;

import com.salary.entity.Deduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeductionRepository extends JpaRepository<Deduction, Integer> {

    @Query(value = "SELECT d.* FROM deduction d JOIN sal_deduct_rel r ON d.deduct_id = r.deduct_id WHERE r.salary_id = :salaryId", nativeQuery = true)
    List<Deduction> findBySalaryId(@Param("salaryId") Integer salaryId);
}
