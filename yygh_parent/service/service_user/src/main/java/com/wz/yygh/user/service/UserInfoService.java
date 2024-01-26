package com.wz.yygh.user.service;

import com.wz.yygh.model.user.UserInfo;
import com.wz.yygh.vo.user.LoginVo;
import com.wz.yygh.vo.user.UserAuthVo;
import com.wz.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author wz
 * @since 2023-11-27
 */
public interface UserInfoService extends IService<UserInfo> {

    Map<String, Object> login(LoginVo loginVo);

    UserInfo selectWxInfoByOpenId(String openid);

    UserInfo getUserInfo(Long userId);

    void userAuth(Long userId, UserAuthVo userAuthVo);

    Page<UserInfo> selectPage(Long pageNo, Long limit, UserInfoQueryVo userInfoQueryVo);

    void approval(Long userId, Integer authStatus);

    void lock(Long userId, Integer status);

    Map<String, Object> detail(Long userId);
}
