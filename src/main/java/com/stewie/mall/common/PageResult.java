package com.stewie.mall.common;

import com.github.pagehelper.PageInfo;
import lombok.Data;

import java.util.List;

/**
 * 统一分页返回体。
 * PageInfo 字段太多(list/total/pageNum/pageSize/pages/navigatepageNums/...),
 * 这里抽成精简结构,前端只需要这 5 项。
 */
@Data
public class PageResult<T> {

    private List<T> list;
    private long total;
    private int pageNum;
    private int pageSize;
    private int pages;

    public static <T> PageResult<T> of(PageInfo<T> pageInfo) {
        PageResult<T> r = new PageResult<>();
        r.list = pageInfo.getList();
        r.total = pageInfo.getTotal();
        r.pageNum = pageInfo.getPageNum();
        r.pageSize = pageInfo.getPageSize();
        r.pages = pageInfo.getPages();
        return r;
    }
}
