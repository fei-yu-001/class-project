import request from '@/utils/request'

export const listBonuses = (params: { empId?: number; payPeriod?: string }) => {
  return request.get('/adjustments/bonuses', { params })
}

export const createBonus = (data: any) => {
  return request.post('/adjustments/bonuses', data)
}

export const deleteBonus = (bonusId: number) => {
  return request.delete(`/adjustments/bonuses/${bonusId}`)
}

export const listDeductions = (params: { empId?: number; payPeriod?: string }) => {
  return request.get('/adjustments/deductions', { params })
}

export const createDeduction = (data: any) => {
  return request.post('/adjustments/deductions', data)
}

export const deleteDeduction = (deductId: number) => {
  return request.delete(`/adjustments/deductions/${deductId}`)
}
