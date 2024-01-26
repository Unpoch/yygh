package com.wz.yygh.common.handler;

import com.wz.yygh.common.exception.YyghException;
import com.wz.yygh.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

//全局异常统一处理
//@RestControllerAdvice = @ResponseBody + @ControllerAdvice
//这样返回的数据被会被解析成JSON格式
@RestControllerAdvice //凡是@ControllerAdvice 标记的类都表示全局异常处理类
@Slf4j //使用log对象记录日志
public class GlobalExceptionHandler {

    /*
    说明：
    如果出现异常，首先走符合异常的细粒度的处理方法
    没有细粒度的，就会通过异常继承树网上找..
     */

    @ExceptionHandler(value = Exception.class) //设置处理所有异常：粗粒度的异常处理
    public R handleException(Exception ex) {
        ex.printStackTrace();
        log.error(ex.getMessage());//异常记录到日志中
        return R.error().message(ex.getMessage());
    }

    //处理自定义异常YyghException
    //注意：自定义异常出现必须自己手动抛出throw new YyghException()
    @ExceptionHandler(value = YyghException.class)
    public R handleSqlException(YyghException yyghException) {
        yyghException.printStackTrace();
        log.error(yyghException.getMessage());
        return R.error().message(yyghException.getMessage());
    }

    //sql异常
    @ExceptionHandler(value = SQLException.class) //细粒度的异常处理
    public R handleSqlException(SQLException sqlException) {
        sqlException.printStackTrace();
        log.error(sqlException.getMessage());
        return R.error().message(sqlException.getMessage());
    }


    //算术异常
    @ExceptionHandler(value = ArithmeticException.class) //细粒度的处理
    public R handleArithmeticException(ArithmeticException arithmeticException) {
        arithmeticException.printStackTrace();
        log.error(arithmeticException.getMessage());
        return R.error().message(arithmeticException.getMessage());
    }

}
