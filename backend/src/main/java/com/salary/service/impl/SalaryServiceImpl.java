package com.salary.service.impl;

import com.salary.dto.SalaryBatchGenerateRequest;
import com.salary.dto.SalaryCalculationPreview;
import com.salary.dto.SalaryRequest;
import com.salary.entity.Attendance;
import com.salary.entity.Employee;
import com.salary.entity.LeaveRequest;
import com.salary.entity.OvertimeRecord;
import com.salary.entity.PerformanceReview;
import com.salary.entity.Position;
import com.salary.entity.Salary;
import com.salary.repository.AttendanceRepository;
import com.salary.repository.EmployeeRepository;
import com.salary.repository.LeaveRequestRepository;
import com.salary.repository.OvertimeRecordRepository;
import com.salary.repository.PerformanceReviewRepository;
import com.salary.repository.PositionRepository;
import com.salary.repository.SalaryRepository;
import com.salary.service.SalaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalaryServiceImpl implements SalaryService {

    private static final BigDecimal STANDARD_WORK_DAYS = new BigDecimal("21.75");
    private static final BigDecimal STANDARD_WORK_HOURS_PER_DAY = new BigDecimal("8");
    private static final BigDecimal OVERTIME_RATE = new BigDecimal("1.5");
    private static final BigDecimal FULL_ATTENDANCE_BONUS = new BigDecimal("300");
    private static final BigDecimal ATTENDANCE_PENALTY_PER_EVENT = new BigDecimal("50");
    private static final DateTimeFormatter PAY_PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final SalaryRepository salaryRepository;
    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final OvertimeRecordRepository overtimeRecordRepository;
    private final PerformanceReviewRepository performanceReviewRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final CacheManager cacheManager;

    @Override
    @Transactional
    public Salary create(SalaryRequest request) {
        if (salaryRepository.existsByEmpIdAndPayPeriod(request.getEmpId(), request.getPayPeriod())) {
            throw new IllegalArgumentException("该员工本周期工资记录已存在");
        }

        if (!employeeRepository.existsById(request.getEmpId())) {
            throw new IllegalArgumentException("员工不存在");
        }

        Salary salary = Salary.builder()
                .empId(request.getEmpId())
                .payPeriod(request.getPayPeriod())
                .baseSnap(request.getBaseSnap() != null ? request.getBaseSnap() : BigDecimal.ZERO)
                .grossTotal(request.getGrossTotal() != null ? request.getGrossTotal() : BigDecimal.ZERO)
                .deductTotal(request.getDeductTotal() != null ? request.getDeductTotal() : BigDecimal.ZERO)
                .netPay(request.getNetPay() != null ? request.getNetPay() : BigDecimal.ZERO)
                .performanceBonus(BigDecimal.ZERO)
                .fullAttendanceBonus(BigDecimal.ZERO)
                .overtimePay(BigDecimal.ZERO)
                .leaveDeduction(BigDecimal.ZERO)
                .attendanceDeduction(BigDecimal.ZERO)
                .taxDeduction(BigDecimal.ZERO)
                .insuranceDeduction(BigDecimal.ZERO)
                .status(request.getStatus() != null ? request.getStatus() : "GENERATED")
                .build();

        Salary saved = salaryRepository.save(salary);
        evictDashboardCache();
        return saved;
    }

    @Override
    @Transactional
    public Salary update(Integer salaryId, SalaryRequest request) {
        Salary salary = salaryRepository.findById(salaryId)
                .orElseThrow(() -> new IllegalArgumentException("工资记录不存在"));
        ensureNotPaid(salary);

        if (!employeeRepository.existsById(request.getEmpId())) {
            throw new IllegalArgumentException("员工不存在");
        }

        salary.setEmpId(request.getEmpId());
        salary.setPayPeriod(request.getPayPeriod());
        salary.setBaseSnap(request.getBaseSnap() != null ? request.getBaseSnap() : BigDecimal.ZERO);
        salary.setGrossTotal(request.getGrossTotal() != null ? request.getGrossTotal() : BigDecimal.ZERO);
        salary.setDeductTotal(request.getDeductTotal() != null ? request.getDeductTotal() : BigDecimal.ZERO);
        salary.setNetPay(request.getNetPay() != null ? request.getNetPay() : BigDecimal.ZERO);
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            salary.setStatus(request.getStatus());
        }

        Salary saved = salaryRepository.save(salary);
        evictDashboardCache();
        return saved;
    }

    @Override
    @Transactional
    public void delete(Integer salaryId) {
        Salary salary = salaryRepository.findById(salaryId)
                .orElseThrow(() -> new IllegalArgumentException("工资记录不存在"));
        ensureNotPaid(salary);
        salaryRepository.delete(salary);
        evictDashboardCache();
    }

    @Override
    public Salary getById(Integer salaryId) {
        return salaryRepository.findById(salaryId)
                .orElseThrow(() -> new IllegalArgumentException("工资记录不存在"));
    }

    @Override
    public Page<Salary> search(Integer empId, String payPeriod, Pageable pageable) {
        return salaryRepository.search(empId, payPeriod, pageable);
    }

    @Override
    public List<SalaryCalculationPreview> preview(String payPeriod) {
        YearMonth yearMonth = parsePayPeriod(payPeriod);
        return employeeRepository.findByEmpStatus("在职").stream()
                .map(employee -> calculate(employee, payPeriod, yearMonth))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<Salary> generateBatch(SalaryBatchGenerateRequest request) {
        String payPeriod = normalizePayPeriod(request.getPayPeriod());
        YearMonth yearMonth = parsePayPeriod(payPeriod);
        String lockKey = "salary:generate:lock:" + payPeriod;
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", 60, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)) {
            throw new IllegalArgumentException("该计薪周期正在生成工资，请稍后再试");
        }

        try {
            List<Employee> employees = resolveEmployees(request.getEmpIds());
            List<Salary> saved = employees.stream()
                    .map(employee -> upsertGeneratedSalary(employee, payPeriod, yearMonth, request.isOverwriteUnpaid()))
                    .flatMap(Optional::stream)
                    .collect(Collectors.toList());
            evictDashboardCache();
            return saved;
        } finally {
            stringRedisTemplate.delete(lockKey);
        }
    }

    private Optional<Salary> upsertGeneratedSalary(
            Employee employee, String payPeriod, YearMonth yearMonth, boolean overwriteUnpaid) {
        Optional<Salary> existing = salaryRepository.findByEmpIdAndPayPeriod(employee.getEmpId(), payPeriod);
        if (existing.isPresent()) {
            Salary salary = existing.get();
            if ("PAID".equals(salary.getStatus())) {
                throw new IllegalArgumentException("已发放工资不能重新生成");
            }
            if (!"GENERATED".equals(salary.getStatus()) && !overwriteUnpaid) {
                return Optional.empty();
            }
            if (!overwriteUnpaid) {
                return Optional.empty();
            }
            SalaryCalculationPreview preview = calculate(employee, payPeriod, yearMonth);
            applyPreview(salary, preview);
            return Optional.of(salaryRepository.save(salary));
        }

        SalaryCalculationPreview preview = calculate(employee, payPeriod, yearMonth);
        Salary salary = Salary.builder()
                .empId(employee.getEmpId())
                .payPeriod(payPeriod)
                .status("GENERATED")
                .build();
        applyPreview(salary, preview);
        return Optional.of(salaryRepository.save(salary));
    }

    private SalaryCalculationPreview calculate(Employee employee, String payPeriod, YearMonth yearMonth) {
        BigDecimal baseSalary = positionRepository.findById(employee.getPosId())
                .map(Position::getBaseSalary)
                .orElse(BigDecimal.ZERO);

        BigDecimal dailySalary = divide(baseSalary, STANDARD_WORK_DAYS);
        BigDecimal hourlySalary = divide(dailySalary, STANDARD_WORK_HOURS_PER_DAY);
        LocalDate periodStart = yearMonth.atDay(1);
        LocalDate periodEnd = yearMonth.atEndOfMonth();

        String performanceGrade = performanceReviewRepository
                .findFirstByEmpIdAndReviewPeriodOrderByReviewIdDesc(employee.getEmpId(), payPeriod)
                .or(() -> performanceReviewRepository.findByEmpId(employee.getEmpId()).stream()
                        .reduce((first, second) -> second))
                .map(PerformanceReview::getGrade)
                .orElse(null);
        BigDecimal performanceBonus = baseSalary.multiply(performanceMultiplier(performanceGrade));

        List<Attendance> attendances = attendanceRepository
                .findByEmpIdAndAttDateBetween(employee.getEmpId(), periodStart, periodEnd);
        long lateCount = attendances.stream().filter(this::isLate).count();
        long earlyLeaveCount = attendances.stream().filter(this::isEarlyLeave).count();
        long absenceCount = attendances.stream().filter(this::isAbsent).count();

        List<LeaveRequest> leaves = leaveRequestRepository
                .findByEmpIdAndApprovalStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        employee.getEmpId(), "APPROVED", periodEnd, periodStart);
        BigDecimal approvedLeaveDays = leaves.stream()
                .map(LeaveRequest::getLeaveDays)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal approvedOvertimeHours = overtimeRecordRepository
                .findByEmpIdAndApprovalStatusAndOtDateBetween(employee.getEmpId(), "APPROVED", periodStart, periodEnd)
                .stream()
                .map(OvertimeRecord::getOtHours)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal overtimePay = hourlySalary.multiply(approvedOvertimeHours).multiply(OVERTIME_RATE);
        BigDecimal leaveDeduction = dailySalary.multiply(approvedLeaveDays);
        BigDecimal absenceDeduction = dailySalary.multiply(BigDecimal.valueOf(absenceCount));
        BigDecimal attendanceEventDeduction = ATTENDANCE_PENALTY_PER_EVENT
                .multiply(BigDecimal.valueOf(lateCount + earlyLeaveCount));
        BigDecimal attendanceDeduction = absenceDeduction.add(attendanceEventDeduction);
        boolean fullAttendance = absenceCount == 0
                && lateCount == 0
                && earlyLeaveCount == 0
                && approvedLeaveDays.compareTo(BigDecimal.ZERO) == 0;
        BigDecimal fullAttendanceBonus = fullAttendance ? FULL_ATTENDANCE_BONUS : BigDecimal.ZERO;

        BigDecimal grossTotal = baseSalary
                .add(performanceBonus)
                .add(fullAttendanceBonus)
                .add(overtimePay)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal deductTotal = leaveDeduction.add(attendanceDeduction).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netPay = grossTotal.subtract(deductTotal).max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        Optional<Salary> existingSalary = salaryRepository.findByEmpIdAndPayPeriod(employee.getEmpId(), payPeriod);

        return SalaryCalculationPreview.builder()
                .empId(employee.getEmpId())
                .empName(employee.getEmpName())
                .payPeriod(payPeriod)
                .baseSalary(baseSalary.setScale(2, RoundingMode.HALF_UP))
                .performanceBonus(performanceBonus.setScale(2, RoundingMode.HALF_UP))
                .fullAttendanceBonus(fullAttendanceBonus.setScale(2, RoundingMode.HALF_UP))
                .overtimePay(overtimePay.setScale(2, RoundingMode.HALF_UP))
                .attendanceDeduction(attendanceDeduction.setScale(2, RoundingMode.HALF_UP))
                .leaveDeduction(leaveDeduction.setScale(2, RoundingMode.HALF_UP))
                .grossTotal(grossTotal)
                .deductTotal(deductTotal)
                .netPay(netPay)
                .performanceGrade(performanceGrade)
                .lateCount(lateCount)
                .earlyLeaveCount(earlyLeaveCount)
                .absenceCount(absenceCount)
                .approvedLeaveDays(approvedLeaveDays.setScale(2, RoundingMode.HALF_UP))
                .approvedOvertimeHours(approvedOvertimeHours.setScale(1, RoundingMode.HALF_UP))
                .generated(existingSalary.isPresent())
                .status(existingSalary.map(Salary::getStatus).orElse(null))
                .build();
    }

    @Override
    @Transactional
    public Salary approve(Integer salaryId) {
        Salary salary = salaryRepository.findById(salaryId)
                .orElseThrow(() -> new IllegalArgumentException("工资记录不存在"));
        if ("PAID".equals(salary.getStatus())) {
            throw new IllegalArgumentException("已发放工资不能重复审核");
        }
        if (!"GENERATED".equals(salary.getStatus())) {
            throw new IllegalArgumentException("只有已生成工资可以审核");
        }
        salary.setStatus("APPROVED");
        salary.setApprovedAt(LocalDateTime.now());
        Salary saved = salaryRepository.save(salary);
        evictDashboardCache();
        return saved;
    }

    @Override
    @Transactional
    public Salary pay(Integer salaryId) {
        Salary salary = salaryRepository.findById(salaryId)
                .orElseThrow(() -> new IllegalArgumentException("工资记录不存在"));
        if (!"APPROVED".equals(salary.getStatus())) {
            throw new IllegalArgumentException("工资需先审核后发放");
        }
        salary.setStatus("PAID");
        salary.setPaidAt(LocalDateTime.now());
        Salary saved = salaryRepository.save(salary);
        evictDashboardCache();
        return saved;
    }

    private void applyPreview(Salary salary, SalaryCalculationPreview preview) {
        salary.setBaseSnap(preview.getBaseSalary());
        salary.setPerformanceBonus(preview.getPerformanceBonus());
        salary.setFullAttendanceBonus(preview.getFullAttendanceBonus());
        salary.setOvertimePay(preview.getOvertimePay());
        salary.setLeaveDeduction(preview.getLeaveDeduction());
        salary.setAttendanceDeduction(preview.getAttendanceDeduction());
        salary.setTaxDeduction(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        salary.setInsuranceDeduction(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        salary.setGrossTotal(preview.getGrossTotal());
        salary.setDeductTotal(preview.getDeductTotal());
        salary.setNetPay(preview.getNetPay());
        salary.setStatus("GENERATED");
        salary.setGeneratedAt(LocalDateTime.now());
        salary.setApprovedAt(null);
        salary.setPaidAt(null);
    }

    private boolean isLate(Attendance attendance) {
        return (attendance.getLateMinutes() != null && attendance.getLateMinutes() > 0)
                || "迟到".equals(attendance.getAttStatus());
    }

    private boolean isEarlyLeave(Attendance attendance) {
        return (attendance.getEarlyLeaveMinutes() != null && attendance.getEarlyLeaveMinutes() > 0)
                || "早退".equals(attendance.getAttStatus());
    }

    private boolean isAbsent(Attendance attendance) {
        return Boolean.TRUE.equals(attendance.getAbsent()) || "缺勤".equals(attendance.getAttStatus());
    }

    private List<Employee> resolveEmployees(List<Integer> empIds) {
        if (empIds == null || empIds.isEmpty()) {
            return employeeRepository.findByEmpStatus("在职");
        }
        return empIds.stream()
                .map(empId -> employeeRepository.findById(empId)
                        .orElseThrow(() -> new IllegalArgumentException("员工不存在: " + empId)))
                .collect(Collectors.toList());
    }

    private BigDecimal performanceMultiplier(String grade) {
        if (grade == null) {
            return BigDecimal.ZERO;
        }
        return switch (grade.toUpperCase()) {
            case "A+", "A" -> new BigDecimal("0.10");
            case "A-" -> new BigDecimal("0.08");
            case "B+", "B" -> new BigDecimal("0.05");
            case "C" -> BigDecimal.ZERO;
            case "D" -> new BigDecimal("-0.03");
            default -> BigDecimal.ZERO;
        };
    }

    private YearMonth parsePayPeriod(String payPeriod) {
        try {
            return YearMonth.parse(normalizePayPeriod(payPeriod), PAY_PERIOD_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("计薪周期格式应为 yyyy-MM");
        }
    }

    private String normalizePayPeriod(String payPeriod) {
        if (payPeriod == null || payPeriod.trim().isEmpty()) {
            throw new IllegalArgumentException("计薪周期不能为空");
        }
        return payPeriod.trim();
    }

    private BigDecimal divide(BigDecimal value, BigDecimal divisor) {
        return value.divide(divisor, 8, RoundingMode.HALF_UP);
    }

    private void ensureNotPaid(Salary salary) {
        if ("PAID".equals(salary.getStatus())) {
            throw new IllegalArgumentException("已发放工资不能编辑或删除");
        }
    }

    private void evictDashboardCache() {
        if (cacheManager.getCache("dashboardStats") != null) {
            cacheManager.getCache("dashboardStats").clear();
        }
    }
}
