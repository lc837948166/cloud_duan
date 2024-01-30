package com.xw.cloud.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xw.cloud.bean.PodLog;
import com.xw.cloud.mapper.PodLogMapper;
import com.xw.cloud.service.PodLogService;
import org.springframework.stereotype.Service;


@Service
public class PodLogServiceImpl extends ServiceImpl<PodLogMapper, PodLog> implements PodLogService {

}
