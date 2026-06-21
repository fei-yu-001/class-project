package com.salary.config;

import com.salary.dto.SalaryBatchGenerateRequest;
import com.salary.entity.*;
import com.salary.repository.*;
import com.salary.service.SalaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DemoBusinessDataSeeder implements org.springframework.boot.CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final SalaryRepository salaryRepository;
    private final AnnouncementRepository announcementRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final PositionChangeRepository positionChangeRepository;
    private final PositionRepository positionRepository;
    private final PerformanceReviewRepository performanceReviewRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final OvertimeRecordRepository overtimeRecordRepository;
    private final BonusRepository bonusRepository;
    private final DeductionRepository deductionRepository;
    private final SalaryService salaryService;

    private static final Random RANDOM = new Random(42L);

    private static final String[] LEAVE_TYPES = {"事假", "病假", "年假"};
    private static final String[] PROJECT_NAMES = {
            "生辰纲数字化转型", "梁山人力资源系统", "聚义厅OA升级",
            "忠义堂数据中台", "水泊梁山官网重构", "招安政策调研"
    };
    private static final String[] PROJECT_STATUSES = {"进行中", "进行中", "进行中", "已完成", "已完成", "已立项"};
    private static final String[] PROJECT_ROLES = {"开发", "测试", "产品经理", "项目经理", "运维", "UI设计"};
    private static final String[] CHANGE_REASONS = {"入职定级", "年度晋升", "部门调整", "岗位轮换"};
    private static final String[] BONUS_TYPES = {"项目奖金", "全勤奖", "节日补贴", "绩效奖金"};
    private static final String[] DEDUCT_TYPES = {"迟到罚款", "缺勤扣款", "设备损坏赔偿"};

    @Override
    @Transactional
    public void run(String... args) {
        List<Employee> employees = employeeRepository.findAll();
        if (employees.isEmpty()) {
            log.warn("没有员工数据，跳过业务演示数据初始化");
            return;
        }

        if (attendanceRepository.count() > 0) {
            log.info("当月业务演示数据已存在，仅补充历史工资数据和公告");
            seedHistoricalSalaries(employees);
            seedAnnouncements();
            seedPositionChanges(employees);
            return;
        }

        YearMonth currentYm = YearMonth.now();
        String payPeriod = currentYm.toString();
        LocalDate today = LocalDate.now();
        LocalDate monthStart = currentYm.atDay(1);
        LocalDate monthEnd = currentYm.atEndOfMonth().isAfter(today) ? today : currentYm.atEndOfMonth();

        seedPaymentMethods(employees);
        List<Project> projects = seedProjects();
        seedProjectMembers(employees, projects);
        seedPositionChanges(employees);
        seedPerformanceReviews(employees, payPeriod);
        seedAttendance(employees, monthStart, monthEnd);
        seedLeaveRequests(employees, monthStart, monthEnd);
        seedOvertimeRecords(employees, monthStart, monthEnd);
        seedBonusesAndDeductions(employees, payPeriod);
        seedSalaries(payPeriod);
        seedHistoricalSalaries(employees);
        seedAnnouncements();

        log.info("业务演示数据初始化完成：{} 名员工", employees.size());
    }

    private void seedPaymentMethods(List<Employee> employees) {
        List<PaymentMethod> methods = new ArrayList<>();
        for (Employee emp : employees) {
            boolean bank = RANDOM.nextDouble() < 0.7;
            PaymentMethod pm = PaymentMethod.builder()
                    .empId(emp.getEmpId())
                    .payType(bank ? "BANK_CARD" : "ALIPAY")
                    .accountNo(bank ? "6222" + (1000000000L + RANDOM.nextInt(900000000)) : "138" + (10000000 + RANDOM.nextInt(90000000)))
                    .cardType(bank ? "储蓄卡" : null)
                    .build();
            methods.add(pm);
        }
        paymentMethodRepository.saveAll(methods);
        log.info("已初始化 {} 条支付方式", methods.size());
    }

    private List<Project> seedProjects() {
        if (projectRepository.count() > 0) {
            return projectRepository.findAll();
        }
        List<Project> projects = new ArrayList<>();
        for (int i = 0; i < PROJECT_NAMES.length; i++) {
            projects.add(Project.builder()
                    .projName(PROJECT_NAMES[i])
                    .projStatus(PROJECT_STATUSES[i])
                    .build());
        }
        return projectRepository.saveAll(projects);
    }

    private void seedProjectMembers(List<Employee> employees, List<Project> projects) {
        List<ProjectMember> members = new ArrayList<>();
        for (Employee emp : employees) {
            int count = 1 + RANDOM.nextInt(2);
            Set<Integer> chosen = new HashSet<>();
            while (chosen.size() < count) {
                chosen.add(projects.get(RANDOM.nextInt(projects.size())).getProjId());
            }
            for (Integer projId : chosen) {
                members.add(ProjectMember.builder()
                        .empId(emp.getEmpId())
                        .projId(projId)
                        .roleName(PROJECT_ROLES[RANDOM.nextInt(PROJECT_ROLES.length)])
                        .contribCoeff(BigDecimal.valueOf(0.6 + RANDOM.nextDouble() * 0.9).setScale(2, RoundingMode.HALF_UP))
                        .build());
            }
        }
        projectMemberRepository.saveAll(members);
        log.info("已初始化 {} 条项目成员关系", members.size());
    }

    private void seedPositionChanges(List<Employee> employees) {
        List<Position> positions = positionRepository.findAll();
        if (positions.isEmpty()) return;

        long validCount = positionChangeRepository.findAll().stream()
                .filter(pc -> pc.getOldPosId() != null && pc.getNewPosId() != null)
                .count();
        if (validCount >= 30) {
            log.info("已有 {} 条有效职位变动记录，跳过初始化", validCount);
            return;
        }

        positionChangeRepository.deleteAll();

        List<PositionChange> changes = new ArrayList<>();
        int count = Math.max(30, employees.size() / 3);
        for (int i = 0; i < count; i++) {
            Employee emp = employees.get(RANDOM.nextInt(employees.size()));
            Position oldPos = positions.stream()
                    .filter(p -> p.getPosId().equals(emp.getPosId()))
                    .findFirst()
                    .orElse(positions.get(0));
            Position newPos;
            do {
                newPos = positions.get(RANDOM.nextInt(positions.size()));
            } while (newPos.getPosId().equals(oldPos.getPosId()) && positions.size() > 1);
            changes.add(PositionChange.builder()
                    .empId(emp.getEmpId())
                    .oldPosId(oldPos.getPosId())
                    .newPosId(newPos.getPosId())
                    .changeDate(randomDate(LocalDate.now().minusMonths(6), LocalDate.now()))
                    .changeReason(CHANGE_REASONS[RANDOM.nextInt(CHANGE_REASONS.length)])
                    .build());
            emp.setPosId(newPos.getPosId());
            emp.setDeptCode(newPos.getDeptCode());
        }
        employeeRepository.saveAll(employees);
        positionChangeRepository.saveAll(changes);
        log.info("已初始化 {} 条职位变动记录", changes.size());
    }

    private void seedPerformanceReviews(List<Employee> employees, String payPeriod) {
        List<PerformanceReview> reviews = new ArrayList<>();
        for (Employee emp : employees) {
            reviews.add(PerformanceReview.builder()
                    .empId(emp.getEmpId())
                    .grade(randomGrade())
                    .reviewPeriod(payPeriod)
                    .build());
        }
        performanceReviewRepository.saveAll(reviews);
        log.info("已初始化 {} 条绩效考核记录", reviews.size());
    }

    private void seedAttendance(List<Employee> employees, LocalDate start, LocalDate end) {
        List<Attendance> attendances = new ArrayList<>();
        List<LocalDate> workdays = IntStream.rangeClosed(0, (int) (end.toEpochDay() - start.toEpochDay()))
                .mapToObj(start::plusDays)
                .filter(d -> d.getDayOfWeek().getValue() <= 5)
                .collect(Collectors.toList());

        for (Employee emp : employees) {
            int recordCount = 8 + RANDOM.nextInt(8);
            Collections.shuffle(workdays, RANDOM);
            List<LocalDate> chosen = workdays.stream().limit(recordCount).collect(Collectors.toList());
            for (LocalDate date : chosen) {
                double r = RANDOM.nextDouble();
                Attendance att;
                if (r < 0.70) {
                    att = Attendance.builder().empId(emp.getEmpId()).attDate(date).attStatus("正常").lateMinutes(0).earlyLeaveMinutes(0).absent(false).build();
                } else if (r < 0.85) {
                    att = Attendance.builder().empId(emp.getEmpId()).attDate(date).attStatus("迟到").lateMinutes(5 + RANDOM.nextInt(26)).earlyLeaveMinutes(0).absent(false).build();
                } else if (r < 0.95) {
                    att = Attendance.builder().empId(emp.getEmpId()).attDate(date).attStatus("早退").lateMinutes(0).earlyLeaveMinutes(5 + RANDOM.nextInt(26)).absent(false).build();
                } else {
                    att = Attendance.builder().empId(emp.getEmpId()).attDate(date).attStatus("缺勤").lateMinutes(0).earlyLeaveMinutes(0).absent(true).build();
                }
                attendances.add(att);
            }
        }
        attendanceRepository.saveAll(attendances);
        log.info("已初始化 {} 条考勤记录", attendances.size());
    }

    private void seedLeaveRequests(List<Employee> employees, LocalDate start, LocalDate end) {
        List<LeaveRequest> leaves = new ArrayList<>();
        for (Employee emp : employees) {
            if (RANDOM.nextDouble() >= 0.35) continue;
            LocalDate leaveStart = randomDate(start, end);
            int days = 1 + RANDOM.nextInt(3);
            leaves.add(LeaveRequest.builder()
                    .empId(emp.getEmpId())
                    .leaveType(LEAVE_TYPES[RANDOM.nextInt(LEAVE_TYPES.length)])
                    .startDate(leaveStart)
                    .endDate(leaveStart.plusDays(days - 1))
                    .leaveDays(BigDecimal.valueOf(days))
                    .approvalStatus("APPROVED")
                    .build());
        }
        leaveRequestRepository.saveAll(leaves);
        log.info("已初始化 {} 条请假记录", leaves.size());
    }

    private void seedOvertimeRecords(List<Employee> employees, LocalDate start, LocalDate end) {
        List<OvertimeRecord> overtimes = new ArrayList<>();
        for (Employee emp : employees) {
            if (RANDOM.nextDouble() >= 0.45) continue;
            int count = 1 + RANDOM.nextInt(2);
            for (int i = 0; i < count; i++) {
                overtimes.add(OvertimeRecord.builder()
                        .empId(emp.getEmpId())
                        .otHours(BigDecimal.valueOf(1 + RANDOM.nextInt(8) * 0.5).setScale(1, RoundingMode.HALF_UP))
                        .otDate(randomDate(start, end))
                        .approvalStatus("APPROVED")
                        .build());
            }
        }
        overtimeRecordRepository.saveAll(overtimes);
        log.info("已初始化 {} 条加班记录", overtimes.size());
    }

    private void seedBonusesAndDeductions(List<Employee> employees, String payPeriod) {
        List<Bonus> bonuses = new ArrayList<>();
        List<Deduction> deductions = new ArrayList<>();
        for (Employee emp : employees) {
            if (RANDOM.nextDouble() < 0.18) {
                bonuses.add(Bonus.builder()
                        .empId(emp.getEmpId())
                        .payPeriod(payPeriod)
                        .bonusType(BONUS_TYPES[RANDOM.nextInt(BONUS_TYPES.length)])
                        .preTaxAmt(BigDecimal.valueOf(100 + RANDOM.nextInt(901)).setScale(2, RoundingMode.HALF_UP))
                        .remark("演示奖金")
                        .build());
            }
            if (RANDOM.nextDouble() < 0.12) {
                deductions.add(Deduction.builder()
                        .empId(emp.getEmpId())
                        .payPeriod(payPeriod)
                        .deductType(DEDUCT_TYPES[RANDOM.nextInt(DEDUCT_TYPES.length)])
                        .deductAmt(BigDecimal.valueOf(50 + RANDOM.nextInt(251)).setScale(2, RoundingMode.HALF_UP))
                        .remark("演示扣款")
                        .build());
            }
        }
        bonusRepository.saveAll(bonuses);
        deductionRepository.saveAll(deductions);
        log.info("已初始化 {} 条奖金、{} 条罚款", bonuses.size(), deductions.size());
    }

    private void seedSalaries(String payPeriod) {
        SalaryBatchGenerateRequest request = new SalaryBatchGenerateRequest();
        request.setPayPeriod(payPeriod);
        request.setOverwriteUnpaid(true);
        List<com.salary.entity.Salary> generated = salaryService.generateBatch(request);

        // 让前 30 条工资进入已审核/已发放状态，展示完整流程
        int approveCount = Math.min(30, generated.size());
        for (int i = 0; i < approveCount; i++) {
            com.salary.entity.Salary salary = generated.get(i);
            salaryService.approve(salary.getSalaryId());
            if (i < approveCount / 2) {
                salaryService.pay(salary.getSalaryId());
            }
        }
        log.info("已生成 {} 条工资记录（{} 条已审核，{} 条已发放）",
                generated.size(), approveCount, approveCount / 2);
    }

    private void seedHistoricalSalaries(List<Employee> employees) {
        List<Position> positions = positionRepository.findAll();
        if (positions.isEmpty()) return;

        List<Salary> historical = new ArrayList<>();
        YearMonth current = YearMonth.now();
        int historyMonths = 5;

        for (int i = 1; i <= historyMonths; i++) {
            YearMonth period = current.minusMonths(i);
            String payPeriod = period.toString();
            LocalDate periodEnd = period.atEndOfMonth();

            for (Employee emp : employees) {
                if (salaryRepository.existsByEmpIdAndPayPeriod(emp.getEmpId(), payPeriod)) {
                    continue;
                }

                BigDecimal baseSalary = positions.stream()
                        .filter(p -> p.getPosId().equals(emp.getPosId()))
                        .findFirst()
                        .map(Position::getBaseSalary)
                        .orElse(BigDecimal.valueOf(5000));

                BigDecimal performanceBonus = baseSalary.multiply(
                        BigDecimal.valueOf(-0.03 + RANDOM.nextDouble() * 0.13)
                ).setScale(2, RoundingMode.HALF_UP).max(BigDecimal.ZERO);

                BigDecimal fullAttendanceBonus = RANDOM.nextDouble() < 0.55
                        ? BigDecimal.valueOf(300).setScale(2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

                BigDecimal overtimePay = BigDecimal.valueOf(RANDOM.nextInt(800) + RANDOM.nextDouble() * 200)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal extraBonus = RANDOM.nextDouble() < 0.25
                        ? BigDecimal.valueOf(200 + RANDOM.nextInt(801)).setScale(2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

                BigDecimal leaveDeduction = RANDOM.nextDouble() < 0.30
                        ? baseSalary.multiply(BigDecimal.valueOf(RANDOM.nextDouble() * 0.08)).setScale(2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                BigDecimal attendanceDeduction = BigDecimal.valueOf(RANDOM.nextInt(6) * 50L)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal extraDeduction = RANDOM.nextDouble() < 0.15
                        ? BigDecimal.valueOf(50 + RANDOM.nextInt(251)).setScale(2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

                BigDecimal grossTotal = baseSalary
                        .add(performanceBonus)
                        .add(fullAttendanceBonus)
                        .add(overtimePay)
                        .add(extraBonus)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal deductTotal = leaveDeduction
                        .add(attendanceDeduction)
                        .add(extraDeduction)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal netPay = grossTotal.subtract(deductTotal).max(BigDecimal.ZERO)
                        .setScale(2, RoundingMode.HALF_UP);

                double statusR = RANDOM.nextDouble();
                String status;
                LocalDateTime approvedAt = null;
                LocalDateTime paidAt = null;
                if (statusR < 0.45) {
                    status = "PAID";
                    approvedAt = periodEnd.minusDays(3).atTime(10, 0);
                    paidAt = periodEnd.minusDays(1).atTime(10, 0);
                } else if (statusR < 0.80) {
                    status = "APPROVED";
                    approvedAt = periodEnd.minusDays(3).atTime(10, 0);
                } else {
                    status = "GENERATED";
                }

                historical.add(Salary.builder()
                        .empId(emp.getEmpId())
                        .payPeriod(payPeriod)
                        .baseSnap(baseSalary.setScale(2, RoundingMode.HALF_UP))
                        .performanceBonus(performanceBonus)
                        .fullAttendanceBonus(fullAttendanceBonus)
                        .overtimePay(overtimePay)
                        .extraBonus(extraBonus)
                        .leaveDeduction(leaveDeduction)
                        .attendanceDeduction(attendanceDeduction)
                        .extraDeduction(extraDeduction)
                        .taxDeduction(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                        .insuranceDeduction(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                        .grossTotal(grossTotal)
                        .deductTotal(deductTotal)
                        .netPay(netPay)
                        .status(status)
                        .generatedAt(periodEnd.minusDays(10).atTime(10, 0))
                        .approvedAt(approvedAt)
                        .paidAt(paidAt)
                        .build());
            }
        }

        salaryRepository.saveAll(historical);
        log.info("已初始化 {} 条历史工资记录（覆盖过去 {} 个月）", historical.size(), historyMonths);
    }

    private void seedAnnouncements() {
        if (announcementRepository.count() > 0) return;

        List<Announcement> announcements = List.of(
                Announcement.builder()
                        .title("端午节快乐")
                        .content("值此端午佳节，公司向全体员工及家人致以最诚挚的祝福：愿您粽享安康，阖家幸福，工作顺利！")
                        .publisher("管理员")
                        .createdAt(LocalDateTime.now().minusDays(2))
                        .isTop(true)
                        .build(),
                Announcement.builder()
                        .title("关于 2026 年端午节放假安排的通知")
                        .content("根据国家法定节假日安排，2026 年端午节放假时间为 6 月 22 日至 6 月 24 日，共 3 天。请大家提前做好工作交接，注意安全出行。")
                        .publisher("管理员")
                        .createdAt(LocalDateTime.now().minusDays(5))
                        .isTop(false)
                        .build(),
                Announcement.builder()
                        .title("夏季防暑降温提示")
                        .content("近期气温持续升高，请大家注意防暑降温，合理安排外出时间，多喝水，保持良好作息。")
                        .publisher("管理员")
                        .createdAt(LocalDateTime.now().minusDays(12))
                        .isTop(false)
                        .build()
        );

        announcementRepository.saveAll(announcements);
        log.info("已初始化 {} 条系统公告", announcements.size());
    }

    private String randomGrade() {
        double r = RANDOM.nextDouble();
        if (r < 0.18) return "A";
        if (r < 0.45) return "B+";
        if (r < 0.70) return "B";
        if (r < 0.90) return "C";
        return "D";
    }

    private LocalDate randomDate(LocalDate start, LocalDate end) {
        long days = end.toEpochDay() - start.toEpochDay();
        return start.plusDays(RANDOM.nextInt((int) days + 1));
    }
}
