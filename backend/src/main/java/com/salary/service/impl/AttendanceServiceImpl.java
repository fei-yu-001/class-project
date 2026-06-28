package com.salary.service.impl;

import com.salary.entity.Attendance;
import com.salary.entity.LeaveRequest;
import com.salary.entity.OvertimeRecord;
import com.salary.repository.AttendanceRepository;
import com.salary.repository.EmployeeRepository;
import com.salary.repository.LeaveRequestRepository;
import com.salary.repository.OvertimeRecordRepository;
import com.salary.repository.LeaveBalanceRepository;
import com.salary.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final OvertimeRecordRepository overtimeRecordRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    @Override
    public List<Attendance> getAllAttendance() { return attendanceRepository.findAll(); }

    @Override
    public List<Attendance> getAttendanceByEmployee(Integer empId) { return attendanceRepository.findByEmpId(empId); }

    @Override
    @Transactional
    public Attendance createAttendance(Attendance record) {
        validateEmployee(record.getEmpId());
        boolean exists = !attendanceRepository.findByEmpIdAndAttDateBetween(
                record.getEmpId(), record.getAttDate(), record.getAttDate()).isEmpty();
        if (exists) {
            throw new IllegalArgumentException("该员工在 " + record.getAttDate() + " 已有考勤记录");
        }
        return attendanceRepository.save(record);
    }

    @Override
    @Transactional
    public Attendance updateAttendance(Integer id, Attendance record) {
        Attendance existing = attendanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("记录不存在"));
        validateEmployee(record.getEmpId());
        existing.setEmpId(record.getEmpId());
        existing.setAttDate(record.getAttDate());
        existing.setAttStatus(record.getAttStatus());
        existing.setLateMinutes(record.getLateMinutes());
        existing.setEarlyLeaveMinutes(record.getEarlyLeaveMinutes());
        existing.setAbsent(record.getAbsent());
        return attendanceRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteAttendance(Integer id) { attendanceRepository.deleteById(id); }

    @Override
    public List<LeaveRequest> getAllLeaveRequests() { return leaveRequestRepository.findAll(); }

    @Override
    public List<LeaveRequest> getLeaveRequestsByEmployee(Integer empId) { return leaveRequestRepository.findByEmpId(empId); }

    @Override
    @Transactional
    public LeaveRequest createLeaveRequest(LeaveRequest request) {
        validateEmployee(request.getEmpId());
        return leaveRequestRepository.save(request);
    }

    @Override
    @Transactional
    public LeaveRequest updateLeaveRequest(Integer id, LeaveRequest request) {
        LeaveRequest existing = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("记录不存在"));
        validateEmployee(request.getEmpId());
        existing.setEmpId(request.getEmpId());
        existing.setLeaveType(request.getLeaveType());
        existing.setStartDate(request.getStartDate());
        existing.setEndDate(request.getEndDate());
        existing.setLeaveDays(request.getLeaveDays());
        existing.setApprovalStatus(request.getApprovalStatus());
        return leaveRequestRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteLeaveRequest(Integer id) { leaveRequestRepository.deleteById(id); }

    @Override
    public List<OvertimeRecord> getAllOvertimeRecords() { return overtimeRecordRepository.findAll(); }

    @Override
    public List<OvertimeRecord> getOvertimeRecordsByEmployee(Integer empId) { return overtimeRecordRepository.findByEmpId(empId); }

    @Override
    @Transactional
    public OvertimeRecord createOvertimeRecord(OvertimeRecord record) {
        validateEmployee(record.getEmpId());
        return overtimeRecordRepository.save(record);
    }

    @Override
    @Transactional
    public OvertimeRecord updateOvertimeRecord(Integer id, OvertimeRecord record) {
        OvertimeRecord existing = overtimeRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("记录不存在"));
        validateEmployee(record.getEmpId());
        existing.setEmpId(record.getEmpId());
        existing.setOtHours(record.getOtHours());
        existing.setOtDate(record.getOtDate());
        existing.setApprovalStatus(record.getApprovalStatus());
        return overtimeRecordRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteOvertimeRecord(Integer id) { overtimeRecordRepository.deleteById(id); }

    @Override
    @Transactional
    public LeaveRequest approveLeave(Integer leaveId) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("请假记录不存在"));
        if (!"PENDING".equals(leave.getApprovalStatus())) {
            throw new IllegalArgumentException("只有待审批的请假可以审核通过");
        }
        // Check and deduct leave balance
        var balanceOpt = leaveBalanceRepository.findByEmpIdAndLeaveTypeAndYear(
                leave.getEmpId(), leave.getLeaveType(), Year.now().getValue());
        if (balanceOpt.isPresent()) {
            var balance = balanceOpt.get();
            BigDecimal remaining = balance.getTotalDays().subtract(balance.getUsedDays());
            if (remaining.compareTo(leave.getLeaveDays()) < 0) {
                throw new IllegalArgumentException("假期余额不足，剩余" + remaining + "天");
            }
            balance.setUsedDays(balance.getUsedDays().add(leave.getLeaveDays()));
            leaveBalanceRepository.save(balance);
        }
        // If no balance record exists, still allow approval (e.g., sick leave has no balance limit)
        leave.setApprovalStatus("APPROVED");
        return leaveRequestRepository.save(leave);
    }

    @Override
    @Transactional
    public LeaveRequest rejectLeave(Integer leaveId) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("请假记录不存在"));
        if (!"PENDING".equals(leave.getApprovalStatus())) {
            throw new IllegalArgumentException("只有待审批的请假可以驳回");
        }
        leave.setApprovalStatus("REJECTED");
        return leaveRequestRepository.save(leave);
    }

    @Override
    @Transactional
    public OvertimeRecord approveOvertime(Integer otId) {
        OvertimeRecord ot = overtimeRecordRepository.findById(otId)
                .orElseThrow(() -> new IllegalArgumentException("加班记录不存在"));
        if (!"PENDING".equals(ot.getApprovalStatus())) {
            throw new IllegalArgumentException("只有待审批的加班可以审核通过");
        }
        ot.setApprovalStatus("APPROVED");
        return overtimeRecordRepository.save(ot);
    }

    @Override
    @Transactional
    public OvertimeRecord rejectOvertime(Integer otId) {
        OvertimeRecord ot = overtimeRecordRepository.findById(otId)
                .orElseThrow(() -> new IllegalArgumentException("加班记录不存在"));
        if (!"PENDING".equals(ot.getApprovalStatus())) {
            throw new IllegalArgumentException("只有待审批的加班可以驳回");
        }
        ot.setApprovalStatus("REJECTED");
        return overtimeRecordRepository.save(ot);
    }

    private void validateEmployee(Integer empId) {
        if (empId == null || !employeeRepository.existsById(empId)) {
            throw new IllegalArgumentException("员工不存在");
        }
    }
}
