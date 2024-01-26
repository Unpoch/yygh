package com.wz.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.wz.yygh.common.exception.YyghException;
import com.wz.yygh.hosp.repository.ScheduleRepository;
import com.wz.yygh.hosp.service.DepartmentService;
import com.wz.yygh.hosp.service.HospitalService;
import com.wz.yygh.hosp.service.ScheduleService;
import com.wz.yygh.model.hosp.BookingRule;
import com.wz.yygh.model.hosp.Department;
import com.wz.yygh.model.hosp.Hospital;
import com.wz.yygh.model.hosp.Schedule;
import com.wz.yygh.vo.hosp.BookingScheduleRuleVo;
import com.wz.yygh.vo.hosp.ScheduleOrderVo;
import com.wz.yygh.vo.hosp.ScheduleQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

    //保存排班信息
    @Override
    public void save(Map<String, Object> paramMap) {
        Schedule schedule = JSONObject.parseObject(JSONObject.toJSONString(paramMap), Schedule.class);
        //去mongodb中查询是否存在相应的排班信息，有做修改，无做添加
        //有hoscode,hosScheduleId(排班的id)
        String hoscode = schedule.getHoscode();
        String hosScheduleId = schedule.getHosScheduleId();
        //去mongodb中查询出的对象
        Schedule scheduleExist = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if (null == scheduleExist) {//没有排班信息,做添加操作
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            scheduleRepository.save(schedule);
        } else {//已有排班信息，做修改
            schedule.setId(scheduleExist.getId());//本质根据id修改，因此要设置id
            schedule.setUpdateTime(new Date());
            schedule.setCreateTime(scheduleExist.getCreateTime());//原来的创建时间
            schedule.setIsDeleted(scheduleExist.getIsDeleted());//原来的状态
            scheduleRepository.save(schedule);
        }
    }

    //排班信息分页查询
    @Override
    public Page<Schedule> selectPage(int pageNo, int limit, ScheduleQueryVo scheduleQueryVo) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        //0为第一页
        Pageable pageable = PageRequest.of(pageNo - 1, limit, sort);

        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleQueryVo, schedule);
        schedule.setIsDeleted(0);

        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符串匹配方式：模糊查询
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写

        //创建实例
        Example<Schedule> example = Example.of(schedule, matcher);
        Page<Schedule> pages = scheduleRepository.findAll(example, pageable);
        return pages;
    }

    //删除排班信息
    @Override
    public void remove(String hoscode, String hosScheduleId) {
        //根据hoscode和hosScheduleId查询排班信息
        //然后根据id进行删除
        Schedule schedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if (null != schedule)
            scheduleRepository.deleteById(schedule.getId());
    }

    //根据医院编号和科室编号查询 排班规则(分页查询)
    @Override
    public Map<String, Object> getRuleSchedule(long pageNo, long limit, String hoscode, String depcode) {
        //根据日期workDate进行聚合(mysql中叫分组)
        //聚合最好使用MongoTemplate
        //聚合条件 -> 查询某个医院(hoscode),某个科室(depcode)下的排班信息,
        // 然后根据某个字段(workDate,字段是mongodb集合中的)进行分组
        /*
        select score,count(),sum()..
        from 表
        where 查询条件
        group by score
        order by workDate
        limit (pageNo-1)*size,size
        mysql中进行分组的时候,查询的字段只能是分组字段，以及一些统计函数
         */
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);//设置查询条件
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria), //聚合条件
                Aggregation.group("workDate")
                        .first("workDate").as("workDate") //.first(字段)表示查询的字段(相当于score)
                        .count().as("docCount") //count就是统计分组之后的记录数，为了封装到BookingScheduleRuleVo中
                        .sum("reservedNumber").as("reservedNumber") //统计已预约数,as是起别名
                        .sum("availableNumber").as("availableNumber"), //统计可预约数
                Aggregation.sort(Sort.Direction.ASC, "workDate"), //根据workDate升序
                Aggregation.skip((pageNo - 1) * limit),//前端要求显示第pageNo页,意味着我们要从(pageNo-1)*limit 这一条开始查
                Aggregation.limit(limit) //每页显示limit条
        );
        /*
        1.第一个参数Aggregation:表示聚合条件
        2.第二个参数InputType:表示输入类型，可以根据当前指定的字节码找到mongodb中对应的集合
        3.第三个参数OutputType:表示输出类型，可以封装聚合后的信息
         */
        AggregationResults<BookingScheduleRuleVo> aggregate =
                mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);//聚合后的结果
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggregate.getMappedResults();//当前页对应的列表数据
        //还要显示这一天是周几,将list中的BookingScheduleRuleVo日期数据进行转化
        //把日期对应星期获取
        for (BookingScheduleRuleVo bookingScheduleRuleVo : bookingScheduleRuleVoList) {
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }

        //分组查询的总记录数(分成了多少组)
        Aggregation totalAgg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );
        AggregationResults<BookingScheduleRuleVo> totalAggResults =
                mongoTemplate.aggregate(totalAgg, Schedule.class, BookingScheduleRuleVo.class);
        int total = totalAggResults.getMappedResults().size();//总记录数

        Map<String, Object> result = new HashMap<>();
        result.put("bookingScheduleRuleList", bookingScheduleRuleVoList);    //当前页对应的列表数据
        result.put("total", total); //总记录数

        //获取医院名称
        Hospital hospital = hospitalService.getHospitalByHoscode(hoscode);
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        baseMap.put("hosname", hospital.getHosname());
        result.put("baseMap", baseMap);
        return result;
    }

    //查询排班详情
    @Override
    public List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate) {
        Date date = new DateTime(workDate).toDate();//因为mongodb是严格区分数据类型的,在集合中日期是Date类型
        List<Schedule> scheduleList =
                scheduleRepository.getScheduleByHoscodeAndDepcodeAndWorkDate(hoscode, depcode, date);
        //把得到list集合遍历，向设置其他值：医院名称、科室名称、日期对应星期
        scheduleList.stream().forEach(item -> {
            this.packageSchedule(item);
        });
        return scheduleList;
    }

    //获取可预约排班数据
    @Override
    public Map<String, Object> getBookingScheduleRule(Integer pageNo, Integer limit, String hoscode, String depcode) {
        Map<String, Object> result = new HashMap<>();
        Hospital hospital = hospitalService.getHospitalByHoscode(hoscode);
        if (null == hospital) {
            throw new YyghException();
        }
        //获取预约规则
        BookingRule bookingRule = hospital.getBookingRule();
        //获取可预约日期分页数据
        IPage<Date> iPage = this.getListDate(pageNo, limit, bookingRule);
        List<Date> dateList = iPage.getRecords();//当前页的时间列表
        //某个医院(hoscode) 某个科室(depcode)下的 符合当前页时间的数据(在当前页时间列表的中的时间内)
        Criteria criteria = Criteria.where("hoscode").is(hoscode)
                .and("depcode").is(depcode)
                .and("workDate").in(dateList);
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate").first("workDate").as("workDate") //根据日期分组
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber") //对已预约数求和
                        .sum("availableNumber").as("availableNumber"), //对可预约数求和
                Aggregation.sort(Sort.Direction.ASC, "workDate"));
        //这里不需要分页了，因为已经根据时间进行了分页处理，现在查询的数据都是在这一页时间内的
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> scheduleVoList = aggregate.getMappedResults();//获取聚合后的排班列表
        //有些天是没有排班的，但是我们要时间连续
        //将这个列表转化为map，方便我们封装到最后的map result中, 用workDate作为键，BookingScheduleRuleVo对象本身作为值
        Map<Date, BookingScheduleRuleVo> scheduleVoMap = scheduleVoList.stream().collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate, BookingScheduleRuleVo -> BookingScheduleRuleVo));
        //遍历这个时间连续的 当前页的时间列表dateList
        int len = dateList.size();
        //获取可预约排班规则
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            Date date = dateList.get(i);
            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleVoMap.get(date);//根据键获取值
            if (null == bookingScheduleRuleVo) {//说明当前没有排班医生
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                //就诊医生人数
                bookingScheduleRuleVo.setDocCount(0);
                //科室剩余预约数  -1表示无号
                bookingScheduleRuleVo.setAvailableNumber(-1);
            }
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date);
            //计算当前预约日期为周几
            String dayOfWeek = this.getDayOfWeek(new DateTime(date));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);

            //最后一页最后一条记录为即将预约   状态 0：正常 1：即将放号 -1：当天已停止挂号
            if (i == len - 1 && pageNo == iPage.getPages()) {
                bookingScheduleRuleVo.setStatus(1);
            } else {
                bookingScheduleRuleVo.setStatus(0);
            }
            //判断当前是否超过当天医院挂号的截止时间，状态显示 -1
            //当天预约如果过了停号时间， 不能预约
            if (i == 0 && pageNo == 1) {//第一页第一条
                DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
                if (stopTime.isBeforeNow()) {
                    bookingScheduleRuleVo.setStatus(-1);//停止预约
                }
            }
            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }
        //可预约日期规则数据
        result.put("bookingScheduleList", bookingScheduleRuleVoList);
        result.put("total", iPage.getTotal());
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        //医院名称
        baseMap.put("hosname", hospitalService.getHospitalByHoscode(hoscode).getHosname());
        //科室
        Department department = departmentService.getDepartment(hoscode, depcode);
        baseMap.put("department", department.getBigname());
        baseMap.put("depname", department.getDepname()); //月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        //放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        //停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());
        result.put("baseMap", baseMap);
        return result;
    }

    //根据id获取排班信息
    @Override
    public Schedule getByScheduleId(String scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        return this.packageSchedule(schedule);
    }

    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        BeanUtils.copyProperties(schedule, scheduleOrderVo);
        //获取医院名字
        Hospital hospital = hospitalService.getHospitalByHoscode(schedule.getHoscode());
        scheduleOrderVo.setHosname(hospital.getHosname());
        //获取科室名字
        Department department = departmentService.getDepartment(schedule.getHoscode(), schedule.getDepcode());
        scheduleOrderVo.setDepname(department.getDepname());

        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        //设置退号的截止时间
        Date date = new DateTime(schedule.getWorkDate()).plusDays(hospital.getBookingRule().getQuitDay()).toDate();
        DateTime dateTime = this.getDateTime(date, hospital.getBookingRule().getQuitTime());
        scheduleOrderVo.setQuitTime(dateTime.toDate());

        //设置当天挂号的截止时间
        Date workDate = schedule.getWorkDate();//用户要预约挂号的时间(不一定是当天的)
        String stopTime = hospital.getBookingRule().getStopTime();
        scheduleOrderVo.setStopTime(this.getDateTime(workDate, stopTime).toDate());
        return scheduleOrderVo;
    }

    //修改排班
    @Override
    public void update(Schedule schedule) {
        schedule.setUpdateTime(new Date());
        //主键一致就是更新
        scheduleRepository.save(schedule);
    }

    //根据医院排班id hosScheduleId获取排班信息
    @Override
    public Schedule getByHosScheduleId(String hosScheduleId) {
        return scheduleRepository.getScheduleByHosScheduleId(hosScheduleId);
    }

    private IPage<Date> getListDate(Integer pageNo, Integer limit, BookingRule bookingRule) {
        Integer cycle = bookingRule.getCycle();
        //判断此时此刻是否已经超过了医院规定的当天的挂号起始时间，如果已经超过了：cycle + 1
        String releaseTime = bookingRule.getReleaseTime();
        //今天医院规定的挂号的起始时间: 2023-12-03 21:39
        DateTime dateTime = this.getDateTime(new Date(), releaseTime);
        if (dateTime.isBeforeNow()) {//表示此时过了医院当天挂号的起始时间
            cycle++;
        }
        //预约周期内所有的时间 列表(10天或者11天)
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < cycle; i++) {
            //计算当前预约日期
            DateTime curDateTime = new DateTime().plusDays(i);
            String dateString = curDateTime.toString("yyyy-MM-dd");
            dateList.add(new DateTime(dateString).toDate());
        }
        //日期分页，由于预约周期不一样，页面一排最多显示7天数据，多了就要分页显示
        List<Date> curPageDateList = new ArrayList<>();//当前页时间列表
        //第pageNo页要显示的数据范围[start,end]
        int start = (pageNo - 1) * limit;//第start条数据
        int end = start + limit;//第end条数据
        if (end > dateList.size()) {//如果是最后一页，数据不足limit条
            end = dateList.size();
        }
        for (int i = start; i < end; i++) {
            curPageDateList.add(dateList.get(i));
        }
        IPage<Date> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNo, limit, dateList.size());//dateList.size()总记录数
        iPage.setRecords(curPageDateList);
        return iPage;
    }

    //将Date日期（yyyy-MM-dd HH:mm）转换为DateTime
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " " + timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
    }

    //封装排班详情其他值 医院名称、科室名称、日期对应星期
    private Schedule packageSchedule(Schedule schedule) {
        //设置医院名称
        schedule.getParam().put("hosname", hospitalService.getHospitalByHoscode(schedule.getHoscode()).getHosname());
        //设置科室名称
        schedule.getParam().put("depname",
                departmentService.getDepName(schedule.getHoscode(), schedule.getDepcode()));
        //设置日期对应星期
        schedule.getParam().put("dayOfWeek", this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
        return schedule;
    }

    /*
     * 根据日期获取周几数据
     */
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }
}
