package com.salary.service.impl;

import com.salary.dto.DashboardStats;
import com.salary.repository.*;
import com.salary.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

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

    @Override
    @Cacheable(value = "dashboardStats", key = "'summary'")
    public DashboardStats getStats() {
        long totalDepts = departmentRepository.count();
        long totalPos = positionRepository.count();
        BigDecimal totalNet = salaryRepository.sumNetPay();
        String currentPeriod = YearMonth.now().toString();

        return DashboardStats.builder()
                .totalEmployees(employeeRepository.count())
                .activeEmployees(employeeRepository.countByEmpStatus("在职"))
                .totalDepartments(totalDepts)
                .totalPositions(totalPos)
                .pendingSalaries(salaryRepository.countByStatus("GENERATED"))
                .paidSalaries(salaryRepository.countByStatus("PAID"))
                .currentPeriodGrossSalary(salaryRepository.sumGrossByPayPeriod(currentPeriod))
                .currentPeriodNetSalary(salaryRepository.sumNetByPayPeriod(currentPeriod))
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
