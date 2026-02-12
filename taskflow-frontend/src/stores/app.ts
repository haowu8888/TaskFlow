import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const darkMode = ref(false)

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function toggleDarkMode() {
    darkMode.value = !darkMode.value
    document.documentElement.classList.toggle('dark', darkMode.value)
    localStorage.setItem('darkMode', String(darkMode.value))
  }

  function initTheme() {
    const saved = localStorage.getItem('darkMode')
    if (saved === 'true') {
      darkMode.value = true
      document.documentElement.classList.add('dark')
    }
  }

  return { sidebarCollapsed, darkMode, toggleSidebar, toggleDarkMode, initTheme }
})
