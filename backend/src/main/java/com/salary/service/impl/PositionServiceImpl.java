package com.salary.service.impl;

import com.salary.dto.PositionRequest;
import com.salary.entity.Position;
import com.salary.repository.DepartmentRepository;
import com.salary.repository.PositionRepository;
import com.salary.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public Position create(PositionRequest request) {
        if (positionRepository.existsById(request.getPosId())) {
            throw new IllegalArgumentException("职位ID已存在");
        }
        if (positionRepository.existsByPosName(request.getPosName())) {
            throw new IllegalArgumentException("职位名称已存在");
        }

        if (!departmentRepository.existsById(request.getDeptCode())) {
            throw new IllegalArgumentException("部门不存在");
        }

        Position position = Position.builder()
                .posId(request.getPosId())
                .posName(request.getPosName())
                .baseSalary(request.getBaseSalary())
                .deptCode(request.getDeptCode())
                .build();
        return positionRepository.save(position);
    }

    @Override
    @Transactional
    public Position update(String posId, PositionRequest request) {
        Position position = positionRepository.findById(posId)
                .orElseThrow(() -> new IllegalArgumentException("职位不存在"));

        if (!position.getPosName().equals(request.getPosName()) && positionRepository.existsByPosName(request.getPosName())) {
            throw new IllegalArgumentException("职位名称已存在");
        }

        if (!departmentRepository.existsById(request.getDeptCode())) {
            throw new IllegalArgumentException("部门不存在");
        }

        position.setPosName(request.getPosName());
        position.setBaseSalary(request.getBaseSalary());
        position.setDeptCode(request.getDeptCode());
        return positionRepository.save(position);
    }

    @Override
    @Transactional
    public void delete(String posId) {
        if (!positionRepository.existsById(posId)) {
            throw new IllegalArgumentException("职位不存在");
        }
        positionRepository.deleteById(posId);
    }

    @Override
    public Position getById(String posId) {
        return positionRepository.findById(posId)
                .orElseThrow(() -> new IllegalArgumentException("职位不存在"));
    }

    @Override
    public List<Position> listAll() {
        return positionRepository.findAll();
    }

    @Override
    public List<Position> listByDeptCode(String deptCode) {
        return positionRepository.findByDeptCode(deptCode);
    }

    @Override
    public Page<Position> listPage(Pageable pageable) {
        return positionRepository.findAll(pageable);
    }
}
