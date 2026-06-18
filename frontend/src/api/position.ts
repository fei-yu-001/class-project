import request from '@/utils/request'

export const listPositions = () => {
  return request.get('/positions/list')
}

export const createPosition = (data: { posId: string; posName: string; baseSalary: number; deptCode: string }) => {
  return request.post('/positions', data)
}

export const updatePosition = (posId: string, data: { posId: string; posName: string; baseSalary: number; deptCode: string }) => {
  return request.put(`/positions/${posId}`, data)
}

export const deletePosition = (posId: string) => {
  return request.delete(`/positions/${posId}`)
}
