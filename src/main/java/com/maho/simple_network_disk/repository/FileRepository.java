package com.maho.simple_network_disk.repository;

import com.maho.simple_network_disk.entity.File;
import com.maho.simple_network_disk.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//查找接口
@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    Optional<File> findByFileId(Long fileId);
    Optional<File> findByFileName(String fileName);
    List<File> findByUser(User user);
}