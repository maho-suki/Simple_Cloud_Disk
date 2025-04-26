package com.maho.simple_network_disk.controller;

import com.maho.simple_network_disk.dto.FileUploadDto;
import com.maho.simple_network_disk.entity.File;
import com.maho.simple_network_disk.entity.User;
import com.maho.simple_network_disk.repository.FileRepository;
import com.maho.simple_network_disk.security.JwtTokenProvider;
import com.maho.simple_network_disk.service.FileService;
import com.maho.simple_network_disk.service.UserService;
import jakarta.validation.Valid;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final FileRepository fileRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final FileService fileService;
    private final UserService userService;

    @Value("${app.storage.root}")
    private String storageRoot;

    public FileController(FileRepository fileRepository, JwtTokenProvider jwtTokenProvider, FileService fileService, UserService userService) {
        this.fileRepository = fileRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.fileService = fileService;
        this.userService = userService;
    }

    // 文件上传
    @SneakyThrows
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@Valid @ModelAttribute FileUploadDto dto, @RequestHeader("Authorization") String token) {
        // 从token获取用户ID
        Long userId = jwtTokenProvider.getUserid(token.substring(7));
        User user = userService.getUserById(userId);
        MultipartFile file = dto.getFile();
        //构建存储路径
        // 确保基础路径标准化
        Path storageRootPath = Paths.get(storageRoot).normalize().toAbsolutePath();

        // 处理子路径（防路径穿越攻击）
        String safeSubPath = Optional.ofNullable(dto.getTargetPath())
                .orElse("")
                .replaceAll("\\.\\.", ""); // 过滤..
        Path userDir = storageRootPath.resolve(user.getUserId().toString())
                .resolve(safeSubPath)
                .normalize();

        // 验证是否仍在安全目录内
        if (!userDir.startsWith(storageRootPath)) {
            throw new IllegalArgumentException("非法路径");
        }
        try{
            Files.createDirectories(userDir);

            // 存储文件
            String savedName =  System.currentTimeMillis() + "_" + dto.getFile().getOriginalFilename();
            Path targetPath = userDir.resolve(savedName);
            file.transferTo(targetPath.toFile());

            //保存文件记录到数据库
            com.maho.simple_network_disk.entity.File fileRecord =  fileService.createFromUpload(
                    file.getOriginalFilename(),
                    targetPath.toString(),
                    (int)file.getSize(),
                    new Date(),
                    user
            );
            return ResponseEntity.ok(Map.of(
                "fileId", fileRecord.getFileId(),
                "path", targetPath.toString()
            ));
        }
        catch(IOException e){
            return ResponseEntity.internalServerError().body("文件存储失败");
        }
    }

    // 文件下载
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long fileId,
            @RequestHeader("Authorization") String token) {
        try {
            // 获取文件信息
            File fileInfo = fileService.getFileById(fileId);
            // 验证用户权限（确保只能下载自己的文件）
            Long userId = jwtTokenProvider.getUserid(token.substring(7));
            if (!fileInfo.getUser().getUserId().equals(userId)) {
                throw new IllegalArgumentException("无权访问此文件");
            }

            // 创建文件资源
            Path filePath = Paths.get(fileInfo.getFilePath());
            Resource resource = new FileSystemResource(filePath.toFile());

            // 如果文件不存在，抛出异常
            if (!resource.exists()) {
                throw new IllegalArgumentException("文件不存在");
            }

            // 设置文件名编码，防止中文乱码
            String encodedFileName = URLEncoder.encode(fileInfo.getFileName(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            // 返回文件流
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                    .body(resource);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败", e);
        }
    }

    // 文件删除
    @DeleteMapping("/delete/{fileId}")
    public ResponseEntity<?> deleteFile(
            @PathVariable Long fileId,
            @RequestHeader("Authorization") String token) {
        try {
            // 获取文件信息
            File fileInfo = fileService.getFileById(fileId);
            if (fileInfo == null) {
                throw new IllegalArgumentException("文件不存在");
            }

            // 验证用户权限
            String username = jwtTokenProvider.getUsername(token.substring(7));
            if (!fileInfo.getUser().getUsername().equals(username)) {
                throw new IllegalArgumentException("无权删除此文件");
            }

            // 删除文件
            fileService.DeleteFile(fileId);

            return ResponseEntity.ok(Map.of(
                "message", "文件删除成功"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "文件删除失败：" + e.getMessage()
            ));
        }
    }

    // 获取文件列表
    @GetMapping("/list")
    public ResponseEntity<?> getFileList(@RequestHeader("Authorization") String token) {
        try {
            // 从token获取用户ID
            Long userId = jwtTokenProvider.getUserid(token.substring(7));
            User user = userService.getUserById(userId);
            
            // 获取用户的文件列表
            List<File> files = fileRepository.findByUser(user);
            
            // 手动转换为前端需要的格式
            List<Map<String, Object>> fileList = files.stream()
                .map(file -> {
                    Map<String, Object> fileMap = new HashMap<>();
                    fileMap.put("fileId", file.getFileId());
                    fileMap.put("fileName", file.getFileName());
                    fileMap.put("fileSize", file.getFileSize());
                    fileMap.put("uploadTime", file.getUploadTime());
                    return fileMap;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(fileList);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "获取文件列表失败：" + e.getMessage()
            ));
        }
    }
}
