/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_TARGET?: string
  readonly VITE_APP_TITLE?: string
  readonly VITE_REGISTRATION_ENABLED?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
