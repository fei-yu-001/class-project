package com.salary.repository;

import com.salary.entity.EmpPayInfoView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpPayInfoViewRepository extends JpaRepository<EmpPayInfoView, Integer> {

    List<EmpPayInfoView> findByPayType(String payType);

    @Query("SELECT e.payType, COUNT(e) FROM EmpPayInfoView e GROUP BY e.payType")
    List<Object[]> payTypeDistribution();
}