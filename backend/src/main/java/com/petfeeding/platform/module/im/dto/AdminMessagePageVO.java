package com.petfeeding.platform.module.im.dto;

import lombok.Data;

import java.util.List;

/**
 * 管理后台消息分页视图对象（游标分页，时间正序）
 */
@Data
public class AdminMessagePageVO {

    private List<AdminMessageVO> list;
    private Long nextCursor;
}
