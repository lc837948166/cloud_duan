package com.xw.cloud.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xw.cloud.bean.NodeInfo;
import com.xw.cloud.bean.VMInfo2;
import com.xw.cloud.mapper.VmMapper;
import com.xw.cloud.service.NodeService;
import com.xw.cloud.service.VmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class VmServiceImpl extends ServiceImpl<VmMapper, VMInfo2> implements VmService {

    @Autowired
    private NodeService nodeService;

    @Override
    public Map<String, Object> queryLatAndLon(String ip) {
        QueryWrapper queryWrapper = Wrappers.query();
        queryWrapper.eq("IP", ip);
        String serverIp = getOne(queryWrapper).getServerip();
        QueryWrapper queryWrapper1 = Wrappers.query();
        queryWrapper1.eq("IN_IP", serverIp);
        NodeInfo nodeInfo = nodeService.getOne(queryWrapper1);
        if(Objects.isNull(nodeInfo)) {
            throw new RuntimeException("虚拟机所在节点不存在，无法查询！");
        }
//        Double nodeLon = nodeInfo.getNodeLon();
//        Double nodeLat = nodeInfo.getNodeLat();
        Double nodeLon = null;
        Double nodeLat = null;
        String bandwidth = nodeInfo.getBandwidth();
        Map<String, Object> map = new HashMap<>();
        map.put("lon", nodeLon);
        map.put("lat", nodeLat);
        map.put("bandwidth", bandwidth);
//        list.add(nodeLon);
//        list.add(nodeLat);
        return map;
    }
}
