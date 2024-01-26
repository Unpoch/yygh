import request from '@/utils/request'

const api_name = `/user/sms`

export default {
    sendCode(mobile) {
        return request({
            url: `${api_name}/send/${mobile}`,
            method: `get`
        })
    }
}