package com.xw.cloud.service.impl;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


import com.xw.cloud.bean.OperationLog;
import com.xw.cloud.mapper.OperationLogMapper;
import com.xw.cloud.service.OpertationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OpertationLogService {

}
