package com.salary.repository;

import com.salary.entity.Bonus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BonusRepository extends JpaRepository<Bonus, Integer> {

    @Query(value = "SELECT b.* FROM bonus b JOIN sal_bonus_rel r ON b.bonus_id = r.bonus_id WHERE r.salary_id = :salaryId", nativeQuery = true)
    List<Bonus> findBySalaryId(@Param("salaryId") Integer salaryId);
}
