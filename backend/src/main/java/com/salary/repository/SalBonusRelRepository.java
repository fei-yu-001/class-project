package com.salary.repository;

import com.salary.entity.SalBonusRel;
import com.salary.entity.SalBonusRel.SalBonusRelId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalBonusRelRepository extends JpaRepository<SalBonusRel, SalBonusRelId> {
    List<SalBonusRel> findBySalaryId(Integer salaryId);
    void deleteBySalaryId(Integer salaryId);
}
