package com.xw.cloud.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xw.cloud.Utils.CommentResp;
import com.xw.cloud.bean.UserInfo;
import com.xw.cloud.inter.OperationLogDesc;
import com.xw.cloud.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;


@Api(tags = "用户管理",value = "用户管理", description = "用户控制器，用于处理用户登录、注销等操作")
@CrossOrigin
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    @ApiOperation(value = "获取用户列表", notes = "列出用户指标")
    @OperationLogDesc(module = "用户管理", events = "用户列表查询")
    @ResponseBody
    public CommentResp getUserList() {
        return new CommentResp(true, userService.list(),"");
    }

    @DeleteMapping("/delete")
    @ApiOperation(value = "删除用户", notes = "删除用户")
    @OperationLogDesc(module = "用户管理", events = "删除用户")
    @ResponseBody
    public CommentResp deleteUser(@RequestBody UserInfo userInfo) {
        boolean deleteResult = userService.delete(userInfo);
        if (deleteResult) {
            return new CommentResp(true, null,"删除成功");
        } else {
            return new CommentResp(false, null,"删除失败");
        }
    }

    /**
     * 登录界面
     * @return
     */
    @GetMapping("/login")
    public RedirectView login2() {
        RedirectView redirectView1 = new RedirectView("/login");
        return redirectView1;
    }

    @PostMapping("/login")
    @ResponseBody
    @ApiOperation(value = "用户登录", notes = "用户登录")
    @OperationLogDesc(module = "用户管理", events = "用户登录")
    public CommentResp login(@RequestBody UserInfo userInfo) {
        boolean loginResult = userService.login(userInfo);
        if (loginResult) {
            return new CommentResp(true, null,"登陆成功");
        } else {
            return new CommentResp(false, null,"登录失败,请检查用户名密码");
        }
    }

    @PostMapping("/login1")
    @ApiOperation(value = "用户登录", notes = "用户登录")
    @OperationLogDesc(module = "用户管理", events = "用户登录")
    public RedirectView  login1(@RequestBody UserInfo userInfo) {
        RedirectView redirectView1 = new RedirectView("/index");//成功登录界面，跳转到/index
        RedirectView redirectView2 = new RedirectView("/user/login");//登陆失败,跳转到登录界面
        boolean loginResult = userService.login(userInfo);
        if (loginResult) {
            return redirectView1;
        } else {
            return redirectView2;
        }

    }



    @PostMapping("/register")
    @ResponseBody
    @ApiOperation(value = "用户注册", notes = "用户注册")
    @OperationLogDesc(module = "用户管理", events = "用户注册")
    public CommentResp register(@RequestBody UserInfo userInfo) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("UserName ", userInfo.getUserName());
        queryWrapper.eq("UserGroupId  ", userInfo.getUserGroupId());
        if(userService.getOne(queryWrapper)!=null){
            return new CommentResp(false, null,"注册失败，用户已存在");
        }
        boolean registerResult = userService.register(userInfo);
        if (registerResult) {
            return new CommentResp(true, null,"注册成功");
        } else {
            return new CommentResp(false, null,"注册失败");
        }
    }

    @PostMapping("/register1")
    @ApiOperation(value = "用户注册", notes = "用户注册")
    @OperationLogDesc(module = "用户管理", events = "用户注册")
    public RedirectView register1(@RequestBody UserInfo userInfo) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("UserName ", userInfo.getUserName());
        queryWrapper.eq("UserGroupId  ", userInfo.getUserGroupId());

        RedirectView redirectView1 = new RedirectView("/user/login");//成功注册，跳转到登录界面
        RedirectView redirectView2 = new RedirectView("/user/login");//注册失败
        if(userService.getOne(queryWrapper)!=null){
            throw new RuntimeException("用户已存在");
        }
        boolean registerResult = userService.register(userInfo);
        if (registerResult) {
            return redirectView1;
        } else {
            throw new RuntimeException("用户注册失败");
        }

    }

    @GetMapping("/logout")
    @ResponseBody
    @ApiOperation(value = "用户登出", notes = "用户登出")
    @OperationLogDesc(module = "用户管理", events = "用户登出")
    public CommentResp logout() {
        boolean logoutResult = userService.logout();

        return new CommentResp(true, null,"登出成功");

    }

    @GetMapping("/logout1")
    @ApiOperation(value = "用户登出", notes = "用户登出")
    @OperationLogDesc(module = "用户管理", events = "用户登出")
    public RedirectView logout1() {
        boolean logoutResult = userService.logout();
        RedirectView redirectView = new RedirectView("/user/login");//登录界面
        return redirectView;
    }


}
