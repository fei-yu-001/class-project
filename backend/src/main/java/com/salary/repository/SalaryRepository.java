package com.salary.repository;

import com.salary.entity.Salary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryRepository extends JpaRepository<Salary, Integer> {

    Optional<Salary> findByEmpIdAndPayPeriod(Integer empId, String payPeriod);

    List<Salary> findByEmpId(Integer empId);

    Page<Salary> findByEmpId(Integer empId, Pageable pageable);

    Page<Salary> findByPayPeriod(String payPeriod, Pageable pageable);

    @Query("SELECT s FROM Salary s WHERE " +
           "(:empId IS NULL OR s.empId = :empId) AND " +
           "(:payPeriod IS NULL OR s.payPeriod = :payPeriod)")
    Page<Salary> search(@Param("empId") Integer empId,
                        @Param("payPeriod") String payPeriod,
                        Pageable pageable);

    boolean existsByEmpIdAndPayPeriod(Integer empId, String payPeriod);

    @Query("SELECT COALESCE(SUM(s.netPay), 0) FROM Salary s")
    BigDecimal sumNetPay();

    @Query("SELECT COALESCE(SUM(s.grossTotal), 0) FROM Salary s WHERE s.payPeriod = :payPeriod")
    BigDecimal sumGrossByPayPeriod(@Param("payPeriod") String payPeriod);

    @Query("SELECT COALESCE(SUM(s.netPay), 0) FROM Salary s WHERE s.payPeriod = :payPeriod")
    BigDecimal sumNetByPayPeriod(@Param("payPeriod") String payPeriod);

    long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(s.grossTotal), 0) FROM Salary s WHERE s.empId = :empId AND s.payPeriod < :payPeriod AND s.status = 'PAID'")
    BigDecimal sumPaidGrossBefore(@Param("empId") Integer empId, @Param("payPeriod") String payPeriod);

    @Query("SELECT COALESCE(SUM(s.insuranceDeduction), 0) FROM Salary s WHERE s.empId = :empId AND s.payPeriod < :payPeriod AND s.status = 'PAID'")
    BigDecimal sumPaidInsuranceBefore(@Param("empId") Integer empId, @Param("payPeriod") String payPeriod);
}
