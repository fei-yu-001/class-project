package com.salary.service.impl;

import com.salary.repository.SysConfigRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 五险一金计算器
 * 计算个人应缴部分（企业部分不在工资中扣除）
 */
@Component
@RequiredArgsConstructor
public class InsuranceCalculator {

    private final SysConfigRepository sysConfigRepository;

    @Data
    public static class InsuranceBreakdown {
        private BigDecimal pension;       // 养老保险
        private BigDecimal medical;       // 医疗保险
        private BigDecimal unemployment;  // 失业保险
        private BigDecimal housing;       // 住房公积金
        private BigDecimal total;         // 合计
    }

    /**
     * 计算个人五险一金
     *
     * @param baseSalary 缴纳基数（通常等于基本工资）
     * @return 分项明细
     */
    public InsuranceBreakdown calculate(BigDecimal baseSalary) {
        BigDecimal pensionRate = getConfig("insurance_pension_rate", "0.08");
        BigDecimal medicalRate = getConfig("insurance_medical_rate", "0.02");
        BigDecimal unemploymentRate = getConfig("insurance_unemployment_rate", "0.005");
        BigDecimal housingRate = getConfig("insurance_housing_rate", "0.12");

        InsuranceBreakdown breakdown = new InsuranceBreakdown();
        breakdown.setPension(baseSalary.multiply(pensionRate).setScale(2, RoundingMode.HALF_UP));
        breakdown.setMedical(baseSalary.multiply(medicalRate).setScale(2, RoundingMode.HALF_UP));
        breakdown.setUnemployment(baseSalary.multiply(unemploymentRate).setScale(2, RoundingMode.HALF_UP));
        breakdown.setHousing(baseSalary.multiply(housingRate).setScale(2, RoundingMode.HALF_UP));
        breakdown.setTotal(breakdown.getPension()
                .add(breakdown.getMedical())
                .add(breakdown.getUnemployment())
                .add(breakdown.getHousing()));
        return breakdown;
    }

    private BigDecimal getConfig(String key, String defaultValue) {
        try {
            return sysConfigRepository.findByConfigKey(key)
                    .map(c -> new BigDecimal(c.getConfigValue()))
                    .orElse(new BigDecimal(defaultValue));
        } catch (NumberFormatException e) {
            return new BigDecimal(defaultValue);
        }
    }
}
