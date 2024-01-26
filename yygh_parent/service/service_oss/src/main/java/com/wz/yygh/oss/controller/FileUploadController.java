package com.wz.yygh.oss.controller;

import com.wz.yygh.common.result.R;
import com.wz.yygh.oss.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Api(tags = "阿里云文件管理")
@RestController
@RequestMapping("/user/oss/file/")
public class FileUploadController {

    @Autowired
    private FileService fileService;




    //文件上传
    @ApiOperation(value = "文件上传")
    @PostMapping("upload")
    public R upload(
            @ApiParam(name = "file", value = "文件", required = true)
            @RequestParam("file") MultipartFile file) {
        String url = fileService.upload(file);//将上传文件的url地址返回
        return R.ok().data("url", url);
    }

}
