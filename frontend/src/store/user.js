import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
    state: () => ({
        userInfo: JSON.parse(localStorage.getItem('user_info')) || null
    }),
    actions: {
        setUserInfo(user) {
            this.userInfo = user
            localStorage.setItem('user_info', JSON.stringify(user))
        },
        clearUserInfo() {
            this.userInfo = null
            localStorage.removeItem('user_info')
        }
    }
})
