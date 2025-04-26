package com.maho.simple_network_disk.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Users")
@Data   //自动添加所有的构造函数和更改函数的东西
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;    //自增，主键

    @Column(nullable = false, unique = true)   //非空，唯一
    private String username;

    @Column(nullable = false)   //非空
    private String password;

    @Column(nullable = false, unique = true)   //非空，唯一
    private String email;

    private Date RegisterTime;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<File> files = new ArrayList<>();
}