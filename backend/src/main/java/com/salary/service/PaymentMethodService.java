package com.salary.service;

import com.salary.dto.PaymentMethodRequest;
import com.salary.entity.PaymentMethod;

import java.util.List;

public interface PaymentMethodService {

    PaymentMethod create(PaymentMethodRequest request);

    PaymentMethod update(Integer empId, PaymentMethodRequest request);

    void delete(Integer empId);

    PaymentMethod getById(Integer empId);

    PaymentMethod getByEmpId(Integer empId);

    List<PaymentMethod> listAll();
}
