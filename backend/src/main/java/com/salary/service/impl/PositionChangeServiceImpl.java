package com.salary.service.impl;

import com.salary.entity.Employee;
import com.salary.entity.Position;
import com.salary.entity.PositionChange;
import com.salary.repository.EmployeeRepository;
import com.salary.repository.PositionChangeRepository;
import com.salary.repository.PositionRepository;
import com.salary.service.PositionChangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PositionChangeServiceImpl implements PositionChangeService {
    private final PositionChangeRepository positionChangeRepository;
    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;

    @Override
    public List<PositionChange> getAllChanges() { return positionChangeRepository.findAll(); }

    @Override
    public List<PositionChange> getChangesByEmployee(Integer empId) { return positionChangeRepository.findByEmpId(empId); }

    @Override
    @Transactional
    public PositionChange createChange(PositionChange change) {
        Employee employee = employeeRepository.findById(change.getEmpId())
                .orElseThrow(() -> new IllegalArgumentException("员工不存在"));
        Position newPosition = positionRepository.findById(change.getNewPosId())
                .orElseThrow(() -> new IllegalArgumentException("目标职位不存在"));

        change.setOldPosId(employee.getPosId());
        change.setChangeDate(change.getChangeDate() != null ? change.getChangeDate() : java.time.LocalDate.now());

        employee.setPosId(newPosition.getPosId());
        employee.setDeptCode(newPosition.getDeptCode());
        employeeRepository.save(employee);

        return positionChangeRepository.save(change);
    }

    @Override
    @Transactional
    public void deleteChange(Integer id) { positionChangeRepository.deleteById(id); }
}
