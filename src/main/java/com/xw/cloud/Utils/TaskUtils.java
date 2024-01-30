package com.xw.cloud.Utils;

import io.swagger.models.auth.In;

public class TaskUtils {

    private Integer task_type;
    private Integer task_executor;
    private Integer task_status;
    private String execution_method;
    private String vm_name;
    private Integer vm_num;
    private String pm_ip;
    private String vm_image_name;
    private Integer is_all_pm;
    private String cpu_num;
    private String memory;
    private String  ports;
    private String disk;
    private String docker_image_name;
    private String bandwidth;
    private String cmds;
    private String vm_ip;
    private String routing_rules;

    public String getCpu_num() {
        return cpu_num;
    }

    public void setCpu_num(String cpu_num) {
        this.cpu_num = cpu_num;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getDisk() {
        return disk;
    }

    public void setDisk(String disk) {
        this.disk = disk;
    }

    public Integer getTask_type() {
        return task_type;
    }

    public void setTask_type(Integer task_type) {
        this.task_type = task_type;
    }

    public Integer getTask_executor() {
        return task_executor;
    }

    public void setTask_executor(Integer task_executor) {
        this.task_executor = task_executor;
    }

    public Integer getTask_status() {
        return task_status;
    }

    public void setTask_status(Integer task_status) {
        this.task_status = task_status;
    }

    public String getExecution_method() {
        return execution_method;
    }

    public void setExecution_method(String execution_method) {
        this.execution_method = execution_method;
    }

    public String getVm_name() {
        return vm_name;
    }

    public void setVm_name(String vm_name) {
        this.vm_name = vm_name;
    }

    public Integer getVm_num() {
        return vm_num;
    }

    public void setVm_num(Integer vm_num) {
        this.vm_num = vm_num;
    }

    public String getPm_ip() {
        return pm_ip;
    }

    public void setPm_ip(String pm_ip) {
        this.pm_ip = pm_ip;
    }

    public String getVm_image_name() {
        return vm_image_name;
    }

    public void setVm_image_name(String vm_image_name) {
        this.vm_image_name = vm_image_name;
    }

    public Integer getIs_all_pm() {
        return is_all_pm;
    }

    public void setIs_all_pm(Integer is_all_pm) {
        this.is_all_pm = is_all_pm;
    }


    public String getPorts() {
        return ports;
    }

    public void setPorts(String ports) {
        this.ports = ports;
    }


    public String getDocker_image_name() {
        return docker_image_name;
    }

    public void setDocker_image_name(String docker_image_name) {
        this.docker_image_name = docker_image_name;
    }

    public String getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(String bandwidth) {
        this.bandwidth = bandwidth;
    }

    public String getCmds() {
        return cmds;
    }

    public void setCmds(String cmds) {
        this.cmds = cmds;
    }

    public String getVm_ip() {
        return vm_ip;
    }

    public void setVm_ip(String vm_ip) {
        this.vm_ip = vm_ip;
    }

    public String getRouting_rules() {
        return routing_rules;
    }

    public void setRouting_rules(String routing_rules) {
        this.routing_rules = routing_rules;
    }

    @Override
    public String toString() {
        return "TaskUtils{" +
                "task_type=" + task_type +
                ", task_executor=" + task_executor +
                ", task_status=" + task_status +
                ", execution_method='" + execution_method + '\'' +
                ", vm_name='" + vm_name + '\'' +
                ", vm_num=" + vm_num +
                ", pm_ip='" + pm_ip + '\'' +
                ", vm_image_name='" + vm_image_name + '\'' +
                ", is_all_pm=" + is_all_pm +
                ", cpu_num=" + cpu_num +
                ", memory=" + memory +
                ", ports='" + ports + '\'' +
                ", disk=" + disk +
                ", docker_image_name='" + docker_image_name + '\'' +
                ", bandwidth=" + bandwidth +
                ", cmds='" + cmds + '\'' +
                ", vm_ip='" + vm_ip + '\'' +
                ", routing_rules='" + routing_rules + '\'' +
                '}';
    }
}
