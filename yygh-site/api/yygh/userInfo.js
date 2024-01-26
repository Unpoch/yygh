import request from '@/utils/request'

const api_name = `/user/userinfo`

export default {
    login(userInfo) {
        return request({
            url: `${api_name}/login`,
            method: `post`,
            data: userInfo
        })
    },
    //获取用户信息
    getUserInfo() {
        return request({
            url: `${api_name}/auth/getUserInfo`,
            method: `get`
        })
    },
    saveUserAuth(userAuth) {
        return request({
             url: `${api_name}/auth/userAuth`,
             method: 'post',
             data: userAuth
        })
    }
}