import request from '@/utils/request'

const api_name = '/admin/hosp/hospitalSet'

export default {
  //医院设置列表
  getPageList(page, limit, searchObj) { //searchObj(json对象) -> 查询条件
    return request({ //通过外部引入的request调用，request里就有使用axios调用
      url: `${api_name}/page/${page}/${limit}`, //模板字符串 ``
      method: 'post',
      //请求如果携带的是普通参数，这个键就使用params;如果携带的是json数据，应该用data
      data: searchObj
    })
  },
  //医院设置删除
  removeById(id) {
    return request({
      url:`${api_name}/deleteById/${id}`,
      method: 'delete'
    })
  },
  //医院设置添加
  save(hospset) {
    return request({
        url: `${api_name}/save`,
        method: 'post',
        data: hospset
    })
  },
  //回显(其实就是通过id获取数据)
  getById(id) {
    return request({
      url: `${api_name}/getHospSet/${id}`,
      method: 'get'
    })
  },
  //更新
  updateById(hospset) {
    return request({
        url: `${api_name}/updateHospSet`,
        method: 'put',
        data: hospset
    })
  },
  //批量删除
  removeRows(ids) {
    return request({
      url: `${api_name}/batchRemove`,
      method: 'delete',
      data: ids
    })
  },
  //锁定和解锁的方法
  lockHospSet(id,status) {
    return request({
      url: `${api_name}/lockHospitalSet/${id}/${status}`,
      method: 'put'
    })
  } 
}