package com.mykola2312.mptv.db.pojo;

import jakarta.persistence.Column;

public class Category {
    @Column(name = "ID")
    public int id;

    @Column(name = "TITLE")
    public String title;
}
