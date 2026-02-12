import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import App from './App.vue'
import router from './router'
import i18n from './i18n'
import { useAuthStore } from './stores/auth'
import { useAppStore } from './stores/app'
import { useWebSocket } from './composables/useWebSocket'
import './assets/styles/global.scss'
import './assets/styles/element-overrides.scss'
import './assets/styles/dark.scss'
import './assets/styles/responsive.scss'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(i18n)
app.use(ElementPlus)

const authStore = useAuthStore()
const appStore = useAppStore()
appStore.initTheme()
authStore.initAuth()
if (authStore.isAuthenticated) {
  authStore.fetchUser()
  const { connect } = useWebSocket()
  connect()
}

app.mount('#app')
