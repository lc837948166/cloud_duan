package com.xw.cloud.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xw.cloud.bean.NodeInfo;
import com.xw.cloud.mapper.NodeMapper;
import com.xw.cloud.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NodeServiceImpl extends ServiceImpl<NodeMapper, NodeInfo> implements NodeService {


    @Autowired
    private NodeMapper nodeMapper;



}
