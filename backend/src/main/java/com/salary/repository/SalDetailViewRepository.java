package com.salary.repository;

import com.salary.entity.SalDetailView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SalDetailViewRepository extends JpaRepository<SalDetailView, Integer> {

    List<SalDetailView> findByPayPeriod(String payPeriod);

    @Query("SELECT COALESCE(SUM(s.netPay), 0) FROM SalDetailView s WHERE s.salaryId IS NOT NULL")
    BigDecimal sumNetPay();

    @Query("SELECT s.payPeriod, COALESCE(SUM(s.netPay), 0) FROM SalDetailView s GROUP BY s.payPeriod ORDER BY s.payPeriod")
    List<Object[]> monthlyNetPayStats();
}