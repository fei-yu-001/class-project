package com.salary.repository;

import com.salary.entity.ProjectMember;
import com.salary.entity.ProjectMember.ProjectMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {
    List<ProjectMember> findByEmpId(Integer empId);
    List<ProjectMember> findByProjId(Integer projId);
}
