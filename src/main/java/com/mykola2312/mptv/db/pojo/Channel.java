package com.mykola2312.mptv.db.pojo;

import jakarta.persistence.Column;

public class Channel {
    @Column(name = "ID")
    public int id;

    @Column(name = "CATEGORY")
    public int category;

    @Column(name = "TITLE")
    public String title;

    @Column(name = "URL")
    public String url;

    @Column(name = "LOGO")
    public String logo;

    @Override
    public String toString() {
        return title;
    }
}
