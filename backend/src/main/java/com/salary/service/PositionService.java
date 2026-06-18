package com.salary.service;

import com.salary.dto.PositionRequest;
import com.salary.entity.Position;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PositionService {

    Position create(PositionRequest request);

    Position update(String posId, PositionRequest request);

    void delete(String posId);

    Position getById(String posId);

    List<Position> listAll();

    List<Position> listByDeptCode(String deptCode);

    Page<Position> listPage(Pageable pageable);
}
