import request from '@/utils/request'

export const listUsers = () => request.get('/admin/users')

export const updateUserRole = (id: number, role: string) => 
  request.put(`/admin/users/${id}/role?role=${role}`)

export const updateUserStatus = (id: number, enabled: boolean) => 
  request.put(`/admin/users/${id}/status?enabled=${enabled}`)
