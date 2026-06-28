package com.salary.service.impl;

import com.salary.dto.DashboardStats;
import com.salary.entity.Employee;
import com.salary.entity.PerformanceReview;
import com.salary.entity.Salary;
import com.salary.repository.*;
import com.salary.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final SalaryRepository salaryRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final SalDetailViewRepository salDetailViewRepo;
    private final EmpPayInfoViewRepository empPayInfoViewRepo;
    private final EmpPerfAttViewRepository empPerfAttViewRepo;
    private final UserRepository userRepository;
    private final PerformanceReviewRepository performanceReviewRepository;

    @Override
    public DashboardStats getStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return getAdminStats();
        } else {
            return getUserStats(auth);
        }
    }

    private DashboardStats getAdminStats() {
        long totalDepts = departmentRepository.count();
        long totalPos = positionRepository.count();
        BigDecimal totalNet = Optional.ofNullable(salaryRepository.sumNetPay()).orElse(BigDecimal.ZERO);
        String currentPeriod = YearMonth.now().toString();

        return DashboardStats.builder()
                .totalEmployees(employeeRepository.count())
                .activeEmployees(employeeRepository.countByEmpStatus("在职"))
                .totalDepartments(totalDepts)
                .totalPositions(totalPos)
                .pendingSalaries(salaryRepository.countByStatus("GENERATED"))
                .paidSalaries(salaryRepository.countByStatus("PAID"))
                .currentPeriodGrossSalary(Optional.ofNullable(salaryRepository.sumGrossByPayPeriod(currentPeriod)).orElse(BigDecimal.ZERO))
                .currentPeriodNetSalary(Optional.ofNullable(salaryRepository.sumNetByPayPeriod(currentPeriod)).orElse(BigDecimal.ZERO))
                .totalNetSalary(totalNet)
                .bankCardCount(paymentMethodRepository.countByPayType("BANK_CARD"))
                .alipayCount(paymentMethodRepository.countByPayType("ALIPAY"))
                .departmentCount(BigDecimal.valueOf(totalDepts))
                .positionCount(BigDecimal.valueOf(totalPos))
                .totalPayment(totalNet)
                .monthlyNetPayChart(monthlyNetPayFromView())
                .payTypeChart(payTypeFromView())
                .gradeChart(gradeFromView())
                .build();
    }

    private DashboardStats getUserStats(Authentication auth) {
        Integer userId = (auth != null && auth.getPrincipal() instanceof Integer id) ? id : null;

        String myName = "";
        BigDecimal myBaseSalary = BigDecimal.ZERO;
        BigDecimal myLatestNetPay = BigDecimal.ZERO;
        String myPerformanceGrade = null;
        BigDecimal myTotalBonus = BigDecimal.ZERO;
        BigDecimal myTotalDeduction = BigDecimal.ZERO;

        if (userId != null) {
            var userOpt = userRepository.findById(userId);
            if (userOpt.isPresent() && userOpt.get().getEmpId() != null) {
                Integer empId = userOpt.get().getEmpId();
                var empOpt = employeeRepository.findById(empId);
                if (empOpt.isPresent()) {
                    Employee emp = empOpt.get();
                    myName = emp.getEmpName();

                    var posOpt = positionRepository.findById(emp.getPosId());
                    if (posOpt.isPresent()) {
                        myBaseSalary = posOpt.get().getBaseSalary();
                    }

                    var salaries = salaryRepository.findByEmpId(empId);
                    salaries.sort((a, b) -> b.getPayPeriod().compareTo(a.getPayPeriod()));
                    if (!salaries.isEmpty()) {
                        Salary latest = salaries.get(0);
                        myLatestNetPay = Optional.ofNullable(latest.getNetPay()).orElse(BigDecimal.ZERO);
                        myTotalBonus = Optional.ofNullable(latest.getPerformanceBonus()).orElse(BigDecimal.ZERO)
                                .add(Optional.ofNullable(latest.getFullAttendanceBonus()).orElse(BigDecimal.ZERO))
                                .add(Optional.ofNullable(latest.getOvertimePay()).orElse(BigDecimal.ZERO))
                                .add(Optional.ofNullable(latest.getExtraBonus()).orElse(BigDecimal.ZERO));
                        myTotalDeduction = Optional.ofNullable(latest.getDeductTotal()).orElse(BigDecimal.ZERO);
                    }

                    var reviews = performanceReviewRepository.findByEmpId(empId);
                    reviews.sort((a, b) -> b.getReviewPeriod().compareTo(a.getReviewPeriod()));
                    if (!reviews.isEmpty()) {
                        myPerformanceGrade = reviews.get(0).getGrade();
                    }
                }
            }
        }

        return DashboardStats.builder()
                .totalEmployees(0)
                .activeEmployees(0)
                .totalDepartments(0)
                .totalPositions(0)
                .pendingSalaries(0)
                .paidSalaries(0)
                .currentPeriodGrossSalary(BigDecimal.ZERO)
                .currentPeriodNetSalary(BigDecimal.ZERO)
                .totalNetSalary(BigDecimal.ZERO)
                .bankCardCount(0)
                .alipayCount(0)
                .departmentCount(BigDecimal.ZERO)
                .positionCount(BigDecimal.ZERO)
                .totalPayment(BigDecimal.ZERO)
                .monthlyNetPayChart(List.of())
                .payTypeChart(List.of())
                .gradeChart(List.of())
                .myName(myName)
                .myBaseSalary(myBaseSalary.setScale(2, RoundingMode.HALF_UP))
                .myLatestNetPay(myLatestNetPay.setScale(2, RoundingMode.HALF_UP))
                .myPerformanceGrade(myPerformanceGrade)
                .myTotalBonus(myTotalBonus.setScale(2, RoundingMode.HALF_UP))
                .myTotalDeduction(myTotalDeduction.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    private List<Map<String, Object>> monthlyNetPayFromView() {
        return salDetailViewRepo.monthlyNetPayStats().stream().map(row -> {
            Object val = row[1];
            double netPay = val instanceof BigDecimal ? ((BigDecimal) val).doubleValue() : ((Number) val).doubleValue();
            return Map.<String, Object>of("period", row[0], "netPay", netPay);
        }).toList();
    }

    private List<Map<String, Object>> payTypeFromView() {
        return empPayInfoViewRepo.payTypeDistribution().stream().map(row ->
                Map.<String, Object>of("type", row[0], "count", ((Number) row[1]).intValue())
        ).toList();
    }

    private List<Map<String, Object>> gradeFromView() {
        return empPerfAttViewRepo.gradeDistribution().stream().map(row ->
                Map.<String, Object>of("grade", row[0], "count", ((Number) row[1]).intValue())
        ).toList();
    }
}
