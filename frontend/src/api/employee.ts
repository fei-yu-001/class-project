import request from '@/utils/request'

export const searchEmployees = (params: any) => {
  return request.get('/employees/search', { params })
}

export const createEmployee = (data: any) => {
  return request.post('/employees', data)
}

export const updateEmployee = (empId: number, data: any) => {
  return request.put(`/employees/${empId}`, data)
}

export const deleteEmployee = (empId: number) => {
  return request.delete(`/employees/${empId}`)
}

export const getEmployee = (empId: number) => {
  return request.get(`/employees/${empId}`)
}
