package com.salary.service.impl;

import com.salary.entity.Project;
import com.salary.repository.ProjectRepository;
import com.salary.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;

    @Override
    public List<Project> getAllProjects() { return projectRepository.findAll(); }

    @Override
    public Project getProjectById(Integer id) { return projectRepository.findById(id).orElse(null); }

    @Override
    @Transactional
    public Project createProject(Project project) { return projectRepository.save(project); }

    @Override
    @Transactional
    public Project updateProject(Integer id, Project project) {
        Project existing = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("项目不存在"));
        existing.setProjName(project.getProjName());
        existing.setProjStatus(project.getProjStatus());
        return projectRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteProject(Integer id) { projectRepository.deleteById(id); }
}
