package com.wz.yygh.user.controller.user;

import com.alibaba.fastjson.JSONObject;
import com.wz.yygh.common.result.R;
import com.wz.yygh.common.utils.JwtHelper;
import com.wz.yygh.model.user.UserInfo;
import com.wz.yygh.user.service.UserInfoService;
import com.wz.yygh.user.util.ConstantPropertiesUtil;
import com.wz.yygh.user.util.HttpClientUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/user/userinfo/wx/")
public class WeixinController {

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private RedisTemplate redisTemplate;

    @ApiOperation(value = "获取微信登录参数")
    @GetMapping("getLoginParam")
    @ResponseBody
    public R getWeixinLoginParam() throws UnsupportedEncodingException {
        String redirectUri = URLEncoder.encode(ConstantPropertiesUtil.WX_OPEN_REDIRECT_URL, "UTF-8");
        Map<String, Object> map = new HashMap<>();
        map.put("appid", ConstantPropertiesUtil.WX_OPEN_APP_ID);
        map.put("redirectUri", redirectUri);
        map.put("scope", "snsapi_login");
        map.put("state", System.currentTimeMillis() + "");//System.currentTimeMillis()+""
        return R.ok().data(map);
    }

    //回调接口,会携带两个参数code和state
    @ApiOperation(value = "微信服务器回调接口")
    @GetMapping("callback")
    public String callback(String code, String state) {
        //再次给微信服务器发送请求,请求地址如下(需要携带参数)
        //第一步 获取临时票据 code
        System.out.println("code:" + code);
        //第二步 拿着code和app_id和AppSecret，请求微信固定地址 获取access_token和open_id
        //  %s   占位符
        StringBuffer baseAccessTokenUrl = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");
        String accessTokenUrl = String.format(baseAccessTokenUrl.toString(),
                ConstantPropertiesUtil.WX_OPEN_APP_ID,
                ConstantPropertiesUtil.WX_OPEN_APP_SECRET,
                code);
        try {
            String accesstokenInfo = HttpClientUtils.get(accessTokenUrl);//发送请求
            System.out.println("accesstokenInfo:" + accesstokenInfo);
            //从返回字符串获取两个值 openid  和  access_token
            JSONObject jsonObject = JSONObject.parseObject(accesstokenInfo);
            String access_token = jsonObject.getString("access_token");//访问微信服务器的凭证
            String openid = jsonObject.getString("openid");//微信扫描用户在微信服务器的唯一标识符
            //使用openid在数据库中查询该用户是否存在
            UserInfo userInfo = userInfoService.selectWxInfoByOpenId(openid);
            if (null == userInfo) {//如果用户不存在,说明首次使用微信登录
                //第三步 拿着openid  和  access_token请求微信地址，得到扫码人信息
                String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                        "?access_token=%s" +
                        "&openid=%s";
                String userInfoUrl = String.format(baseUserInfoUrl, access_token, openid);
                String resultInfo = HttpClientUtils.get(userInfoUrl);//发送请求
                System.out.println("resultInfo:" + resultInfo);
                JSONObject resultUserInfoJson = JSONObject.parseObject(resultInfo);
                //解析用户信息
                //用户昵称
                String nickname = resultUserInfoJson.getString("nickname");
                //用户头像
                String headImgUrl = resultUserInfoJson.getString("headimgurl");
                //将扫码人信息添加到数据库
                userInfo = new UserInfo();
                userInfo.setNickName(nickname);
                userInfo.setOpenid(openid);
                userInfo.setStatus(1);//设置状态
                userInfoService.save(userInfo);
            }
            //返回name和token字符串
            Map<String, String> map = new HashMap<>();
            String name = userInfo.getName();
            if (StringUtils.isEmpty(name)) {
                name = userInfo.getNickName();
            }
            if (StringUtils.isEmpty(name)) {
                name = userInfo.getPhone();
            }
            map.put("name", name);
            //这里还需要做一个强制绑定手机号的绑定
            //判断userInfo是否有手机号，如果为空(首次微信登录)，返回openid
            //如果手机号不为空(已经绑定过手机号了)，返回openid 值是字符串
            //前端判断：如果openid不为空，绑定手机号，如果openid为空，不需要绑定手机号
            if(StringUtils.isEmpty(userInfo.getPhone())) {
                map.put("openid",userInfo.getOpenid());
            }else {
                map.put("openid","");
            }
            //使用jwt生成token字符串
            String token = JwtHelper.createToken(userInfo.getId(), name);
            map.put("token", token);
            //跳转到前端页面
            return "redirect:http://localhost:3000/weixin/callback?token="+map.get("token")+ "&openid="+map.get("openid")+"&name="+URLEncoder.encode(map.get("name"),"utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
