import request from '@/utils/request'

export const getProfile = () => {
  return request.get('/user/profile')
}

export const updateProfile = (data: any) => {
  return request.put('/user/profile', data)
}

export const uploadAvatar = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/user/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const changePassword = (data: { oldPassword: string; newPassword: string }) => {
  return request.put('/user/password', data)
}
