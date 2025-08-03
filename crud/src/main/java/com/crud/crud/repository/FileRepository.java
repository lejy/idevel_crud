package com.crud.crud.repository;

import com.crud.crud.entity.FileSystem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileSystem,Long> {
}
