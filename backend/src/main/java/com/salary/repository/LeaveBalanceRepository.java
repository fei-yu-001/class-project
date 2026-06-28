package com.salary.repository;

import com.salary.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Integer> {

    Optional<LeaveBalance> findByEmpIdAndLeaveTypeAndYear(Integer empId, String leaveType, Integer year);
}
