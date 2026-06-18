import request from '@/utils/request'

// ===========================================
// 项目成员 API
// ===========================================

/** 查询某项目的所有成员 */
export const getProjectMembers = (projId: number) =>
  request.get(`/project-members/project/${projId}`)

/** 查询某员工参与的所有项目 */
export const getEmployeeProjects = (empId: number) =>
  request.get(`/project-members/employee/${empId}`)

/** 新增项目成员 */
export const addProjectMember = (data: any) =>
  request.post('/project-members', data)

/** 更新项目成员 */
export const updateProjectMember = (data: any) =>
  request.put('/project-members', data)

/** 删除项目成员 */
export const deleteProjectMember = (empId: number, projId: number) =>
  request.delete(`/project-members/${empId}/${projId}`)

/** 批量替换项目所有成员（用于贡献系数调整） */
export const replaceProjectMembers = (data: any) =>
  request.post('/project-members/batch', data)

/** 获取项目贡献系数总和 */
export const getProjectMembersSum = (projId: number) =>
  request.get(`/project-members/sum/${projId}`)
