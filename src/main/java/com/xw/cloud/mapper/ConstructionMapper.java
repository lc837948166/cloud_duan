package com.xw.cloud.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xw.cloud.bean.ConstructionInfo;
import com.xw.cloud.bean.NodeInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConstructionMapper extends BaseMapper<ConstructionInfo>  {
}
