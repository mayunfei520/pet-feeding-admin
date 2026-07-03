import request from './request'

export const userApi = {
  login: (data) => request.post('/user/login', data),
  register: (data) => request.post('/user/register', data),
  list: () => request.get('/user/list'),
  listByRole: (role) => request.get('/user/list', { params: { role } }),
  updateStatus: (id, status) => request.put(`/user/${id}/status`, { status }),
  remove: (id) => request.delete(`/user/${id}`),
  resetPassword: (id) => request.post(`/user/${id}/reset-password`)
}

export const petApi = {
  list: () => request.get('/pet/all'),
  remove: (id) => request.delete(`/pet/${id}`)
}

export const feederApi = {
  list: () => request.get('/feeder'),
  pending: () => request.get('/feeder/pending'),
  approve: (id) => request.put(`/feeder/${id}/approve`),
  reject: (id) => request.put(`/feeder/${id}/reject`),
  remove: (id) => request.delete(`/feeder/${id}`)
}

export const orderApi = {
  list: () => request.get('/order/all'),
  cancel: (id) => request.put(`/order/${id}/cancel`),
  assign: (id, feederId) => request.put(`/order/${id}/assign?feederId=${feederId}`),
  remove: (id) => request.delete(`/order/${id}`)
}

export const reviewApi = {
  list: () => request.get('/review/all'),
  remove: (id) => request.delete(`/review/${id}`)
}

export const paymentApi = {
  list: () => request.get('/payment/all')
}

export const dashboardApi = {
  stats: () => request.get('/dashboard/stats')
}
