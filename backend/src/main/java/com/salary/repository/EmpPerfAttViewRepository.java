package com.salary.repository;

import com.salary.entity.EmpPerfAttView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpPerfAttViewRepository extends JpaRepository<EmpPerfAttView, Integer> {

    @Query("SELECT COALESCE(e.grade, '未考核'), COUNT(e) FROM EmpPerfAttView e GROUP BY e.grade")
    List<Object[]> gradeDistribution();

    @Query("SELECT COALESCE(e.attStatus, '未记录'), COUNT(e) FROM EmpPerfAttView e GROUP BY e.attStatus")
    List<Object[]> attStatusDistribution();
}