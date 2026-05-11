import request from './request'

// Auth
export const login = (data) => request.post('/auth/login', data)
export const logout = () => request.post('/auth/logout')

// Card
export const generateCards = (data) => request.post('/card/generate', data)
export const getCardList = (params) => request.get('/card/list', { params })
export const getAllBatches = () => request.get('/card/batches')
export const recycleBatch = (batchNumber) => request.put(`/card/recycle/batch/${batchNumber}`)
export const recycleCard = (cardNumber) => request.put(`/card/recycle/${cardNumber}`)
export const exportCards = (params) => {
    return request.get('/card/export', { params, responseType: 'blob' })
}
export const getStatistics = () => request.get('/card/statistics')

// Verify
export const verifyCard = (data) => request.post('/verify/use', data)
export const getVerificationHistory = (params) => request.get('/verify/history', { params })

// Public
export const queryPublicCard = (params) => request.get('/public/query', { params })

// User
export const getUserList = (params) => request.get('/user/list', { params })
export const saveUser = (data) => request.post('/user/save', data)
export const deleteUser = (id) => request.delete(`/user/${id}`)
