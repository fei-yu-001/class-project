package com.salary.repository;

import com.salary.entity.EmpProjectView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpProjectViewRepository extends JpaRepository<EmpProjectView, Integer> {

    List<EmpProjectView> findByProjId(Integer projId);

    @Query("SELECT e.projName, COUNT(e) FROM EmpProjectView e GROUP BY e.projName")
    List<Object[]> projectMemberStats();
}