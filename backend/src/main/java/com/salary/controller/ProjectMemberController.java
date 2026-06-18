package com.salary.controller;

import com.salary.common.Result;
import com.salary.dto.ProjectMembersBatchRequest;
import com.salary.entity.ProjectMember;
import com.salary.security.RequireRole;
import com.salary.service.ProjectMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/project-members")
@RequiredArgsConstructor
public class ProjectMemberController {

    private final ProjectMemberService service;

    /** 查询某项目的所有成员 */
    @GetMapping("/project/{projId}")
    public Result<List<ProjectMember>> getByProject(@PathVariable Integer projId) {
        return Result.success(service.findByProjId(projId));
    }

    /** 查询某员工参与的所有项目 */
    @GetMapping("/employee/{empId}")
    public Result<List<ProjectMember>> getByEmployee(@PathVariable Integer empId) {
        return Result.success(service.findByEmpId(empId));
    }

    /** 新增项目成员 */
    @PostMapping
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    public Result<ProjectMember> add(@RequestBody ProjectMember member) {
        return Result.success(service.addMember(member));
    }

    /** 更新单个项目成员 */
    @PutMapping
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    public Result<ProjectMember> update(@RequestBody ProjectMember member) {
        return Result.success(service.updateMember(member));
    }

    /** 删除项目成员 */
    @DeleteMapping("/{empId}/{projId}")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    public Result<Void> delete(@PathVariable Integer empId, @PathVariable Integer projId) {
        service.deleteMember(empId, projId);
        return Result.success(null);
    }

    /**
     * 批量替换项目所有成员（用于贡献系数调整）
     * 校验所有成员 contrib_coeff 总和 = 1.00
     */
    @PostMapping("/batch")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    public Result<List<ProjectMember>> replaceMembers(@Valid @RequestBody ProjectMembersBatchRequest request) {
        return Result.success(service.replaceMembers(request));
    }

    /** 获取项目当前贡献系数总和 */
    @GetMapping("/sum/{projId}")
    public Result<Map<String, Object>> getCoeffSum(@PathVariable Integer projId) {
        BigDecimal sum = service.sumCoeffByProjId(projId);
        Map<String, Object> data = new HashMap<>();
        data.put("projId", projId);
        data.put("sum", sum);
        data.put("valid", sum.subtract(BigDecimal.ONE).abs().compareTo(new BigDecimal("0.01")) <= 0);
        return Result.success(data);
    }
}
