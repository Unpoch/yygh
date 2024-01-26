package com.wz.yygh.user.service.impl;

import com.wz.yygh.common.exception.YyghException;
import com.wz.yygh.common.utils.JwtHelper;
import com.wz.yygh.enums.AuthStatusEnum;
import com.wz.yygh.model.user.Patient;
import com.wz.yygh.model.user.UserInfo;
import com.wz.yygh.user.mapper.UserInfoMapper;
import com.wz.yygh.user.service.PatientService;
import com.wz.yygh.user.service.UserInfoService;
import com.wz.yygh.vo.user.LoginVo;
import com.wz.yygh.vo.user.UserAuthVo;
import com.wz.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author wz
 * @since 2023-11-27
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private PatientService patientService;

    //用户登录
    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        //1.获取用户输入的手机号和验证码信息
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        //2.对接收到的手机号和验证码做非空验证
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)) {
            throw new YyghException(20001, "手机号或者验证码为空");
        }
        //3.对验证码校验(将redis保存的验证码和用户输入的验证码进行对比)
        String queryCode = redisTemplate.opsForValue().get(phone);
        if (StringUtils.isEmpty(queryCode) || !code.equals(queryCode)) {
            throw new YyghException(20001, "验证码有误");
        }
        String openid = loginVo.getOpenid();
        Map<String, Object> map;
        //如果没有openid,说明纯手机号登录
        if (StringUtils.isEmpty(openid)) {
            //根据手机号查询数据库
            QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("phone", phone);
            UserInfo userInfo = baseMapper.selectOne(wrapper);
            //如果返回对象为空，就是第一次登录，存到数据库登录数据
            if (userInfo == null) {
                userInfo = new UserInfo();
                userInfo.setName("");
                userInfo.setPhone(phone);
                userInfo.setStatus(1);
                baseMapper.insert(userInfo);
            }
            //判断用户是否可用
            if (userInfo.getStatus() == 0) {
                throw new YyghException(20001, "用户已经禁用");
            }
            map = get(userInfo);
        } else {//有openid,微信强制绑定了手机号登录
            //1 创建userInfo对象，用于存在最终所有数据
            UserInfo userInfoFinal = new UserInfo();
            //2 根据手机查询数据
            // 如果查询手机号对应数据,封装到userInfoFinal
            UserInfo userInfoPhone =
                    baseMapper.selectOne(new QueryWrapper<UserInfo>().eq("phone", phone));
            if (userInfoPhone != null) {//之前用手机号登录过,数据要合并,手机号那一行要删除
                // 如果查询手机号对应数据,封装到userInfoFinal
                BeanUtils.copyProperties(userInfoPhone, userInfoFinal);
                //把手机号对应数据删除
                baseMapper.delete(new QueryWrapper<UserInfo>().eq("phone", phone));
            }
            //3 根据openid查询微信信息
            QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("openid", openid);
            UserInfo userInfoWX = baseMapper.selectOne(wrapper);
            //4 把微信信息封装userInfoFinal
            userInfoFinal.setOpenid(userInfoWX.getOpenid());
            userInfoFinal.setNickName(userInfoWX.getNickName());
            userInfoFinal.setId(userInfoWX.getId());
            //数据库表没有相同绑定手机号，设置值
            if (userInfoPhone == null) {
                userInfoFinal.setPhone(phone);
                userInfoFinal.setStatus(userInfoWX.getStatus());
            }
            //修改手机号
            baseMapper.updateById(userInfoFinal);
            //5 判断用户是否锁定
            if (userInfoFinal.getStatus() == 0) {
                throw new YyghException(20001, "用户被锁定");
            }
            //6 登录后，返回登录数据
            map = get(userInfoFinal);
        }
        return map;
    }

    //返回页面显示名称,封装成一个方法
    private Map<String, Object> get(UserInfo userInfo) {
        Map<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        map.put("name", name);
        //根据userid和name生成token字符串
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token", token);
        return map;
    }

    //根据openid查询用户信息
    @Override
    public UserInfo selectWxInfoByOpenId(String openid) {
        return baseMapper.selectOne(new QueryWrapper<UserInfo>().eq("openid", openid));
    }

    //根据用户id获取用户信息
    @Override
    public UserInfo getUserInfo(Long userId) {
        UserInfo userInfo = baseMapper.selectById(userId);
        userInfo.getParam().put("authStatusString", AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        return userInfo;
    }

    //用户实名认证
    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        //根据用户id查询用户信息
        UserInfo userInfo = baseMapper.selectById(userId);
        //设置认证信息
        //认证人姓名
        userInfo.setName(userAuthVo.getName());
        //其他认证信息
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
        //信息更新
        baseMapper.updateById(userInfo);
    }

    //分页查询你
    @Override
    public Page<UserInfo> selectPage(Long pageNo, Long limit, UserInfoQueryVo userInfoQueryVo) {
        //UserInfoQueryVo获取条件值
        String name = userInfoQueryVo.getKeyword(); //用户名称
        Integer status = userInfoQueryVo.getStatus();//用户状态
        Integer authStatus = userInfoQueryVo.getAuthStatus(); //认证状态
        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin(); //开始时间
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd(); //结束时间
        //对条件值进行非空判断
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(name)) {
            wrapper.like("name", name);
        }
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("status", status);
        }
        if (!StringUtils.isEmpty(authStatus)) {
            wrapper.eq("auth_status", authStatus);
        }
        if (!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge("create_time", createTimeBegin);
        }
        if (!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le("create_time", createTimeEnd);
        }
        Page<UserInfo> pageParam = new Page<>(pageNo, limit);
        Page<UserInfo> pages = baseMapper.selectPage(pageParam, wrapper);
        pages.getRecords().stream().forEach(item -> {
            this.packageUserInfo(item);
        });
        return pages;
    }

    //用户认证审批
    //2通过,-1表示不通过
    @Override
    public void approval(Long userId, Integer authStatus) {
        if (authStatus.intValue() == 2 || authStatus.intValue() == -1) {
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setAuthStatus(authStatus);
            baseMapper.updateById(userInfo);
        }
    }

    //锁定和解锁
    @Override
    public void lock(Long userId, Integer status) {
        if (status.intValue() == 0 || status.intValue() == 1) {
            UserInfo userInfo = this.getById(userId);
            userInfo.setStatus(status);
            this.updateById(userInfo);
        }
    }

    //查看用户详情
    @Override
    public Map<String, Object> detail(Long userId) {
        //userId查询用户信息
        UserInfo userInfo = this.packageUserInfo(baseMapper.selectById(userId));
        //根据userId查询用户就诊人信息
        List<Patient> patientList = patientService.findAllByUserId(userId);
        Map<String, Object> map = new HashMap<>();
        map.put("userInfo", userInfo);
        map.put("patientList", patientList);
        return map;
    }

    //编号变成对应值封装(证件类型编号 -> 证件类型, 用户状态0，1 -> 用户状态)
    private UserInfo packageUserInfo(UserInfo userInfo) {
        //处理认证状态编码
        userInfo.getParam().put("authStatusString", AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        //处理用户状态 0  1
        String statusString = userInfo.getStatus().intValue() == 0 ? "锁定" : "正常";
        userInfo.getParam().put("statusString", statusString);
        return userInfo;
    }
}
