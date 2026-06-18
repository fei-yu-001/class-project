package com.salary.service.impl;

import com.salary.dto.PaymentMethodRequest;
import com.salary.entity.PaymentMethod;
import com.salary.repository.EmployeeRepository;
import com.salary.repository.PaymentMethodRepository;
import com.salary.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public PaymentMethod create(PaymentMethodRequest request) {
        if (paymentMethodRepository.existsByEmpId(request.getEmpId())) {
            throw new IllegalArgumentException("该员工已设置支付方式");
        }

        if (!employeeRepository.existsById(request.getEmpId())) {
            throw new IllegalArgumentException("员工不存在");
        }

        PaymentMethod pm = PaymentMethod.builder()
                .empId(request.getEmpId())
                .payType(request.getPayType())
                .accountNo(request.getAccountNo())
                .cardType(request.getCardType())
                .build();

        return paymentMethodRepository.save(pm);
    }

    @Override
    @Transactional
    public PaymentMethod update(Integer empId, PaymentMethodRequest request) {
        PaymentMethod pm = paymentMethodRepository.findById(empId)
                .orElseThrow(() -> new IllegalArgumentException("支付方式不存在"));

        pm.setPayType(request.getPayType());
        pm.setAccountNo(request.getAccountNo());
        pm.setCardType(request.getCardType());

        return paymentMethodRepository.save(pm);
    }

    @Override
    @Transactional
    public void delete(Integer empId) {
        if (!paymentMethodRepository.existsById(empId)) {
            throw new IllegalArgumentException("支付方式不存在");
        }
        paymentMethodRepository.deleteById(empId);
    }

    @Override
    public PaymentMethod getById(Integer empId) {
        return paymentMethodRepository.findById(empId)
                .orElseThrow(() -> new IllegalArgumentException("支付方式不存在"));
    }

    @Override
    public PaymentMethod getByEmpId(Integer empId) {
        return paymentMethodRepository.findByEmpId(empId)
                .orElseThrow(() -> new IllegalArgumentException("该员工未设置支付方式"));
    }

    @Override
    public List<PaymentMethod> listAll() {
        return paymentMethodRepository.findAll();
    }
}
