package com.petfeeding.platform.module.im.dto;

import lombok.Data;

@Data
public class PeerInfo {
    private Long id;          // 对方 userId
    private String role;      // OWNER / FEEDER
    private String name;      // 真实姓名或用户名
    private String avatar;
    private Boolean certified; // 喂养员是否认证通过
}
