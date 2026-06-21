import request from '@/utils/request'

export const listAnnouncements = () => {
  return request.get('/announcements')
}

export const createAnnouncement = (data: { title: string; content: string; isTop?: boolean }) => {
  return request.post('/announcements', data)
}

export const deleteAnnouncement = (id: number) => {
  return request.delete(`/announcements/${id}`)
}

export const toggleAnnouncementTop = (id: number) => {
  return request.put(`/announcements/${id}/top`)
}
