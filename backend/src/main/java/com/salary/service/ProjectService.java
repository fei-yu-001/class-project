package com.salary.service;

import com.salary.entity.Project;
import java.util.List;

public interface ProjectService {
    List<Project> getAllProjects();
    Project getProjectById(Integer id);
    Project createProject(Project project);
    Project updateProject(Integer id, Project project);
    void deleteProject(Integer id);
}
