import request from '@/utils/request'

export const getAllAttendance = () => request.get('/attendance/records')

export const getAttendanceByEmployee = (empId: number) => request.get(`/attendance/records/employee/${empId}`)

export const createAttendance = (data: any) => request.post('/attendance/records', data)

export const updateAttendance = (id: number, data: any) => request.put(`/attendance/records/${id}`, data)

export const deleteAttendance = (id: number) => request.delete(`/attendance/records/${id}`)

export const getAllLeaves = () => request.get('/attendance/leaves')

export const getLeavesByEmployee = (empId: number) => request.get(`/attendance/leaves/employee/${empId}`)

export const createLeave = (data: any) => request.post('/attendance/leaves', data)

export const updateLeave = (id: number, data: any) => request.put(`/attendance/leaves/${id}`, data)

export const deleteLeave = (id: number) => request.delete(`/attendance/leaves/${id}`)

export const approveLeave = (id: number) => request.put(`/attendance/leaves/${id}/approve`)

export const rejectLeave = (id: number) => request.put(`/attendance/leaves/${id}/reject`)

export const getAllOvertime = () => request.get('/attendance/overtime')

export const getOvertimeByEmployee = (empId: number) => request.get(`/attendance/overtime/employee/${empId}`)

export const createOvertime = (data: any) => request.post('/attendance/overtime', data)

export const updateOvertime = (id: number, data: any) => request.put(`/attendance/overtime/${id}`, data)

export const deleteOvertime = (id: number) => request.delete(`/attendance/overtime/${id}`)

export const approveOvertime = (id: number) =>
  request.put(`/attendance/overtime/${id}/approve`)

export const rejectOvertime = (id: number) =>
  request.put(`/attendance/overtime/${id}/reject`)
