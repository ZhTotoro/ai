package com.orange.demo.demos.web.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.http.HttpUtil;
import com.orange.demo.demos.web.base.CommonResult;
import com.orange.demo.demos.web.exception.ServiceException;
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
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
@CrossOrigin
@RestController
public class BasicController {

    private static final ConcurrentHashMap<Integer, String> hashMap = new ConcurrentHashMap<>();

    private static final ExecutorService executorService = new ThreadPoolExecutor(
            6, // 核心线程数
            6, // 最大线程数
            60L, // 线程空闲时间
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(12), // 队列大小
            new ThreadFactory() {
                private int threadNumber = 1;

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "TaskThread-" + threadNumber++);
                }
            },
            new ThreadPoolExecutor.AbortPolicy() { // 自定义拒绝策略
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                    log.error("[线程池已满，拒绝执行任务]");
                    throw new RejectedExecutionException("线程池已满，拒绝执行任务");
                }
            }
    );

    @Value("${file.path}")
    private String path;

    @Value("${file.out.path}")
    private String outPath;

    @Value("${hashmap.maxSize}")
    private Integer maxSize;

    @Value("${file.media}")
    private String media;

    // 定期输出线程池状态
//    public BasicController() {
//        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//        scheduler.scheduleAtFixedRate(() -> log.info("[线程池状态] - 活动线程数: {}, 队列大小: {}, 完成任务数: {}, 任务总数: {}",
//                ((ThreadPoolExecutor) executorService).getActiveCount(),
//                ((ThreadPoolExecutor) executorService).getQueue().size(),
//                ((ThreadPoolExecutor) executorService).getCompletedTaskCount(),
//                ((ThreadPoolExecutor) executorService).getTaskCount()
//        ), 0, 10, TimeUnit.SECONDS); // 每10秒输出一次状态
//    }

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

//        String taskId = avatar.hashCode() + "and" + image.hashCode() + ".png";
        String taskId = UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);

        // 执行shell命令
        String command = "cd /root/ai/inswapper && source /root/ai/inswapper/venv/bin/activate &&  python /root/ai/inswapper/swapper.py \\\n" +
                "--source_img \"" + path + avatarUuid + ".png\"" + " \\\n" +
                "--target_img \"" + path + imageUuid + ".png\"" + " \\\n" +
                "--output_img \"" + outPath + taskId + ".png\" \\\n" +
//                "--face_restore \\\n" +
                "--background_enhance \\\n" +
                "--face_upsample \\\n" +
                "--upscale=1 \\\n" +
                "--codeformer_fidelity=0";

        CompletableFuture.runAsync(() -> {
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
                throw new RejectedExecutionException(e);
            }
        }, executorService).exceptionally(t -> {
            if (t instanceof RejectedExecutionException) {
                log.warn("[线程池已满,拒绝执行任务]");
            }
            return null;
        });


        return taskId;
    }

    @GetMapping("/get-progress")
    public ResponseEntity<String> getProgress(@RequestParam("taskId") String taskId) {

        try {
            FileUtil.readBytes(outPath + taskId + ".png");
        } catch (IORuntimeException e) {
            log.warn("[文件不存在,信息:{}", e.getMessage());
            return ResponseEntity.ok("文件不存在");
        }

        String url = media + "output/" + taskId + ".png";
        log.info("[文件映射url:{}", url);
        return ResponseEntity.ok(url);

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

            String url = media + file.hashCode() + "/" + file.getOriginalFilename();

            log.info("[文件映射url:{}]", url);

            return ResponseEntity.ok(url);
        } catch (IOException e) {
            log.debug("[文件上传异常,信息:{}]", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.OK).body("文件上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/exception")
    public CommonResult<?> exception() {
        throw new ServiceException(500, "自定义异常");
    }

    @GetMapping("/error")
    public CommonResult<?> error() {
        throw new RuntimeException("我直接报错");
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
