package com.crud.crud.repository;

import com.crud.crud.entity.FileSystem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileSystemRepository extends JpaRepository<FileSystem,Long> {
    Optional<Object> findByOriginalName(String originalName);
}
