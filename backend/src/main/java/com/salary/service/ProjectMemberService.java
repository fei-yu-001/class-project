package com.salary.service;

import com.salary.entity.ProjectMember;
import com.salary.dto.ProjectMembersBatchRequest;

import java.math.BigDecimal;
import java.util.List;

public interface ProjectMemberService {

    /** 查询某项目的所有成员 */
    List<ProjectMember> findByProjId(Integer projId);

    /** 查询某员工参与的所有项目 */
    List<ProjectMember> findByEmpId(Integer empId);

    /** 新增项目成员 */
    ProjectMember addMember(ProjectMember member);

    /** 更新单个项目成员 */
    ProjectMember updateMember(ProjectMember member);

    /** 删除项目成员 */
    void deleteMember(Integer empId, Integer projId);

    /** 批量替换项目的所有成员（用于贡献系数调整） */
    List<ProjectMember> replaceMembers(ProjectMembersBatchRequest request);

    /** 校验某项目所有成员贡献系数总和 */
    BigDecimal sumCoeffByProjId(Integer projId);

    /** 校验某项目贡献系数总和是否等于 1.00 (允许容差 0.01) */
    void validateSumEqualsOne(Integer projId);
}
