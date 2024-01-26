import request from '@/utils/request'

const api_name = '/admin/cmn/dict'

export default {
    //获取数据字典列表
    dictList(pid) {
        return request ({
            url: `${api_name}/findChildData/${pid}`,
            method: 'get'
        })
    }
}