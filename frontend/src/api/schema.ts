import request from '@/utils/request'

export const getSchema = () => {
  return request.get('/views/schema')
}