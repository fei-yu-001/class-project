import request from '@/utils/request'

export const getAllReviews = () => request.get('/performance-reviews')

export const getReviewById = (id: number) => request.get(`/performance-reviews/${id}`)

export const getReviewsByEmployee = (empId: number) => request.get(`/performance-reviews/employee/${empId}`)

export const createReview = (data: any) => request.post('/performance-reviews', data)

export const updateReview = (id: number, data: any) => request.put(`/performance-reviews/${id}`, data)

export const deleteReview = (id: number) => request.delete(`/performance-reviews/${id}`)
