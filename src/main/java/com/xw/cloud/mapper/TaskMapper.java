package com.xw.cloud.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xw.cloud.bean.OperationLog;
import com.xw.cloud.bean.Task;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskMapper extends BaseMapper<Task>  {
}
