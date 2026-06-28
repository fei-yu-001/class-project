package com.salary.repository;

import com.salary.entity.SysConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SysConfigRepository extends JpaRepository<SysConfig, String> {

    Optional<SysConfig> findByConfigKey(String configKey);
}
