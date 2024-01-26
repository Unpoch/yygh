package com.wz.yygh.hosp.controller.api;

import com.wz.yygh.common.exception.YyghException;
import com.wz.yygh.common.utils.MD5;
import com.wz.yygh.hosp.result.Result;
import com.wz.yygh.hosp.service.DepartmentService;
import com.wz.yygh.hosp.service.HospitalService;
import com.wz.yygh.hosp.service.HospitalSetService;
import com.wz.yygh.hosp.service.ScheduleService;
import com.wz.yygh.hosp.utils.HttpRequestHelper;
import com.wz.yygh.model.hosp.Department;
import com.wz.yygh.model.hosp.Hospital;
import com.wz.yygh.model.hosp.Schedule;
import com.wz.yygh.vo.hosp.DepartmentQueryVo;
import com.wz.yygh.vo.hosp.ScheduleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

//平台对外开放的接口都写在该类中,该类用于和第三方医院对接
@Api(tags = "医院管理API接口")
@RestController
@RequestMapping("/api/hosp/")
public class ApiController {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private HospitalSetService hospitalSetService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;

    @ApiOperation(value = "排班信息分页查询")
    @PostMapping("schedule/list")
    public Result<Page<Schedule>> getSchedulePage(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //必须参数校验..signKey验证...
        //要根据医院的编号hoscode和科室编号depcode,设置查询条件
        String hoscode = (String) paramMap.get("hoscode");
        String depcode = (String) paramMap.get("depcode");
        int pageNo = StringUtils.isEmpty(paramMap.get("page")) ? 1 : Integer.parseInt((String) paramMap.get("page"));
        int limit = StringUtils.isEmpty(paramMap.get("limit")) ? 10 : Integer.parseInt((String) paramMap.get("limit"));
        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);
        scheduleQueryVo.setDepcode(depcode);
        //调用分页查询的方法
        Page<Schedule> page = scheduleService.selectPage(pageNo, limit, scheduleQueryVo);
        return Result.ok(page);
    }

    @ApiOperation(value = "排班信息删除")
    @PostMapping("schedule/remove")
    public Result removeSchedule(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        String hoscode = (String) paramMap.get("hoscode");
        String hosScheduleId = (String) paramMap.get("hosScheduleId");
        //参数校验... signKey校验..
        //根据医院编号和排班id删除排班信息
        scheduleService.remove(hoscode,hosScheduleId);
        return Result.ok();
    }

    @ApiOperation(value = "排班信息上传")
    @PostMapping("saveSchedule")
    public Result saveSchedule(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //验证signKey..
        scheduleService.save(paramMap);
        return Result.ok();
    }

    @ApiOperation(value = "科室信息分页查询")
    @PostMapping("department/list")
    public Result<Page<Department>> getDepartmentPage(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //必须参数校验..略
        String hoscode = (String) paramMap.get("hoscode");
        String depcode = (String) paramMap.get("depcode");
        int pageNo = StringUtils.isEmpty(paramMap.get("page")) ? 1 : Integer.parseInt((String) paramMap.get("page"));
        int limit = StringUtils.isEmpty(paramMap.get("limit")) ? 10 : Integer.parseInt((String) paramMap.get("limit"));
        //签名校验..
        DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
        departmentQueryVo.setHoscode(hoscode);
        departmentQueryVo.setDepcode(depcode);
        Page<Department> page = departmentService.selectPage(pageNo, limit, departmentQueryVo);
        return Result.ok(page);
    }

    @ApiOperation(value = "科室删除")
    @PostMapping("department/remove")
    public Result removeDepartment(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //signKey验证...
        String hoscode = (String) paramMap.get("hoscode");
        String depcode = (String) paramMap.get("depcode");
        departmentService.remove(hoscode, depcode);
        return Result.ok();
    }


    @ApiOperation(value = "上传科室信息")
    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //必须的参数校验..省略
        //signKey的验证..(省略了)
        departmentService.save(paramMap);
        return Result.ok();
    }


    @ApiOperation(value = "查询医院信息")
    @PostMapping("hospital/show")
    public Result<Hospital> getHospitalInfo(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        String hoscode = (String) paramMap.get("hoscode");
        //参数校验
        if (StringUtils.isEmpty(hoscode)) {
            throw new YyghException(20001, "失败");
        }
        //signKey的验证...
        //根据hoscode查询医院信息
        Hospital hospital = hospitalService.getHospitalByHoscode(hoscode);
        return Result.ok(hospital);
    }


    @ApiOperation(value = "第三方医院上传医院数据")
    @PostMapping("saveHospital")
    public Result saveHospital(HttpServletRequest request) {
        //1.获取所有的参数
        //将map进行转化,因为传过来的参数没有一个键key对应多个值(String[]),都是一对一,因此进行转化
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //要将第三方医院传过来的signSey和我们存的该医院的signKey作比较，不一致不能添加
        //我们提供的signKey存在yygh_hosp数据库的hospital_set表中,
        // 因此我们要通过hospitalSetService来通过医院编码查询密钥
        String hoscode = (String) paramMap.get("hoscode");
        if (StringUtils.isEmpty(hoscode)) {
            throw new YyghException(20001, "失败");
        }
        // String hospSign =  hospitalSetService.getSignKey(hoscode);
        //1.获取医院传过来的签名，签名进行MD5加密
        String hospSign = (String) paramMap.get("sign");
        //2.根据传递过来的医院编码hoscode，查询数据库，查询签名
        String signKey = hospitalSetService.getSignKey(hoscode);
        //3.把数据库查询签名(数据库存储时是明文)进行MD5加密
        String signKeyMD5 = MD5.encrypt(signKey);
        //4.判断签名是否一致
        if (!hospSign.equals(signKeyMD5)) {
            throw new YyghException(20001, "校验失败");
        }
        //图片的编码在传输过程中会将"+" -> " ",因此我们要还原回来
        String logoData = (String) paramMap.get("logoData");
        String result = logoData.replaceAll(" ", "+");
        paramMap.put("logoData", result);
        hospitalService.save(paramMap);
        return Result.ok();
    }


}
