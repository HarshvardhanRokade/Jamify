import { useQuery } from '@tanstack/react-query'
import { authApi } from '../api/auth'
import useUserStore from '../stores/useUserStore'
import { useEffect } from 'react'

export const useAuth = () => {
  const { setUser, clearUser, user, isAuthenticated } = useUserStore()

  const { data, isLoading, error } = useQuery({
    queryKey: ['me'],
    queryFn: () => authApi.getMe(),
    retry: false,
    staleTime: 5 * 60 * 1000,
  })

  useEffect(() => {
    if (data?.data) {
      setUser(data.data)
    } else if (error) {
      clearUser()
    }
  }, [data, error])

  return { user, isAuthenticated, isLoading }
}