package com.xw.cloud.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xw.cloud.bean.ConstructionInfo;
import com.xw.cloud.bean.NodeInfo;
import com.xw.cloud.mapper.ConstructionMapper;
import com.xw.cloud.mapper.NodeMapper;
import com.xw.cloud.service.ConstructionService;
import com.xw.cloud.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConstructionServiceImpl extends ServiceImpl<ConstructionMapper, ConstructionInfo> implements ConstructionService {


    @Autowired
    private ConstructionMapper constructionMapper;


}
