package com.taskflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskflow.entity.Board;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BoardMapper extends BaseMapper<Board> {
}
