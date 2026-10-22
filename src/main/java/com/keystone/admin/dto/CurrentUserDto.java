package com.keystone.admin.dto;

import lombok.Data;

/**
 * 当前登录用户对象
 */
@Data
public class CurrentUserDto {
    /** 用户标识 */
    private String id;
    /** 用户名（登录名） */
    private String name;
}