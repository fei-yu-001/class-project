package com.salary.controller;

import com.salary.common.Result;
import com.salary.entity.Project;
import com.salary.security.RequireRole;
import com.salary.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping
    public Result<List<Project>> getAll() { return Result.success(projectService.getAllProjects()); }

    @GetMapping("/{id}")
    public Result<Project> getById(@PathVariable Integer id) { return Result.success(projectService.getProjectById(id)); }

    @PostMapping
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    public Result<Project> create(@RequestBody Project project) { return Result.success(projectService.createProject(project)); }

    @PutMapping("/{id}")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    public Result<Project> update(@PathVariable Integer id, @RequestBody Project project) { return Result.success(projectService.updateProject(id, project)); }

    @DeleteMapping("/{id}")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    public Result<Void> delete(@PathVariable Integer id) { projectService.deleteProject(id); return Result.success(); }
}
