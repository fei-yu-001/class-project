package com.salary.service.impl;

import com.salary.dto.ProjectMembersBatchRequest;
import com.salary.entity.ProjectMember;
import com.salary.entity.ProjectMember.ProjectMemberId;
import com.salary.repository.ProjectMemberRepository;
import com.salary.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {

    /** 贡献系数总和容差（0.01 = 1%） */
    private static final BigDecimal SUM_TOLERANCE = new BigDecimal("0.01");

    private final ProjectMemberRepository repository;

    @Override
    public List<ProjectMember> findByProjId(Integer projId) {
        return repository.findByProjId(projId);
    }

    @Override
    public List<ProjectMember> findByEmpId(Integer empId) {
        return repository.findByEmpId(empId);
    }

    @Override
    @Transactional
    public ProjectMember addMember(ProjectMember member) {
        validateMember(member);
        BigDecimal newSum = sumCoeffByProjId(member.getProjId())
                .add(Optional.ofNullable(member.getContribCoeff()).orElse(BigDecimal.ZERO));
        if (newSum.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException(
                    "项目贡献系数之和不能超过 1.00，当前总和 = " + newSum.setScale(2, RoundingMode.HALF_UP));
        }
        return repository.save(member);
    }

    @Override
    @Transactional
    public ProjectMember updateMember(ProjectMember member) {
        validateMember(member);
        // 更新前先校验项目总和（包含待更新的值）
        BigDecimal currentSum = sumCoeffByProjId(member.getProjId());
        BigDecimal newSum = currentSum
                .subtract(Optional.ofNullable(findExisting(member)).map(ProjectMember::getContribCoeff).orElse(BigDecimal.ZERO))
                .add(Optional.ofNullable(member.getContribCoeff()).orElse(BigDecimal.ZERO));
        if (newSum.subtract(BigDecimal.ONE).abs().compareTo(SUM_TOLERANCE) > 0) {
            throw new IllegalArgumentException(
                    "项目贡献系数之和必须等于 1.00，当前总和 = " + newSum.setScale(2, RoundingMode.HALF_UP));
        }
        return repository.save(member);
    }

    private ProjectMember findExisting(ProjectMember member) {
        return repository.findById(new ProjectMemberId(member.getEmpId(), member.getProjId()))
                .orElse(null);
    }

    @Override
    @Transactional
    public void deleteMember(Integer empId, Integer projId) {
        repository.deleteById(new ProjectMemberId(empId, projId));
    }

    private void validateMember(ProjectMember member) {
        if (member.getEmpId() == null || member.getProjId() == null) {
            throw new IllegalArgumentException("员工ID和项目ID不能为空");
        }
        BigDecimal coeff = Optional.ofNullable(member.getContribCoeff()).orElse(BigDecimal.ZERO);
        if (coeff.compareTo(BigDecimal.ZERO) < 0 || coeff.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("贡献系数必须在 [0, 1] 范围内");
        }
    }

    /**
     * 批量替换项目的所有成员
     * 1. 校验请求中所有成员 contrib_coeff 总和 = 1.00
     * 2. 删除项目现有的所有成员
     * 3. 插入新成员列表
     * 在单个事务中完成，失败回滚
     */
    @Override
    @Transactional
    public List<ProjectMember> replaceMembers(ProjectMembersBatchRequest request) {
        Integer projId = request.getProjId();
        List<ProjectMembersBatchRequest.MemberCoeff> members = request.getMembers();

        if (members == null || members.isEmpty()) {
            throw new IllegalArgumentException("成员列表不能为空");
        }

        // 1. 应用层校验：所有 contrib_coeff 之和 = 1
        BigDecimal sum = BigDecimal.ZERO;
        for (ProjectMembersBatchRequest.MemberCoeff m : members) {
            if (m.getContribCoeff() == null) {
                throw new IllegalArgumentException("员工 " + m.getEmpId() + " 的贡献系数不能为空");
            }
            if (m.getContribCoeff().compareTo(BigDecimal.ZERO) < 0
                    || m.getContribCoeff().compareTo(BigDecimal.ONE) > 0) {
                throw new IllegalArgumentException("员工 " + m.getEmpId() + " 的贡献系数必须在 [0, 1] 范围内");
            }
            sum = sum.add(m.getContribCoeff());
        }
        BigDecimal diff = sum.subtract(BigDecimal.ONE).abs();
        if (diff.compareTo(SUM_TOLERANCE) > 0) {
            throw new IllegalArgumentException(
                    "贡献系数之和必须等于 1.00 (100%)，当前总和 = " + sum.setScale(2, RoundingMode.HALF_UP));
        }

        // 2. 删除项目现有的所有成员
        List<ProjectMember> existing = repository.findByProjId(projId);
        repository.deleteAll(existing);
        repository.flush();

        // 3. 插入新成员
        List<ProjectMember> saved = new ArrayList<>();
        for (ProjectMembersBatchRequest.MemberCoeff m : members) {
            ProjectMember pm = new ProjectMember();
            pm.setEmpId(m.getEmpId());
            pm.setProjId(projId);
            pm.setRoleName(m.getRoleName());
            pm.setContribCoeff(m.getContribCoeff());
            saved.add(repository.save(pm));
        }
        return saved;
    }

    @Override
    public BigDecimal sumCoeffByProjId(Integer projId) {
        List<ProjectMember> list = repository.findByProjId(projId);
        BigDecimal sum = BigDecimal.ZERO;
        for (ProjectMember m : list) {
            if (m.getContribCoeff() != null) {
                sum = sum.add(m.getContribCoeff());
            }
        }
        return sum.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public void validateSumEqualsOne(Integer projId) {
        BigDecimal sum = sumCoeffByProjId(projId);
        if (sum.subtract(BigDecimal.ONE).abs().compareTo(SUM_TOLERANCE) > 0) {
            throw new IllegalArgumentException(
                    "项目贡献系数之和必须等于 1.00 (100%)，当前总和 = " + sum);
        }
    }
}
