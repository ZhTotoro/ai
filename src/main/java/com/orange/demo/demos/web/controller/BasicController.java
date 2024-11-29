package com.orange.demo.demos.web.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@CrossOrigin
@RestController
public class BasicController {

    private static final ConcurrentHashMap<Integer, String> hashMap = new ConcurrentHashMap<>();

    @Value("${file.path}")
    private String path;

    @Value("${file.out.path}")
    private String outPath;

    @Value("${hashmap.maxSize}")
    private Integer maxSize;

    @GetMapping("/upload")
    public String upload(@RequestParam("avatar") String avatar, @RequestParam("image") String image) {

        // 下载图片
        String avatarUuid;
        String imageUuid;

        long avatarSize = Integer.MIN_VALUE;

        long imageSize = Integer.MIN_VALUE;

        if (!hashMap.containsKey(avatar.hashCode())) {
            avatarUuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
            avatarSize = HttpUtil.downloadFile(avatar, path + avatarUuid + ".png");
        } else {
            avatarUuid = hashMap.get(avatar.hashCode());
        }

        if (!hashMap.containsKey(image.hashCode())) {
            imageUuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
            imageSize = HttpUtil.downloadFile(image, path + imageUuid + ".png");
        } else {
            imageUuid = hashMap.get(image.hashCode());
        }

        if (avatarSize == 0 || imageSize == 0) {
            return "图片下载失败";
        }

        put(avatar.hashCode(), avatarUuid);
        put(image.hashCode(), imageUuid);
        log.info("[hashMap size:{}]", hashMap.size());

        String taskId = avatar.hashCode() + "&" + image.hashCode() + ".png";

        // 执行shell命令
        String command = "cd /home/tim/下载/inswapper && source /home/tim/下载/inswapper/venv/bin/activate &&  python /home/tim/下载/inswapper/swapper.py \\\n" +
                "--source_img \"" + path + avatarUuid + ".png\"" + "\\\n" +
                "--target_img \"" + path + imageUuid + ".png\"" + "\\\n" +
                "--output_img \"" + outPath + taskId + " \"\\\n" +
                "--face_restore \\\n" +
                "--background_enhance \\\n" +
                "--face_upsample \\\n" +
                "--upscale=2 \\\n" +
                "--codeformer_fidelity=0.5";

        try {
            // 创建 ProcessBuilder 对象
            ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command);

            log.info("[执行命令:{}]", command);

            // 启动进程
            Process process = processBuilder.start();

            // 获取命令输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("[命令输出:{}]", line);
            }
            // 等待命令执行完成
            int exitCode = process.waitFor();
            log.info("[命令执行完成,退出码：{}]", exitCode);

            // 检查指定目录下是否存在文件
            // TODO BY 张三丰  检查指定目录下是否存在文件     2024/11/29
        } catch (Exception e) {
            log.debug("[执行命令异常,信息:{}]", e.getMessage(), e);
            throw new RuntimeException(e);
        }

        return taskId;
    }

    @GetMapping("/get-progress")
    public Boolean getProgress(@RequestParam("taskId") String taskId) {

        byte[] bytes = FileUtil.readBytes(outPath + taskId);

        return bytes.length > 0;
    }

    @PostMapping("/file-upload")
    public ResponseEntity<String> fileUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("文件为空");
        }
        try {
            // 保存文件到指定路径
            Path filePath = Paths.get(path + file.hashCode() + "/" + file.getOriginalFilename());
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getBytes());

            log.info("[文件上传成功,文件路径:{}]", filePath.toAbsolutePath());

            return ResponseEntity.ok(filePath.toAbsolutePath().toString());
        } catch (IOException e) {
            log.debug("[文件上传异常,信息:{}]", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("文件上传失败: " + e.getMessage());
        }
    }


    /**
     * 添加到hashMap，并管理长度
     *
     * @param key   键
     * @param value 值
     */
    private void put(Integer key, String value) {

        if (hashMap.size() >= maxSize) {
            Integer firstKey = hashMap.keySet().iterator().next();
            hashMap.remove(firstKey);
        }

        hashMap.putIfAbsent(key, value);
    }
}
