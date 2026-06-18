package com.salary.repository;

import com.salary.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, String> {

    Optional<Position> findByPosName(String posName);

    boolean existsByPosName(String posName);

    List<Position> findByDeptCode(String deptCode);
}
