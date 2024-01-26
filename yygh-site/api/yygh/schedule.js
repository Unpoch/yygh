import request from '@/utils/request'

const api_name = `/user/hosp/schedule`

export default {
    //获取预约挂号规则，分页展示
    getBookingScheduleRule(pageNo, limit, hoscode, depcode) {
        return request({
            url: `${api_name}/auth/getBookingScheduleRule/${pageNo}/${limit}/${hoscode}/${depcode}`,
            method: 'get'
        })
    },
    //得到科室列表
    findScheduleList(hoscode, depcode, workDate) {
        return request({
            url: `${api_name}/auth/findScheduleList/${hoscode}/${depcode}/${workDate}`,
            method: 'get'
        })
    },
    getSchedule(id) {
        return request({
            url: `${api_name}/getSchedule/${id}`,
            method: 'get'
        })
    }
}