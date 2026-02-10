package com.subtitle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.subtitle.entity.TaskRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskRecordMapper extends BaseMapper<TaskRecord> {
}