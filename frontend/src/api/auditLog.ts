import request from '@/utils/request'

export const getAuditLogs = (params: { page?: number; size?: number }) => {
  return request.get('/audit-logs', { params })
}
