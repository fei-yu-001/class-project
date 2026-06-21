package com.salary.service;

import com.salary.dto.AdjustmentRequest;
import com.salary.entity.Bonus;
import com.salary.entity.Deduction;

import java.util.List;

public interface AdjustmentService {

    List<Bonus> listBonuses(Integer empId, String payPeriod);

    Bonus createBonus(AdjustmentRequest request);

    void deleteBonus(Integer bonusId);

    List<Deduction> listDeductions(Integer empId, String payPeriod);

    Deduction createDeduction(AdjustmentRequest request);

    void deleteDeduction(Integer deductId);
}
