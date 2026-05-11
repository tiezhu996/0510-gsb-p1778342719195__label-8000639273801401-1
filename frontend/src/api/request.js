import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const service = axios.create({
    baseURL: '/api',
    timeout: 5000
})

service.interceptors.request.use(
    config => {
        const token = localStorage.getItem('user_info')
        if (token) {
            // Ideally add token header here if JWT was used
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

service.interceptors.response.use(
    response => {
        const res = response.data
        // Check if response is a blob (file download)
        if (response.config.responseType === 'blob' || res instanceof Blob) {
            return res
        }

        if (res.code !== 200) {
            ElMessage.error(res.message || 'Error')
            if (res.code === 401) {
                localStorage.removeItem('user_info')
                router.push('/login')
            }
            return Promise.reject(new Error(res.message || 'Error'))
        } else {
            return res.data
        }
    },
    error => {
        ElMessage.error(error.message || 'Request Error')
        return Promise.reject(error)
    }
)

export default service
