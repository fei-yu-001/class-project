import request from '@/utils/request'

export const listDepartments = () => {
  return request.get('/departments/list')
}

export const createDepartment = (data: { deptName: string; parentDeptCode: string; deptManagerEmp: string }) => {
  return request.post('/departments', data)
}

export const updateDepartment = (deptCode: string, data: { deptName: string; parentDeptCode: string; deptManagerEmp: string }) => {
  return request.put(`/departments/${deptCode}`, data)
}

export const deleteDepartment = (deptCode: string) => {
  return request.delete(`/departments/${deptCode}`)
}
