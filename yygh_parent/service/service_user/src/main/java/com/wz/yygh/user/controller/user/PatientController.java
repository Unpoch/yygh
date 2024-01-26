package com.wz.yygh.user.controller.user;


import com.wz.yygh.common.result.R;
import com.wz.yygh.common.utils.AuthContextHolder;
import com.wz.yygh.model.user.Patient;
import com.wz.yygh.user.service.PatientService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 就诊人表 前端控制器
 * </p>
 *
 * @author wz
 * @since 2023-12-01
 */
@RestController
@RequestMapping("/user/userinfo/patient/")
public class PatientController {

    @Autowired
    private PatientService patientService;

    //说明：
    //POJO加了@RequestBody,说明前端发请求是用data作为键
    // (data表明传的数据是JSON数据，因此要用@RequestBody将JSON数据转化为POJO对象)
    //POJO不加@RequestBody,说明前端发请求是用param传参数,使用普通POJO接收参数
    //能自动封装到POJO的属性之中

    //增加就诊人
    @PostMapping("auth/save")
    public R save(@RequestBody Patient patient, HttpServletRequest request) {
        //获取就诊人的id
        Long userId = AuthContextHolder.getUserId(request);
        patient.setUserId(userId);//谁添加的就诊人信息，这个信息就属于哪个用户
        patientService.save(patient);
        return R.ok();
    }


    //删除就诊人
    @DeleteMapping("auth/remove/{id}")
    public R delete(@PathVariable Long id) {
        patientService.removeById(id);
        return R.ok();
    }

    //修改就诊人
    //修改包括两步：去修改页面回显就诊人信息，提交就诊人信息进行修改

    //根据就诊人id获取就诊人信息
    @GetMapping("auth/getById/{id}")
    public R getById(@PathVariable Long id) {
        //注意要封装数据
        Patient patient = patientService.getPatientById(id);
        return R.ok().data("patient", patient);
    }

    //修改就诊人信息
    @PutMapping("auth/update")
    public R update(@RequestBody Patient patient) {
        patientService.updateById(patient);//id前端一定是携带的,不然不知道修改哪个就诊人
        return R.ok();
    }

    //查询就诊人信息
    //当点击就诊人管理时，要跳转页面，显示所有就诊人信息
    //此时就应该根据token中的用户id查询 对应的就诊人信息
    @GetMapping("auth/findAll")
    public R findAll(HttpServletRequest request) { //也可以使用@RequestHeader String token
        Long userId = AuthContextHolder.getUserId(request);
        List<Patient> list = patientService.findAllByUserId(userId);
        return R.ok().data("list", list);
    }

    /**
    远程调用接口
    根据就诊人id获取就诊人信息
     */
    @ApiOperation("根据就诊人id获取就诊人信息")
    @GetMapping("inner/get/{id}")
    public Patient getPatientOrder(
            @ApiParam(name = "id", value = "就诊人id", required = true)
            @PathVariable("id") Long id) {
        return patientService.getById(id);
    }

}


