import request from '@/utils/request'

export const getAllProjects = () => request.get('/projects')

export const getProjectById = (id: number) => request.get(`/projects/${id}`)

export const createProject = (data: any) => request.post('/projects', data)

export const updateProject = (id: number, data: any) => request.put(`/projects/${id}`, data)

export const deleteProject = (id: number) => request.delete(`/projects/${id}`)
