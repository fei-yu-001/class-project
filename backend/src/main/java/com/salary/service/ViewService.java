package com.salary.service;

import com.salary.entity.*;

import java.util.List;
import java.util.Map;

public interface ViewService {

    List<EmpFullView> getEmpFullInfo();

    List<EmpFullView> getEmpFullByDept(String deptCode);

    List<SalDetailView> getSalDetails();

    List<SalDetailView> getSalDetailsByPeriod(String payPeriod);

    List<EmpPerfAttView> getEmpPerfAtt();

    List<EmpProjectView> getEmpProjects();

    List<EmpPayInfoView> getEmpPayInfo();

    List<Map<String, Object>> getMonthlyNetPayChart();

    List<Map<String, Object>> getPayTypeChart();

    List<Map<String, Object>> getGradeChart();

    Map<String, Object> getDatabaseSchema();
}