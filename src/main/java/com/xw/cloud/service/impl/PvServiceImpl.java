package com.xw.cloud.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xw.cloud.bean.NodeInfo;
import com.xw.cloud.bean.PvInfo;
import com.xw.cloud.mapper.NodeMapper;
import com.xw.cloud.mapper.PvMapper;
import com.xw.cloud.service.NodeService;
import com.xw.cloud.service.PvService;
import org.springframework.stereotype.Service;

@Service
public class PvServiceImpl extends ServiceImpl<PvMapper, PvInfo> implements PvService {
}
