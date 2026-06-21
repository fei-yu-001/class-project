package com.salary.controller;

import com.salary.common.Result;
import com.salary.entity.Attendance;
import com.salary.entity.LeaveRequest;
import com.salary.entity.OvertimeRecord;
import com.salary.security.RequireRole;
import com.salary.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;

    @GetMapping("/records")
    public Result<List<Attendance>> getAllAttendance() { return Result.success(attendanceService.getAllAttendance()); }

    @GetMapping("/records/employee/{empId}")
    public Result<List<Attendance>> getAttendanceByEmployee(@PathVariable Integer empId) { return Result.success(attendanceService.getAttendanceByEmployee(empId)); }

    @PostMapping("/records")
    @RequireRole("ADMIN")
    public Result<Attendance> createAttendance(@RequestBody Attendance record) { return Result.success(attendanceService.createAttendance(record)); }

    @PutMapping("/records/{id}")
    @RequireRole("ADMIN")
    public Result<Attendance> updateAttendance(@PathVariable Integer id, @RequestBody Attendance record) { return Result.success(attendanceService.updateAttendance(id, record)); }

    @DeleteMapping("/records/{id}")
    @RequireRole("ADMIN")
    public Result<Void> deleteAttendance(@PathVariable Integer id) { attendanceService.deleteAttendance(id); return Result.success(); }

    @GetMapping("/leaves")
    public Result<List<LeaveRequest>> getAllLeaves() { return Result.success(attendanceService.getAllLeaveRequests()); }

    @GetMapping("/leaves/employee/{empId}")
    public Result<List<LeaveRequest>> getLeavesByEmployee(@PathVariable Integer empId) { return Result.success(attendanceService.getLeaveRequestsByEmployee(empId)); }

    @PostMapping("/leaves")
    @RequireRole("ADMIN")
    public Result<LeaveRequest> createLeave(@RequestBody LeaveRequest request) { return Result.success(attendanceService.createLeaveRequest(request)); }

    @PutMapping("/leaves/{id}")
    @RequireRole("ADMIN")
    public Result<LeaveRequest> updateLeave(@PathVariable Integer id, @RequestBody LeaveRequest request) { return Result.success(attendanceService.updateLeaveRequest(id, request)); }

    @DeleteMapping("/leaves/{id}")
    @RequireRole("ADMIN")
    public Result<Void> deleteLeave(@PathVariable Integer id) { attendanceService.deleteLeaveRequest(id); return Result.success(); }

    @GetMapping("/overtime")
    public Result<List<OvertimeRecord>> getAllOvertime() { return Result.success(attendanceService.getAllOvertimeRecords()); }

    @GetMapping("/overtime/employee/{empId}")
    public Result<List<OvertimeRecord>> getOvertimeByEmployee(@PathVariable Integer empId) { return Result.success(attendanceService.getOvertimeRecordsByEmployee(empId)); }

    @PostMapping("/overtime")
    @RequireRole("ADMIN")
    public Result<OvertimeRecord> createOvertime(@RequestBody OvertimeRecord record) { return Result.success(attendanceService.createOvertimeRecord(record)); }

    @PutMapping("/overtime/{id}")
    @RequireRole("ADMIN")
    public Result<OvertimeRecord> updateOvertime(@PathVariable Integer id, @RequestBody OvertimeRecord record) { return Result.success(attendanceService.updateOvertimeRecord(id, record)); }

    @DeleteMapping("/overtime/{id}")
    @RequireRole("ADMIN")
    public Result<Void> deleteOvertime(@PathVariable Integer id) { attendanceService.deleteOvertimeRecord(id); return Result.success(); }
}