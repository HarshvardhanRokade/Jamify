import client from './client'

export const authApi = {
  getMe: () => client.get('/auth/me'),
  logout: () => client.post('/auth/logout'),
}