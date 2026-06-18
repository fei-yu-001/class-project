package com.salary.repository;

import com.salary.entity.EmpFullView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpFullViewRepository extends JpaRepository<EmpFullView, Integer> {
    List<EmpFullView> findByDeptCode(String deptCode);
    List<EmpFullView> findByEmpStatus(String empStatus);
}