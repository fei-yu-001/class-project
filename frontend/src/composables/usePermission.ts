import { useAuthStore } from '@/stores/auth'

export function usePermission() {
  const auth = useAuthStore()

  const isAdmin = () => auth.user?.role === 'ADMIN'
  const isUser = () => auth.user?.role === 'USER'

  const canEdit = () => isAdmin()
  const canDelete = () => isAdmin()
  const canCreate = () => isAdmin()
  const canManageUsers = () => isAdmin()
  const canApprove = () => isAdmin()

  return { isAdmin, isUser, canEdit, canDelete, canCreate, canManageUsers, canApprove }
}
