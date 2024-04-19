package com.mykola2312.mptv;

import jakarta.persistence.*;

public class Test {
    @Column(name = "ID")
    public int id;

    @Column(name = "VALUE")
    public String value;
}