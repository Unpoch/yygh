package com.wz.yygh.user.controller.user;


import com.wz.yygh.common.result.R;
import com.wz.yygh.common.utils.AuthContextHolder;
import com.wz.yygh.model.user.UserInfo;
import com.wz.yygh.user.service.UserInfoService;
import com.wz.yygh.vo.user.LoginVo;
import com.wz.yygh.vo.user.UserAuthVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author wz
 * @since 2023-11-27
 */
//用户系统
@RestController
@RequestMapping("/user/userinfo/")
public class UserInfoController {


    @Autowired
    private UserInfoService userInfoService;

    @ApiOperation(value = "用户登录")
    @PostMapping("login")
    public R login(@RequestBody LoginVo loginVo) {
        //登录之后还需要返回一些信息(用户名..)
        Map<String, Object> map = userInfoService.login(loginVo);
        return R.ok().data(map);
    }

    @ApiOperation(value = "获取用户id信息接口")
    @GetMapping("auth/getUserInfo")
    public R getUserInfo(HttpServletRequest request) {
        Long userId = AuthContextHolder.getUserId(request);//获取请求头中的token,通过token解析出用户id
        //通过用户id获取用户信息(还需要根据用户状态 放入对应的状态文字)
        UserInfo userInfo = userInfoService.getUserInfo(userId);
        return R.ok().data("userInfo", userInfo);
    }


    @ApiOperation(value = "用户认证接口")
    @PostMapping("auth/userAuth")
    public R userAuth(@RequestBody UserAuthVo userAuthVo, HttpServletRequest request) {
        //传递两个参数，第一个参数用户id，第二个参数认证数据vo对象
        userInfoService.userAuth(AuthContextHolder.getUserId(request), userAuthVo);
        return R.ok();
    }

}

