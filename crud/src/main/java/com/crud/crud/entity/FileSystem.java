package com.crud.crud.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
public class FileSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fileId")
    private Long fileId;

    @Column(name = "originalName")
    private String originalName;

    @Column(name = "storedName")
    private String storedName;

    @Column(name = "path")
    private String path;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postId")
    private Board board;
}

