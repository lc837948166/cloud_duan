package com.xw.cloud.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xw.cloud.bean.VMLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VMLogMapper extends BaseMapper<VMLog>  {
    @Select("select distinct VMNAME from T_CLOUD_LOG_VMLOG")
    List<String> getVmName();
}
