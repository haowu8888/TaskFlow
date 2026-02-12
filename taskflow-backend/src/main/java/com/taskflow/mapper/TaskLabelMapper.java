package com.taskflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskflow.entity.TaskLabel;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskLabelMapper extends BaseMapper<TaskLabel> {
}
