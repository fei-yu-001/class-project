package com.salary.repository;

import com.salary.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Integer> {

    Optional<PaymentMethod> findByEmpId(Integer empId);

    boolean existsByEmpId(Integer empId);

    long countByPayType(String payType);
}
