package com.wz.yygh.user.controller.admin;

import com.wz.yygh.common.result.R;
import com.wz.yygh.model.user.UserInfo;
import com.wz.yygh.user.service.UserInfoService;
import com.wz.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

//后台管理员系统对用户信息的操作
@RestController
@RequestMapping("/admin/userinfo/")
public class AdminUserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    @ApiOperation(value = "后台带查询分页展示用户信息")
    @GetMapping("{pageNo}/{limit}")
    public R getUserInfoPage(
            @PathVariable Long pageNo,
            @PathVariable Long limit,
            UserInfoQueryVo userInfoQueryVo) {
        Page<UserInfo> pages = userInfoService.selectPage(pageNo, limit, userInfoQueryVo);
        return R.ok().data("pageModel", pages);
    }

    @ApiOperation(value = "认证审批")
    @GetMapping("approval/{userId}/{authStatus}")
    public R approval(@PathVariable Long userId, @PathVariable Integer authStatus) {
        userInfoService.approval(userId, authStatus);
        return R.ok();
    }

    @ApiOperation(value = "锁定和解锁")
    @GetMapping("lock/{userId}/{status}")
    public R lock(@PathVariable("userId") Long userId,
                  @PathVariable("status") Integer status) {
        userInfoService.lock(userId, status);
        return R.ok();
    }

    @ApiOperation("查看用户详情")
    @GetMapping("show/{userId}")
    public R show(@PathVariable Long userId) {
        Map<String, Object> map = userInfoService.detail(userId);
        return R.ok().data(map);
    }
}
