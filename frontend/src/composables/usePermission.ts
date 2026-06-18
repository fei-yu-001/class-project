import { useAuthStore } from '@/stores/auth'

export function usePermission() {
  const auth = useAuthStore()

  const isSuperAdmin = () => auth.user?.role === 'SUPER_ADMIN'
  const isAdmin = () => auth.user?.role === 'ADMIN' || auth.user?.role === 'SUPER_ADMIN'
  const isUser = () => auth.user?.role === 'USER'

  const canEdit = () => isAdmin()
  const canDelete = () => isAdmin()
  const canCreate = () => isAdmin()
  const canManageUsers = () => isSuperAdmin()
  const canApprove = () => isAdmin()

  return { isSuperAdmin, isAdmin, isUser, canEdit, canDelete, canCreate, canManageUsers, canApprove }
}
