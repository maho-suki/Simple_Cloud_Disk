package com.maho.simple_network_disk.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotNull;

@Data
public class FileUploadDto {
    @NotNull(message = "文件不能为空")
    private MultipartFile file;
    
    private String targetPath; // 可选的目标子路径
}
