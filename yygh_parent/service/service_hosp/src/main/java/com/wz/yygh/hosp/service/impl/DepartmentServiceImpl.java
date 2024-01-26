package com.wz.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.wz.yygh.hosp.repository.DepartmentRepository;
import com.wz.yygh.hosp.service.DepartmentService;
import com.wz.yygh.model.hosp.Department;
import com.wz.yygh.vo.hosp.DepartmentQueryVo;
import com.wz.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    //保存科室信息
    /*
    如果是第一次添加，就是插入
    如果是第二次添加，就是更新
     */
    @Override
    public void save(Map<String, Object> paramMap) {
        //将paramMap转化为JSON字符串，将JSON字符串转化为对应的对象
        Department department = JSONObject.parseObject(JSONObject.toJSONString(paramMap), Department.class);
        //根据医院编号hoscode和医院科室编号depcode 联合查询，确定指定医院的指定科室是否存在
        String hoscode = department.getHoscode();
        String depcode = department.getDepcode();
        //我们平台存的信息,要和第三方医院传过来的进行比对
        Department deptExist = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if (deptExist == null) {//没有加入过,添加操作
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        } else {//修改操作
            //因为根据id修改，要设置id
            department.setId(deptExist.getId());
            department.setCreateTime(deptExist.getCreateTime());
            department.setUpdateTime(new Date());
            department.setIsDeleted(deptExist.getIsDeleted());
            departmentRepository.save(department);
        }
    }

    //科室带条件分页查询的方法
    @Override
    public Page<Department> selectPage(int pageNo, int limit, DepartmentQueryVo departmentQueryVo) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(pageNo - 1, limit, sort);
        Department department = new Department();
        BeanUtils.copyProperties(departmentQueryVo, department);
        department.setIsDeleted(0);

        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符串匹配方式：模糊查询
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写
        //创建实例
        Example<Department> example = Example.of(department, matcher);
        //带查询条件的分页
        Page<Department> pages = departmentRepository.findAll(example, pageable);
        return pages;
    }

    //删除科室信息
    @Override
    public void remove(String hoscode, String depcode) {
        //本质还是根据id删除,因此我们mongodb查询(可以自定义查询方法)出Department对象,
        // 然后调用删除的方法,从数据库中查询出来的一定有id
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if (null != department)
            departmentRepository.deleteById(department.getId());
    }

    //根据医院编号hoscode查询医院所有科室列表
    @Override
    public List<DepartmentVo> findDeptTree(String hoscode) {
        List<DepartmentVo> result = new ArrayList<>();
        //设置查询条件
        Department departmentQuery = new Department();
        departmentQuery.setHoscode(hoscode);
        Example<Department> example = Example.of(departmentQuery);
        //这就是该医院所有的科室信息
        //_id   hoscode     depcode     depname     bigcode     bigname ....
        //xxx1  10000        0001       神经内科       001         内科
        //xxx2  10000        0002       皮肤外科       002         外科
        //xxx3  10000        0003       泌尿内科       001         内科
        List<Department> departmentList = departmentRepository.findAll(example);
        //那么为了返回对应的层级结构,我们要对以上数据 进行分组(内科放一起,外科放一起..)
        //这里使用流的方式是最简单的
        //根据当前科室所属大科室的编号分组，相同大科室的分在一组
        //得到的map，key是根据谁分组，就是谁，这里就是bigcode,
        // map的value就是科室信息，也就是同属于大科室bigcode的子科室信息
        Map<String, List<Department>> deparmentMap = departmentList.stream().collect(Collectors.groupingBy(Department::getBigcode));
        for (Map.Entry<String, List<Department>> entry : deparmentMap.entrySet()) {
            String bigcode = entry.getKey();//大科室编号
            List<Department> childrenDept = entry.getValue();//子科室列表

            //封装子科室vo列表childrenDeptVos -> 遍历childrenDept
            List<DepartmentVo> childrenDeptVos = new ArrayList<>();
            for (Department child : childrenDept) {
                DepartmentVo childDeptVo = new DepartmentVo();
                childDeptVo.setDepcode(child.getDepcode());//设置编号
                childDeptVo.setDepname(child.getDepname());//设置科室名称
                childrenDeptVos.add(childDeptVo);//加入结果
            }

            DepartmentVo bigDeptVo = new DepartmentVo();//大科室Vo对象
            bigDeptVo.setDepcode(bigcode);//设置大科室编号
            bigDeptVo.setDepname(childrenDept.get(0).getBigname());//子科室所属大科室的名字 -> 设置到bigDeptVo
            bigDeptVo.setChildren(childrenDeptVos);//设置子科室列表
            result.add(bigDeptVo);//加入要返回的结果
        }
        return result;
    }

    //根据医院编号和科室编号获取科室名称
    @Override
    public String getDepName(String hoscode, String depcode) {
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if (department != null) {
            return department.getDepname();
        }
        return null;
    }

    //根据医院编号和科室编号获取科室对象
    @Override
    public Department getDepartment(String hoscode, String depcode) {
        return departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
    }
}
