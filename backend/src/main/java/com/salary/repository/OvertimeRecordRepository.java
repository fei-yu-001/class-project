package com.salary.repository;

import com.salary.entity.OvertimeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OvertimeRecordRepository extends JpaRepository<OvertimeRecord, Integer> {
    List<OvertimeRecord> findByEmpId(Integer empId);

    List<OvertimeRecord> findByEmpIdAndApprovalStatusAndOtDateBetween(
            Integer empId, String approvalStatus, LocalDate startDate, LocalDate endDate);
}
