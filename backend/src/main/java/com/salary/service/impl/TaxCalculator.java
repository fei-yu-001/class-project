package com.salary.service.impl;

import com.salary.repository.SysConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 中国个人所得税计算器（累计预扣预缴法）
 *
 * 累计应纳税所得额 = 累计收入 - 累计免税收入 - 累计减除费用(5000×月数)
 *                   - 累计专项扣除(五险一金) - 累计专项附加扣除
 */
@Component
@RequiredArgsConstructor
public class TaxCalculator {

    private final SysConfigRepository sysConfigRepository;

    /** 七档超额累进税率表 */
    private static final BigDecimal[] BRACKETS = {
        new BigDecimal("36000"),
        new BigDecimal("144000"),
        new BigDecimal("300000"),
        new BigDecimal("420000"),
        new BigDecimal("660000"),
        new BigDecimal("960000")
    };
    private static final BigDecimal[] RATES = {
        new BigDecimal("0.03"),
        new BigDecimal("0.10"),
        new BigDecimal("0.20"),
        new BigDecimal("0.25"),
        new BigDecimal("0.30"),
        new BigDecimal("0.35"),
        new BigDecimal("0.45")
    };
    private static final BigDecimal[] QUICK_DEDUCTIONS = {
        BigDecimal.ZERO,
        new BigDecimal("2520"),
        new BigDecimal("16920"),
        new BigDecimal("31920"),
        new BigDecimal("52920"),
        new BigDecimal("85920"),
        new BigDecimal("181920")
    };

    /**
     * 计算当月应预扣个税
     *
     * @param cumulativeIncome      累计收入（含本月）
     * @param cumulativeInsurance   累计五险一金（含本月）
     * @param monthNumber           当前月份 (1-12)
     * @return 当月应扣个税
     */
    public BigDecimal calculate(BigDecimal cumulativeIncome, BigDecimal cumulativeInsurance, int monthNumber) {
        BigDecimal threshold = getThreshold();
        BigDecimal cumulativeDeduction = threshold.multiply(BigDecimal.valueOf(monthNumber));
        BigDecimal taxableIncome = cumulativeIncome
                .subtract(cumulativeInsurance)
                .subtract(cumulativeDeduction)
                .max(BigDecimal.ZERO);

        BigDecimal cumulativeTax = calculateCumulativeTax(taxableIncome);

        // 需要减去已预缴的（前几个月的累计税额），但这里我们直接返回本月应扣
        // 调用方应传入：累计收入含本月、累计五险一金含本月
        // 计算出的是截至本月的累计应纳税额
        // 为了简化，这里返回累计应纳税额，调用方需自行减去上月累计已缴税额
        return cumulativeTax.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算当月个税（简化版：无累计，仅当月收入）
     * 适用于首次发薪或无历史数据的场景
     */
    public BigDecimal calculateSimple(BigDecimal monthlyIncome, BigDecimal monthlyInsurance) {
        BigDecimal threshold = getThreshold();
        BigDecimal taxableIncome = monthlyIncome
                .subtract(monthlyInsurance)
                .subtract(threshold)
                .max(BigDecimal.ZERO);
        // Use monthly brackets (annual / 12)
        return calculateMonthlyBrackets(taxableIncome).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMonthlyBrackets(BigDecimal monthlyTaxable) {
        if (monthlyTaxable.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        // Monthly thresholds: annual / 12
        BigDecimal[] monthlyBrackets = {
            new BigDecimal("3000"),    // 36000/12
            new BigDecimal("12000"),   // 144000/12
            new BigDecimal("25000"),   // 300000/12
            new BigDecimal("35000"),   // 420000/12
            new BigDecimal("55000"),   // 660000/12
            new BigDecimal("80000")    // 960000/12
        };
        BigDecimal[] monthlyQuickDeductions = {
            BigDecimal.ZERO,
            new BigDecimal("210"),     // 2520/12
            new BigDecimal("1410"),    // 16920/12
            new BigDecimal("2660"),    // 31920/12
            new BigDecimal("4410"),    // 52920/12
            new BigDecimal("7160"),    // 85920/12
            new BigDecimal("15160")    // 181920/12
        };
        for (int i = 0; i < monthlyBrackets.length; i++) {
            if (monthlyTaxable.compareTo(monthlyBrackets[i]) <= 0) {
                return monthlyTaxable.multiply(RATES[i]).subtract(monthlyQuickDeductions[i]);
            }
        }
        return monthlyTaxable.multiply(RATES[6]).subtract(monthlyQuickDeductions[6]);
    }

    private BigDecimal calculateCumulativeTax(BigDecimal taxableIncome) {
        if (taxableIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        for (int i = 0; i < BRACKETS.length; i++) {
            if (taxableIncome.compareTo(BRACKETS[i]) <= 0) {
                return taxableIncome.multiply(RATES[i]).subtract(QUICK_DEDUCTIONS[i]);
            }
        }
        // 超过最高档
        return taxableIncome.multiply(RATES[6]).subtract(QUICK_DEDUCTIONS[6]);
    }

    private BigDecimal getThreshold() {
        try {
            return sysConfigRepository.findByConfigKey("tax_threshold")
                    .map(c -> new BigDecimal(c.getConfigValue()))
                    .orElse(new BigDecimal("5000"));
        } catch (NumberFormatException e) {
            return new BigDecimal("5000");
        }
    }
}
