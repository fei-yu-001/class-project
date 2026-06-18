package com.salary.service;

import com.salary.entity.Attendance;
import com.salary.entity.LeaveRequest;
import com.salary.entity.OvertimeRecord;
import java.util.List;

public interface AttendanceService {
    List<Attendance> getAllAttendance();
    List<Attendance> getAttendanceByEmployee(Integer empId);
    Attendance createAttendance(Attendance record);
    Attendance updateAttendance(Integer id, Attendance record);
    void deleteAttendance(Integer id);

    List<LeaveRequest> getAllLeaveRequests();
    List<LeaveRequest> getLeaveRequestsByEmployee(Integer empId);
    LeaveRequest createLeaveRequest(LeaveRequest request);
    LeaveRequest updateLeaveRequest(Integer id, LeaveRequest request);
    void deleteLeaveRequest(Integer id);

    List<OvertimeRecord> getAllOvertimeRecords();
    List<OvertimeRecord> getOvertimeRecordsByEmployee(Integer empId);
    OvertimeRecord createOvertimeRecord(OvertimeRecord record);
    OvertimeRecord updateOvertimeRecord(Integer id, OvertimeRecord record);
    void deleteOvertimeRecord(Integer id);
}