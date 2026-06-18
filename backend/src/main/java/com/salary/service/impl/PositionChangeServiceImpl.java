package com.salary.service.impl;

import com.salary.entity.PositionChange;
import com.salary.repository.PositionChangeRepository;
import com.salary.service.PositionChangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PositionChangeServiceImpl implements PositionChangeService {
    private final PositionChangeRepository positionChangeRepository;

    @Override
    public List<PositionChange> getAllChanges() { return positionChangeRepository.findAll(); }

    @Override
    public List<PositionChange> getChangesByEmployee(Integer empId) { return positionChangeRepository.findByEmpId(empId); }

    @Override
    @Transactional
    public PositionChange createChange(PositionChange change) { return positionChangeRepository.save(change); }

    @Override
    @Transactional
    public void deleteChange(Integer id) { positionChangeRepository.deleteById(id); }
}
