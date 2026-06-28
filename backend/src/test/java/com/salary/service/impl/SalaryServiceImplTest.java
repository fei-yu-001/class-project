package com.salary.service.impl;

import com.salary.dto.SalaryBatchGenerateRequest;
import com.salary.dto.SalaryCalculationPreview;
import com.salary.entity.Attendance;
import com.salary.entity.Employee;
import com.salary.entity.LeaveRequest;
import com.salary.entity.OvertimeRecord;
import com.salary.entity.PerformanceReview;
import com.salary.entity.Position;
import com.salary.entity.Salary;
import com.salary.repository.AttendanceRepository;
import com.salary.repository.BonusRepository;
import com.salary.repository.DeductionRepository;
import com.salary.repository.EmployeeRepository;
import com.salary.repository.LeaveRequestRepository;
import com.salary.repository.OvertimeRecordRepository;
import com.salary.repository.PerformanceReviewRepository;
import com.salary.repository.PositionRepository;
import com.salary.repository.SalaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SalaryServiceImplTest {

    private SalaryRepository salaryRepository;
    private EmployeeRepository employeeRepository;
    private PositionRepository positionRepository;
    private AttendanceRepository attendanceRepository;
    private LeaveRequestRepository leaveRequestRepository;
    private OvertimeRecordRepository overtimeRecordRepository;
    private PerformanceReviewRepository performanceReviewRepository;
    private BonusRepository bonusRepository;
    private DeductionRepository deductionRepository;
    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private SalaryServiceImpl salaryService;

    @BeforeEach
    void setUp() {
        salaryRepository = mock(SalaryRepository.class);
        employeeRepository = mock(EmployeeRepository.class);
        positionRepository = mock(PositionRepository.class);
        attendanceRepository = mock(AttendanceRepository.class);
        leaveRequestRepository = mock(LeaveRequestRepository.class);
        overtimeRecordRepository = mock(OvertimeRecordRepository.class);
        performanceReviewRepository = mock(PerformanceReviewRepository.class);
        bonusRepository = mock(BonusRepository.class);
        deductionRepository = mock(DeductionRepository.class);
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        CacheManager cacheManager = mock(CacheManager.class);
        Cache cache = mock(Cache.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(cacheManager.getCache("dashboardStats")).thenReturn(cache);

        salaryService = new SalaryServiceImpl(
                salaryRepository,
                employeeRepository,
                positionRepository,
                attendanceRepository,
                leaveRequestRepository,
                overtimeRecordRepository,
                performanceReviewRepository,
                bonusRepository,
                deductionRepository,
                redisTemplate,
                cacheManager
        );
    }

    @Test
    void calculateCoversPerformanceOvertimeAttendanceAndLeaveRules() throws Exception {
        Employee employee = Employee.builder()
                .empId(1001)
                .empName("张三")
                .empStatus("在职")
                .posId("P001")
                .deptCode("D001")
                .hireDate(LocalDate.of(2024, 1, 1))
                .build();
        Position position = Position.builder()
                .posId("P001")
                .posName("工程师")
                .deptCode("D001")
                .baseSalary(new BigDecimal("8700.00"))
                .build();
        PerformanceReview review = PerformanceReview.builder()
                .empId(1001)
                .grade("A")
                .reviewPeriod("2026-06")
                .build();
        Attendance late = Attendance.builder()
                .empId(1001)
                .attDate(LocalDate.of(2026, 6, 3))
                .attStatus("迟到")
                .lateMinutes(10)
                .earlyLeaveMinutes(0)
                .absent(false)
                .build();
        Attendance absent = Attendance.builder()
                .empId(1001)
                .attDate(LocalDate.of(2026, 6, 4))
                .attStatus("缺勤")
                .lateMinutes(0)
                .earlyLeaveMinutes(0)
                .absent(true)
                .build();
        LeaveRequest leave = LeaveRequest.builder()
                .empId(1001)
                .leaveType("事假")
                .startDate(LocalDate.of(2026, 6, 10))
                .endDate(LocalDate.of(2026, 6, 10))
                .leaveDays(new BigDecimal("1.00"))
                .approvalStatus("APPROVED")
                .build();
        OvertimeRecord overtime = OvertimeRecord.builder()
                .empId(1001)
                .otDate(LocalDate.of(2026, 6, 8))
                .otHours(new BigDecimal("8.0"))
                .approvalStatus("APPROVED")
                .build();

        when(positionRepository.findById("P001")).thenReturn(Optional.of(position));
        when(performanceReviewRepository.findFirstByEmpIdAndReviewPeriodOrderByReviewIdDesc(1001, "2026-06"))
                .thenReturn(Optional.of(review));
        when(attendanceRepository.findByEmpIdAndAttDateBetween(eq(1001), any(), any()))
                .thenReturn(List.of(late, absent));
        when(leaveRequestRepository.findByEmpIdAndApprovalStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq(1001), eq("APPROVED"), any(), any()))
                .thenReturn(List.of(leave));
        when(overtimeRecordRepository.findByEmpIdAndApprovalStatusAndOtDateBetween(eq(1001), eq("APPROVED"), any(), any()))
                .thenReturn(List.of(overtime));
        when(salaryRepository.findByEmpIdAndPayPeriod(1001, "2026-06")).thenReturn(Optional.empty());

        SalaryCalculationPreview preview = invokeCalculate(employee, "2026-06", YearMonth.of(2026, 6));

        assertThat(preview.getPerformanceBonus()).isEqualByComparingTo("870.00");
        assertThat(preview.getFullAttendanceBonus()).isEqualByComparingTo("0.00");
        assertThat(preview.getOvertimePay()).isEqualByComparingTo("600.00");
        assertThat(preview.getLeaveDeduction()).isEqualByComparingTo("400.00");
        assertThat(preview.getAttendanceDeduction()).isEqualByComparingTo("450.00");
        assertThat(preview.getNetPay()).isEqualByComparingTo("9320.00");
        assertThat(preview.getLateCount()).isEqualTo(1);
        assertThat(preview.getAbsenceCount()).isEqualTo(1);
    }

    @Test
    void generatedSalaryMustBeApprovedBeforePaidAndPaidSalaryIsImmutable() {
        Salary generated = Salary.builder()
                .salaryId(1)
                .empId(1001)
                .payPeriod("2026-06")
                .baseSnap(new BigDecimal("5000.00"))
                .grossTotal(new BigDecimal("5300.00"))
                .deductTotal(BigDecimal.ZERO)
                .netPay(new BigDecimal("5300.00"))
                .status("GENERATED")
                .build();
        when(salaryRepository.findById(1)).thenReturn(Optional.of(generated));
        when(salaryRepository.save(any(Salary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Salary approved = salaryService.approve(1);
        assertThat(approved.getStatus()).isEqualTo("APPROVED");
        assertThat(approved.getApprovedAt()).isNotNull();

        Salary paid = salaryService.pay(1);
        assertThat(paid.getStatus()).isEqualTo("PAID");
        assertThat(paid.getPaidAt()).isNotNull();

        assertThatThrownBy(() -> salaryService.delete(1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("已发放工资不能编辑或删除");
    }

    @Test
    void generateDoesNotOverwriteExistingUnpaidSalaryWhenDisabled() {
        Employee employee = Employee.builder()
                .empId(1001)
                .empName("张三")
                .empStatus("在职")
                .posId("P001")
                .deptCode("D001")
                .hireDate(LocalDate.of(2024, 1, 1))
                .build();
        Salary existing = Salary.builder()
                .salaryId(1)
                .empId(1001)
                .payPeriod("2026-06")
                .status("GENERATED")
                .build();
        SalaryBatchGenerateRequest request = new SalaryBatchGenerateRequest();
        request.setPayPeriod("2026-06");
        request.setOverwriteUnpaid(false);

        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any())).thenReturn(true);
        when(employeeRepository.findByEmpStatus("在职")).thenReturn(List.of(employee));
        when(salaryRepository.findByEmpIdAndPayPeriod(1001, "2026-06")).thenReturn(Optional.of(existing));

        List<Salary> result = salaryService.generateBatch(request);

        assertThat(result).isEmpty();
        verify(salaryRepository, never()).save(any(Salary.class));
    }

    private SalaryCalculationPreview invokeCalculate(Employee employee, String payPeriod, YearMonth yearMonth) throws Exception {
        Method method = SalaryServiceImpl.class.getDeclaredMethod("calculate", Employee.class, String.class, YearMonth.class);
        method.setAccessible(true);
        return (SalaryCalculationPreview) method.invoke(salaryService, employee, payPeriod, yearMonth);
    }
}
