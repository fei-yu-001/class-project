package com.salary.repository;

import com.salary.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {
    List<Attendance> findByEmpId(Integer empId);

    List<Attendance> findByEmpIdAndAttDateBetween(Integer empId, LocalDate startDate, LocalDate endDate);

    boolean existsByEmpIdAndAttDate(Integer empId, LocalDate attDate);
}
