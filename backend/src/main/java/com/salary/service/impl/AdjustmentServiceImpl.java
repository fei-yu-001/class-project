package com.salary.service.impl;

import com.salary.dto.AdjustmentRequest;
import com.salary.entity.Bonus;
import com.salary.entity.Deduction;
import com.salary.repository.BonusRepository;
import com.salary.repository.DeductionRepository;
import com.salary.repository.EmployeeRepository;
import com.salary.service.AdjustmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdjustmentServiceImpl implements AdjustmentService {

    private final BonusRepository bonusRepository;
    private final DeductionRepository deductionRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public List<Bonus> listBonuses(Integer empId, String payPeriod) {
        if (empId != null && payPeriod != null && !payPeriod.isBlank()) {
            return bonusRepository.findByEmpIdAndPayPeriod(empId, payPeriod);
        }
        if (empId != null) {
            return bonusRepository.findByEmpId(empId);
        }
        return bonusRepository.findAll();
    }

    @Override
    @Transactional
    public Bonus createBonus(AdjustmentRequest request) {
        validateEmployee(request.getEmpId());
        Bonus bonus = Bonus.builder()
                .empId(request.getEmpId())
                .payPeriod(request.getPayPeriod())
                .bonusType(request.getType())
                .preTaxAmt(request.getAmount())
                .remark(request.getRemark())
                .build();
        return bonusRepository.save(bonus);
    }

    @Override
    @Transactional
    public void deleteBonus(Integer bonusId) {
        bonusRepository.deleteById(bonusId);
    }

    @Override
    public List<Deduction> listDeductions(Integer empId, String payPeriod) {
        if (empId != null && payPeriod != null && !payPeriod.isBlank()) {
            return deductionRepository.findByEmpIdAndPayPeriod(empId, payPeriod);
        }
        if (empId != null) {
            return deductionRepository.findByEmpId(empId);
        }
        return deductionRepository.findAll();
    }

    @Override
    @Transactional
    public Deduction createDeduction(AdjustmentRequest request) {
        validateEmployee(request.getEmpId());
        Deduction deduction = Deduction.builder()
                .empId(request.getEmpId())
                .payPeriod(request.getPayPeriod())
                .deductType(request.getType())
                .deductAmt(request.getAmount())
                .remark(request.getRemark())
                .build();
        return deductionRepository.save(deduction);
    }

    @Override
    @Transactional
    public void deleteDeduction(Integer deductId) {
        deductionRepository.deleteById(deductId);
    }

    private void validateEmployee(Integer empId) {
        if (!employeeRepository.existsById(empId)) {
            throw new IllegalArgumentException("员工不存在");
        }
    }
}
