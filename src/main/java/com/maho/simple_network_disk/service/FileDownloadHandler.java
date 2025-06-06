package com.maho.simple_network_disk.service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;

public class FileDownloadHandler {
    private static final int BUFFER_SIZE = 8192;
    
    public void handleDownload(HttpServletResponse response, File fileInfo, String range) throws IOException {
        if (!fileInfo.exists()) {
            throw new FileNotFoundException("文件不存在: " + fileInfo.getPath());
        }

        try (RandomAccessFile file = new RandomAccessFile(fileInfo.getPath(), "r")) {
            long fileLength = file.length();
            long startByte = 0;
            long endByte = fileLength - 1;

            // 只有在有Range头部时才进行分块处理
            if (range != null && range.startsWith("bytes=")) {
                String[] ranges = range.substring("bytes=".length()).split("-");
                try {
                    startByte = Long.parseLong(ranges[0]);
                    if (ranges.length > 1 && !ranges[1].isEmpty()) {
                        endByte = Long.parseLong(ranges[1]);
                    }
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                
                // 验证范围的有效性
                if (startByte < 0 || endByte >= fileLength || startByte > endByte) {
                    response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    response.setHeader("Content-Range", "bytes */" + fileLength);
                    return;
                }
                
                // 设置206状态码和Content-Range头
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                response.setHeader("Content-Range", "bytes " + startByte + "-" + endByte + "/" + fileLength);
            }

            // 设置响应头
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Content-Type", "application/octet-stream");
            String fileName = fileInfo.getName();
            fileName = new String(fileName.getBytes("UTF-8"), "ISO-8859-1");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            response.setHeader("Content-Length", String.valueOf(endByte - startByte + 1));

            // 发送文件内容
            try (OutputStream out = new BufferedOutputStream(response.getOutputStream(), BUFFER_SIZE)) {
                file.seek(startByte);
                byte[] buffer = new byte[BUFFER_SIZE];
                long bytesRemaining = endByte - startByte + 1;
                int bytesRead;

                while (bytesRemaining > 0 && (bytesRead = file.read(buffer, 0, (int)Math.min(buffer.length, bytesRemaining))) != -1) {
                    out.write(buffer, 0, bytesRead);
                    bytesRemaining -= bytesRead;
                }
                out.flush();
            }
        } catch (IOException e) {
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            throw e;
        }
    }
}