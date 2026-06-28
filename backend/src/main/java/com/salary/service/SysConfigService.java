package com.salary.service;

import com.salary.entity.SysConfig;
import java.util.List;

public interface SysConfigService {
    List<SysConfig> getAll();
    SysConfig getByKey(String key);
    SysConfig update(String key, String value);
}
