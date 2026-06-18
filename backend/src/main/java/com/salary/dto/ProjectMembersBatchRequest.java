package com.salary.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 批量更新项目成员贡献系数请求
 * 用于一次性提交整个项目所有成员的贡献系数
 * 前端校验所有系数之和 = 1.00 后再提交
 */
@Data
public class ProjectMembersBatchRequest {

    /** 项目 ID */
    @NotNull(message = "项目ID不能为空")
    private Integer projId;

    /** 成员列表（每个成员的贡献系数） */
    @NotNull(message = "成员列表不能为空")
    private List<MemberCoeff> members;

    @Data
    public static class MemberCoeff {
        private Integer empId;
        private String roleName;
        private BigDecimal contribCoeff;
    }
}
