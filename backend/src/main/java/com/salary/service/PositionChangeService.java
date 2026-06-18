package com.salary.service;

import com.salary.entity.PositionChange;
import java.util.List;

public interface PositionChangeService {
    List<PositionChange> getAllChanges();
    List<PositionChange> getChangesByEmployee(Integer empId);
    PositionChange createChange(PositionChange change);
    void deleteChange(Integer id);
}
