package com.example.administrator.ximalayafm.entry;

/**
 * Created by Administrator on 2017/12/13.
 */

public class TitleBean {
    private String title ;
    private long id;
    private boolean isClick;

    public TitleBean() {
    }

    public TitleBean(String title, long id, boolean isClick) {
        this.title = title;
        this.id = id;
        this.isClick = isClick;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isClick() {
        return isClick;
    }

    public void setClick(boolean click) {
        isClick = click;
    }
    @Override
    public String toString() {
        return "TitleBean{" +
                "title='" + title + '\'' +
                ", id=" + id +
                ", isClick=" + isClick +
                '}';
    }
}
