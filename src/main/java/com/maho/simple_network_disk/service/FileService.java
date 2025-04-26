package com.maho.simple_network_disk.service;

import com.maho.simple_network_disk.entity.File;
import com.maho.simple_network_disk.entity.User;
import com.maho.simple_network_disk.repository.FileRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Optional;

//文件的服务层，但是还没来得及写注释
@Service
public class FileService {

    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public File CreateFile(File file) {
        return fileRepository.save(file);
    }

    public File createFromUpload(String fileName, String filePath, Integer fileSize, Date uploadTime, User user){
        File entity = new File();
        entity.setFileName(fileName);
        entity.setFilePath(filePath);
        entity.setFileSize(fileSize);
        entity.setUploadTime(uploadTime);
        entity.setUser(user);
        fileRepository.save(entity);
        return entity;
    }

    public File saveFile(File file) {
        return fileRepository.save(file);
    }

    public boolean DeleteFile(long fileId) {
        Optional<File> fileOptional = fileRepository.findById(fileId);
        if (fileOptional.isEmpty()) {
            throw new IllegalArgumentException("File not found");
        }
        File currentFile = fileOptional.get();
        
        // 只删除物理文件
        try {
            Path filePath = Paths.get(currentFile.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("删除文件失败: " + e.getMessage());
        }

        // 删除数据库记录
        fileRepository.delete(currentFile);
        return true;
    }

    public boolean ChangeFileName(long fileId, String newFileName) {
        if (newFileName == null) {
            throw new IllegalArgumentException("File name cannot be null");
        }
        Optional<File> fileOptional = fileRepository.findById(fileId);
        if (fileOptional.isEmpty()) {
            throw new IllegalArgumentException("File not found");
        }
        File currentFile = fileOptional.get();
        currentFile.setFileName(newFileName);
        fileRepository.save(currentFile);
        return true;
    }

    public boolean ChangeFilePath(long fileId, String newFilePath) {
        if (newFilePath == null) {
            throw new IllegalArgumentException("File path cannot be null");
        }
        Optional<File> fileOptional = fileRepository.findById(fileId);
        if (fileOptional.isEmpty()) {
            throw new IllegalArgumentException("File not found");
        }
        File currentFile = fileOptional.get();
        currentFile.setFilePath(newFilePath);
        fileRepository.save(currentFile);
        return true;
    }

    public List<File> getAllFiles() {
        return fileRepository.findAll();
    }

    public File getFileById(long fileId) {
        Optional<File> fileOptional = fileRepository.findById(fileId);
        if (fileOptional.isEmpty()) {
            throw new IllegalArgumentException("文件不存在");
        }
        return fileOptional.get();
    }
}
