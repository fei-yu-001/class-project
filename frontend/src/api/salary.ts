import request from '@/utils/request'

export const searchSalaries = (params: any) => {
  return request.get('/salaries/search', { params })
}

export const getSalariesByEmployee = (empId: number) => {
  return request.get(`/salaries/employee/${empId}`)
}

export const createSalary = (data: any) => {
  return request.post('/salaries', data)
}

export const updateSalary = (id: number, data: any) => {
  return request.put(`/salaries/${id}`, data)
}

export const deleteSalary = (id: number) => {
  return request.delete(`/salaries/${id}`)
}

export const previewSalaries = (payPeriod: string) => {
  return request.get('/salaries/preview', { params: { payPeriod } })
}

export const generateSalaries = (data: { payPeriod: string; empIds?: number[]; overwriteUnpaid?: boolean }) => {
  return request.post('/salaries/generate', data)
}

export const approveSalary = (id: number) => {
  return request.post(`/salaries/${id}/approve`)
}

export const paySalary = (id: number) => {
  return request.post(`/salaries/${id}/pay`)
}

export const batchApproveSalaries = (salaryIds: number[]) => {
  return request.post('/salaries/batch-approve', { salaryIds })
}

export const batchPaySalaries = (salaryIds: number[]) => {
  return request.post('/salaries/batch-pay', { salaryIds })
}
