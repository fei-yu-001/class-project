package com.salary.repository;

import com.salary.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    Page<Employee> findByEmpStatus(String empStatus, Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE " +
           "(:empName IS NULL OR e.empName LIKE %:empName%) AND " +
           "(:deptCode IS NULL OR e.deptCode = :deptCode) AND " +
           "(:empStatus IS NULL OR e.empStatus = :empStatus)")
    Page<Employee> search(@Param("empName") String empName,
                          @Param("deptCode") String deptCode,
                          @Param("empStatus") String empStatus,
                          Pageable pageable);

    List<Employee> findByDeptCode(String deptCode);

    List<Employee> findByEmpStatus(String empStatus);

    long countByEmpStatus(String empStatus);

    Optional<Employee> findByEmpName(String empName);
}
