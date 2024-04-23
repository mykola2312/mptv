package com.mykola2312.mptv.db.pojo;

import jakarta.persistence.Column;

public class Source {
    @Column(name = "ID")
    public int id;

    @Column(name = "TYPE")
    public String type;

    @Column(name = "ROOT_NAME")
    public String rootName;

    @Column(name = "URL_OR_PATH")
    public String urlOrPath;

    @Column(name = "COOKIES")
    public String cookies;
}
