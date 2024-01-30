package com.xw.cloud.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xw.cloud.bean.PodLog;
import com.xw.cloud.bean.Task;
import com.xw.cloud.mapper.PodLogMapper;
import com.xw.cloud.mapper.TaskMapper;
import com.xw.cloud.service.PodLogService;
import com.xw.cloud.service.TaskService;
import org.springframework.stereotype.Service;


@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {

}
