import { create } from 'zustand'

const useUIStore = create((set) => ({
  isLoading: false,
  activeModal: null,

  setLoading: (isLoading) => set({ isLoading }),
  openModal: (modal) => set({ activeModal: modal }),
  closeModal: () => set({ activeModal: null }),
}))

export default useUIStore