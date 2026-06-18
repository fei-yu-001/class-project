package com.salary.repository;

import com.salary.entity.PositionChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PositionChangeRepository extends JpaRepository<PositionChange, Integer> {
    List<PositionChange> findByEmpId(Integer empId);
}
