package com.zhang.lib.http.bean;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * 请求参数结构体
 *
 * @author ZhangXiaoMing 2023-05-22 22:10 周一
 */
public class RequestParamVo implements Serializable {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;

    private Object data;
    private PageInfo pageQueryReq;

    private RequestParamVo() {
    }

    public static RequestParamVo obtain() {
        return new RequestParamVo();
    }

    public Object getData() {
        return data;
    }

    public RequestParamVo setData(Object data) {
        this.data = data;
        return this;
    }

    public RequestParamVo setPage(int page) {
        return setPageInfo(page, DEFAULT_PAGE_SIZE);
    }

    public RequestParamVo setPageSize(int pageSize) {
        return setPageInfo(DEFAULT_PAGE, pageSize);
    }

    public RequestParamVo setPageInfo(int page, int pageSize) {
        pageQueryReq = PageInfo.obtain()
                .setPage(page)
                .setPageSize(pageSize);
        return this;
    }

    public int getPage() {
        return pageQueryReq.getPage();
    }

    public int getPageSize() {
        return pageQueryReq.getPageSize();
    }

    public String toJsonString() {
        return new Gson().toJson(this);
    }

    @NonNull
    @Override
    public String toString() {
        return toJsonString();
    }


    public static class PageInfo implements Serializable {
        private int page;
        private int pageSize;

        private PageInfo() {
        }

        public PageInfo(int page, int pageSize) {
            this.page = page;
            this.pageSize = pageSize;
        }

        public static PageInfo obtain() {
            return new PageInfo();
        }

        //<editor-fold desc="Getter and Setter">
        public int getPage() {
            return page;
        }

        public PageInfo setPage(int page) {
            this.page = page;
            return this;
        }

        public int getPageSize() {
            return pageSize;
        }

        public PageInfo setPageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }
        //</editor-fold>
    }

}
