import request from '@/utils/request'


export default {
    getScheduleRule(pageNo, limit, hoscode, depcode) {
        return request({
            url: `/admin/hosp/schedule/getScheduleRule/${pageNo}/${limit}/${hoscode}/${depcode}`,
            method: 'get'
        })
    },
    //查询排班详情
    getScheduleDetail(hoscode,depcode,workDate) {
        return request ({
            url: `/admin/hosp/schedule/getScheduleDetail/${hoscode}/${depcode}/${workDate}`,
            method: 'get'
        })
    }
}