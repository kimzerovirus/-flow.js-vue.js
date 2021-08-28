package com.example.demo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@CrossOrigin(origins = {"*"})
@Log4j2
public class UploadController {

    private final Map<String, FileInfo> fileInfos = new ConcurrentHashMap<>();

    private final String uploadDirectory;

    public UploadController(
            @Value("#{environment.uploadDirectory}") String uploadDirectory) {
        //기본 폴더 생성 위치 C:/upload
        this.uploadDirectory = uploadDirectory;
        Path dataDir = Paths.get(uploadDirectory);

        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {

        }
    }

    @GetMapping("/upload")
    public void chunkExists(HttpServletResponse response,
                            @RequestParam("flowChunkNumber") int flowChunkNumber,
                            @RequestParam("flowIdentifier") String flowIdentifier) {

        FileInfo fi = this.fileInfos.get(flowIdentifier);
        if (fi != null && fi.containsChunk(flowChunkNumber)) {
            response.setStatus(HttpStatus.OK.value());
            return;
        }

        response.setStatus(HttpStatus.NOT_FOUND.value());
    }

    @PostMapping("/upload")
    public void processUpload(
            HttpServletResponse response,
            @RequestParam("flowChunkNumber") int flowChunkNumber,
            @RequestParam("flowTotalChunks") int flowTotalChunks,
            @RequestParam("flowChunkSize") long flowChunkSize,
            @SuppressWarnings("unused") @RequestParam("flowTotalSize") long flowTotalSize,
            @RequestParam("flowIdentifier") String flowIdentifier,
            @RequestParam("flowFilename") String flowFilename,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("category") String category
    ) throws IOException {
        log.info("filename: " + flowFilename);

        FileInfo fileInfo = this.fileInfos.get(flowIdentifier);
        if (fileInfo == null) {
            fileInfo = new FileInfo();
            this.fileInfos.put(flowIdentifier, fileInfo);
        }

        String folderPath = makeFolder(URLDecoder.decode(category, "utf-8"));
        log.info("category: " + folderPath);
        String uploadPath = this.uploadDirectory + File.separator + folderPath;

        Path identifierFile = Paths.get(uploadPath, flowIdentifier);

        try (RandomAccessFile raf = new RandomAccessFile(identifierFile.toString(), "rw");
             InputStream is = file.getInputStream()) {
            raf.seek((flowChunkNumber - 1) * flowChunkSize);

            long readed = 0;
            long content_length = file.getSize();
            byte[] bytes = new byte[1024 * 100];
            while (readed < content_length) {
                int r = is.read(bytes);
                if (r < 0) {
                    break;
                }
                raf.write(bytes, 0, r);
                readed += r;
            }
        }

        fileInfo.addUploadedChunk(flowChunkNumber);

        if (fileInfo.isUploadFinished(flowTotalChunks)) {
            Path uploadedFile = Paths.get(uploadPath, flowFilename);
            Files.move(identifierFile, uploadedFile, StandardCopyOption.ATOMIC_MOVE);

            this.fileInfos.remove(flowIdentifier);
        }

        response.setStatus(HttpStatus.OK.value());
    }

    private String makeFolder(String category) {
        //카테고리명 폴더 생성
        String str = category;

        String folderPath = str.replace("//", File.separator);

        File uploadPathFolder = new File(this.uploadDirectory, folderPath);
        if (uploadPathFolder.exists() == false) {
            uploadPathFolder.mkdirs();
        }
        return folderPath;
    }
}

