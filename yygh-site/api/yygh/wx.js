import request from '@/utils/request'

const api_name = `/user/userinfo/wx`

export default {
  getLoginParam() {
    return request({
      url: `${api_name}/getLoginParam`,
      method: `get`
    })
  },
  //
  createNative(orderId) {
    return request({
      url: `/api/order/weixin/createNative/${orderId}`,
      method: 'get'
    })
  },
  //查询订单支付的状态
  queryPayStatus(orderId) {
    return request({
      url: `/api/order/weixin/queryPayStatus/${orderId}`,
      method: 'get'
    })
  },
  cancelOrder(orderId) {
    return request({
      url: `/api/order/orderInfo/auth/cancelOrder/${orderId}`,
      method: 'get'
    })
  },
}