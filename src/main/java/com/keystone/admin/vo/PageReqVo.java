package com.keystone.admin.vo;

import lombok.Data;

/**
 * 分页请求参数：page 默认第 1 页，limit 默认每页 15 条
 */
@Data
public class PageReqVo {

    /** 当前页，默认 1 */
    private int page = 1;

    /** 每页条数，默认 15 */
    private int limit = 15;
}
