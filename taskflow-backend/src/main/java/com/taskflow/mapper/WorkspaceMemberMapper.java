package com.taskflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskflow.entity.WorkspaceMember;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WorkspaceMemberMapper extends BaseMapper<WorkspaceMember> {
}
