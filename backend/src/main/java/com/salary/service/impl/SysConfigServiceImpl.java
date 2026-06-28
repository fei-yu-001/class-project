package com.salary.service.impl;

import com.salary.entity.SysConfig;
import com.salary.repository.SysConfigRepository;
import com.salary.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl implements SysConfigService {

    private final SysConfigRepository sysConfigRepository;

    @Override
    public List<SysConfig> getAll() {
        return sysConfigRepository.findAll();
    }

    @Override
    public SysConfig getByKey(String key) {
        return sysConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new IllegalArgumentException("配置项不存在: " + key));
    }

    @Override
    @Transactional
    public SysConfig update(String key, String value) {
        SysConfig config = sysConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new IllegalArgumentException("配置项不存在: " + key));
        config.setConfigValue(value);
        return sysConfigRepository.save(config);
    }
}
