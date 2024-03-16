package com.xw.cloud.controller;

import com.xw.cloud.inter.OperationLogDesc;
import com.xw.cloud.mapper.TemplateMapper;
import com.xw.cloud.bean.*;
import com.xw.cloud.service.LibvirtService;
import io.swagger.annotations.Api;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import com.xw.cloud.Utils.CommentResp;

import java.io.InputStream;
import java.util.List;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import javax.annotation.Resource;
@Api(tags = "模版管理", description = "提供模版管理")
@CrossOrigin
@Controller
@RequestMapping("/Template")
public class TemplateController {

    @Resource
    private TemplateMapper templateMapper;

    @Resource(name = "libvirtService")
    private LibvirtService libvirtService;
    //通过id得到用户信息
//    @RequestMapping(value = "/getUser/{id}", method = RequestMethod.GET)
//    public String getUser(@PathVariable int id){
//        return templateService.gettemp(id).toString();
//    }
    //通过id删除用户信息
    @RequestMapping(value = "/delete/{id}",method = RequestMethod.DELETE)
    @ResponseBody
    public CommentResp delete(@PathVariable("id") int id){
        System.out.println(id);
        int result = templateMapper.deleteById(id);
        if(result >= 1){
            return new CommentResp(true, null,"删除成功");
        }else{
            return new CommentResp(false, null,"删除失败");
        }
    }
    //更改用户信息
    @RequestMapping(value = "/update")
    @ResponseBody
    @OperationLogDesc(module = "模板管理", events = "模板更新")
    public CommentResp update(@RequestBody Template temp){
        System.out.println(temp);
        int result = templateMapper.updateById(temp);
        if (result >= 1) {
            return new CommentResp(true, null,"更新成功");
        } else {
            return new CommentResp(false, null,"更新失败");
        }
    }
    //插入用户信息
    @RequestMapping(value = "/insert")
    @ResponseBody
    @OperationLogDesc(module = "模板管理", events = "添加模板")
    public CommentResp insert(@RequestBody Template temp) {
        int result = templateMapper.insert(temp);
        if (result >= 1) {
            return new CommentResp(true, null,"插入成功");
        } else {
            return new CommentResp(false, null,"插入失败");
        }
    }



    //查询所有用户的信息
    @RequestMapping(value = "/selectAll")
    @ResponseBody   //理解为：单独作为响应体，这里不调用实体类的toString方法
    public CommentResp  listUser(){
        List<Template> tempList = templateMapper.selectList(null);
        return new CommentResp(true, tempList,"");
    }

//    @ResponseBody
//    @RequestMapping(value = "/cloneFromTemplate", method = RequestMethod.GET)
//    public CommentResp cloneFromTemplate(@RequestParam("1name") String name,
//                              @RequestParam("clone_name") String clone_name) throws Exception {
//        String virtualMachineIp = "127.0.0.1";
//        String username = "root";
//        String password = "111";
//
//        Session session = null;
//        ModelAndView modelAndView = new ModelAndView("jsonView");
//        JSch jsch = new JSch();
//        session = jsch.getSession(username, virtualMachineIp, 22);
//        session.setConfig("StrictHostKeyChecking", "no");
//        session.setPassword(password);
//        session.connect();
//        // 执行命令
//        Channel execChannel = session.openChannel("exec");
//        ((ChannelExec) execChannel).setCommand("virt-clone -o "+name+" -n "+clone_name+" --auto-clone"); // 设置执行的命令
//        InputStream in = null;
//        in = execChannel.getInputStream();  // 获取命令执行结果的输入流
//        execChannel.connect();  // 连接远程执行命令
//        byte[] tmp = new byte[1024];
//        StringBuilder commandOutput = new StringBuilder(); //存储命令执行的输出
//        while (true) {
//            while (in.available() > 0) {
//                int i = in.read(tmp, 0, 1024);
//                if (i < 0) break;
//                commandOutput.append(new String(tmp, 0, i));
//            }
//            if (execChannel.isClosed()) {
//                if (in.available() > 0) continue;
//                break;
//            }
//            try {
//                Thread.sleep(1000);
//            } catch (Exception ee) {
//                // 处理异常
//            }
//        }
//        String result=commandOutput.toString();
//        if(result.contains("功")){
//            return new CommentResp(true, null,"克隆成功");
//        }
//        else return new CommentResp(true, null,"克隆虚拟机失败，请检查是否与其他虚拟机名称重复");
//    }


    @ResponseBody
    @RequestMapping("/addVirtual")
    public CommentResp addVirtual(@RequestParam("ImgName") String ImgName, @RequestParam("name") String name,
                                  @RequestParam("memory") int memory, @RequestParam("cpuNum") int cpuNum,
                                  @RequestParam("OStype") String OStype,@RequestParam("nettype") String NetType,
                                  @RequestParam("serverip") String serverip) throws InterruptedException {
        VM_create vmc = new VM_create();
        vmc.setName(name);
        vmc.setMemory(memory);
        vmc.setCpuNum(cpuNum);
        vmc.setOStype(OStype);
        vmc.setImgName(ImgName);
        vmc.setNetType(NetType);
        libvirtService.addImgFile(vmc.getName(),ImgName);
        libvirtService.addDomainByName(vmc,serverip);

        return new CommentResp(true, null,"创建虚拟机成功");
    }
}

