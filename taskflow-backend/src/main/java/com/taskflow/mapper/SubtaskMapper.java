package com.taskflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskflow.entity.Subtask;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SubtaskMapper extends BaseMapper<Subtask> {
}
