package com.wz.yygh.hosp.controller.admin;


import com.wz.yygh.common.result.R;
import com.wz.yygh.hosp.service.HospitalSetService;
import com.wz.yygh.common.utils.MD5;
import com.wz.yygh.model.hosp.HospitalSet;
import com.wz.yygh.vo.hosp.HospitalSetQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

/**
 * <p>
 * 医院设置表 前端控制器
 * </p>
 *
 * @author wz
 * @since 2023-11-08
 */
@RestController //在SpringBoot中，@ResponseBody注解将返回的JavaBean对象封装成JSON对象返回,@RequestBody将前端传来的JSON数据转换为POJO参数
@Api(tags = "医院设置信息") //swagger，为controller设置说明信息
@RequestMapping("/admin/hosp/hospitalSet/")
@Slf4j //为了输出日志，记录日志
// @CrossOrigin //解决跨域请求
public class HospitalSetController {

    @Autowired
    private HospitalSetService hospitalSetService;

    /*
    做带条件查询的分页
    这里接收前端的查询条件：例如hosname,hoscode不建议使用 对应的HospitalSet对象
    因为只用到了HospitalSet对象的几个属性而已，因此我们使用VO对象，也就是ValueObject对象
    VO：controller层和页面进行交互的使用
    @RequestBody注解,前端传的JSON数据转换为对象
     */
    @ApiOperation(value = "带查询条件的分页")
    @PostMapping("page/{pageNum}/{limit}")
    public R getPageInfo(@ApiParam(name = "pageName", value = "当前页")
                         @PathVariable Long pageNum,
                         @ApiParam(name = "limit", value = "每页显示的条数")
                         @PathVariable Long limit,
                         @RequestBody HospitalSetQueryVo hospitalSetQueryVo) {
        //这里的泛型是HospitalSet,因为它要通过HospitalSet找到数据库中对应的表，从表中查询数据
        Page<HospitalSet> page = new Page<>(pageNum, limit);//分页查询
        QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();//查询条件
        if (!StringUtils.isEmpty(hospitalSetQueryVo.getHosname())) {
            queryWrapper.like("hosname", hospitalSetQueryVo.getHosname());
        }
        if (!StringUtils.isEmpty(hospitalSetQueryVo.getHoscode())) {
            queryWrapper.eq("hoscode", hospitalSetQueryVo.getHoscode());
        }
        hospitalSetService.page(page, queryWrapper);//执行之后page里就有我们需要的分页信息
        //将总记录数total和 当前页对应的列表的数据
        return R.ok().data("total", page.getTotal()).data("rows", page.getRecords());
    }

    /*
    锁定和解锁功能,其实就是修改状态status
     */
    @PutMapping("lockHospitalSet/{id}/{status}")
    public R updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        // HospitalSet byId = hospitalSetService.getById(id); 最大的用处是乐观锁出现的时候
        //两种都可以，这里使用自己new一个对象的方式
        HospitalSet hospitalSet = new HospitalSet();
        hospitalSet.setId(id);
        hospitalSet.setStatus(status);
        hospitalSetService.updateById(hospitalSet);
        return R.ok();
    }

    //批量删除
    @DeleteMapping("batchRemove")
    public R batchDelete(@RequestBody List<Long> ids) {
        hospitalSetService.removeByIds(ids);
        return R.ok();
    }

    //根据id查询医院设置信息
    @ApiOperation(value = "根据ID查询医院设置")
    @GetMapping("getHospSet/{id}")
    public R getById(@PathVariable Long id) {
        return R.ok().data("item", hospitalSetService.getById(id));
    }

    //修改,之前要做数据回显，因此会调用getById方法
    @ApiOperation(value = "根据ID修改医院设置")
    @PutMapping("updateHospSet")
    public R update(@RequestBody HospitalSet hospitalSet) {
        hospitalSetService.updateById(hospitalSet);
        return R.ok();
    }

    //新增医院设置
    @ApiOperation(value = "新增接口")
    @PostMapping("save")
    public R save(@RequestBody HospitalSet hospitalSet) {
        hospitalSet.setStatus(1);//设置状态，1表示正常，0表示不能使用
        Random random = new Random();
        //为第三方医院设置密钥(当前时间戳 + 随机数 + MD5)
        hospitalSet.setSignKey(MD5.encrypt(System.currentTimeMillis() + "" + random.nextInt(1000)));
        hospitalSetService.save(hospitalSet);
        return R.ok();
    }


    //查询所有
    @ApiOperation(value = "查询所有的医院设置信息") // @ApiOperation 对方法进行说明,在swagger界面可以看到
    @GetMapping("findAll")
    public R findAll() {
        List<HospitalSet> list = hospitalSetService.list();
        return R.ok().data("items", list);
    }


    //根据医院设置id删除医院设置信息
    @ApiOperation(value = "根据医院设置id删除医院设置信息")
    @DeleteMapping("deleteById/{id}")
    public R deleteById(@ApiParam(name = "id", value = "医院设置id", required = true)
                        @PathVariable Integer id) {
        hospitalSetService.removeById(id);
        return R.ok();
    }

    /*
    使用Swagger涉及到的注解：

    @Api(tars = ""):标记在接口上
    @ApiOperation(value = ""):标记在方法上
    @ApiParam(value=""):标记在参数上

    @ApiModel(value=""):对POJO类做说明
    @ApiModelProperty(value=""):对POJO类的属性做说明
     */

}

