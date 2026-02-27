package com.subtitle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("operation_logs")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String username;

    private String operation;  // 操作类型

    private String method;     // 请求方法

    private String params;     // 请求参数

    private String ip;         // IP地址

    private Integer status;    // 操作状态 1-成功 0-失败

    private String errorMsg;   // 错误信息

    private Long duration;     // 执行时长（毫秒）

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
