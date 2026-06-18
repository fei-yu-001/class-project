package com.salary.repository;

import com.salary.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Integer> {
    List<LeaveRequest> findByEmpId(Integer empId);

    List<LeaveRequest> findByEmpIdAndApprovalStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Integer empId, String approvalStatus, LocalDate periodEnd, LocalDate periodStart);
}
