package com.wz.yygh.hosp.controller.admin;

import com.wz.yygh.common.result.R;
import com.wz.yygh.model.acl.User;
import org.springframework.web.bind.annotation.*;

//后台用户信息相关
// @CrossOrigin
@RestController
@RequestMapping("/admin/user/")
public class UserController {

    @PostMapping("login")
    public R login(@RequestBody User user) {
        //暂时不去数据库中查：用户系统再去
        return R.ok().data("token", "admin-token");
    }

    @GetMapping("info")
    public R info(String token) {
        //暂时不去数据库中查：用户系统再去
        return R.ok().data("roles", "[admin]").
                data("introduction", "I am a super administrator").
                data("avatar", "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif").
                data("name", "Super Admin");
    }


}
