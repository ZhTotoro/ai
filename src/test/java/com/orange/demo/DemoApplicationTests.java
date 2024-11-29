package com.orange.demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Locale;
import java.util.UUID;

@Slf4j
@SpringBootTest
class DemoApplicationTests {

    @Value("${file.path}")
    private String path;

    @Value("${file.out.path}")
    private String outPath;

    @Test
    void contextLoads() {

        String uuid = UUID.randomUUID().toString().replace("-", "").toLowerCase(Locale.ROOT);

        String command = "cd /home/tim/下载/inswapper && source /home/tim/下载/inswapper/venv/bin/activate &&  python /home/tim/下载/inswapper/swapper.py \\\n" +
                "--source_img \"" + path + uuid + ".png\"" + "\\\n" +
                "--target_img \"" + path + uuid + ".png\"" + "\\\n" +
                "--output_img \"" + outPath + uuid + " \"\\\n" +
                "--face_restore \\\n" +
                "--background_enhance \\\n" +
                "--face_upsample \\\n" +
                "--upscale=2 \\\n" +
                "--codeformer_fidelity=0.5";


        log.info("[命令:{}]", command);



    }

}
