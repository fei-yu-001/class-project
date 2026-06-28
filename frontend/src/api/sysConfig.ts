import request from '@/utils/request'

export const getAllConfigs = () => {
  return request.get('/sys-config')
}

export const updateConfig = (key: string, configValue: string) => {
  return request.put(`/sys-config/${key}`, { configValue })
}
