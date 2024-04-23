package com.mykola2312.mptv.db.pojo;

import jakarta.persistence.Column;

public class Source {
    @Column(name = "ID")
    public int id;

    @Column(name = "TYPE")
    public String type;

    @Column(name = "ROOT_NAME")
    public String rootName;

    @Column(name = "URL")
    public String url;

    @Column(name = "PATH")
    public String path;

    @Column(name = "COOKIES")
    public String cookies;
}
