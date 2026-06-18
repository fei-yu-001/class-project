import request from '@/utils/request'

export const getAllChanges = () => request.get('/position-changes')

export const getChangesByEmployee = (empId: number) => request.get(`/position-changes/employee/${empId}`)

export const createChange = (data: any) => request.post('/position-changes', data)

export const deleteChange = (id: number) => request.delete(`/position-changes/${id}`)
