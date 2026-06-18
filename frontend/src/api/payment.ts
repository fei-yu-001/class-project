import request from '@/utils/request'

export const listPaymentMethods = () => {
  return request.get('/payment-methods/list')
}

export const createPaymentMethod = (data: any) => {
  return request.post('/payment-methods', data)
}

export const updatePaymentMethod = (empId: number, data: any) => {
  return request.put(`/payment-methods/${empId}`, data)
}

export const deletePaymentMethod = (empId: number) => {
  return request.delete(`/payment-methods/${empId}`)
}

export const getPaymentByEmployee = (employeeId: number) => {
  return request.get(`/payment-methods/employee/${employeeId}`)
}
