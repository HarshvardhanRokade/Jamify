import { create } from 'zustand'

const useUserStore = create((set) => ({
  user: null,
  isAuthenticated: false,

  setUser: (user) => set({
    user,
    isAuthenticated: !!user
  }),

  clearUser: () => set({
    user: null,
    isAuthenticated: false
  }),
}))

export default useUserStore