package com.maho.simple_network_disk.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "Files")
@Data   //自动添加所有的构造函数和更改函数的东西
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fileId;    //自增，主键

    @Column(nullable = false)      //非空
    private String fileName;

    @Column(nullable = false)   //非空
    private String filePath;

    private Integer fileSize;

    @Temporal(TemporalType.TIMESTAMP)
    private Date uploadTime;

    @ManyToOne(fetch = FetchType.LAZY)          //与userId构成外键
    @JoinColumn(name = "userId", nullable = false)
    private User user;
}
