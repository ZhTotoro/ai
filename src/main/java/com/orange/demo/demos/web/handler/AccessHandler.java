package com.orange.demo.demos.web.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.orange.demo.demos.web.utils.ServletUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author TOTORO
 * @since 2024/11/29 14:28
 */
@Slf4j
@Component
public class AccessHandler implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        Map<String, String> paramMap = ServletUtil.getParamMap(request);

        String body = ServletUtils.isJsonRequest(request) ? ServletUtils.getBody(request) : null;

        if (CollUtil.isNotEmpty(paramMap) || StrUtil.isNotBlank(body)) {
            log.info("[AccessHandler] [开始请求 URL({}), 参数({})]", request.getRequestURI(), StrUtil.blankToDefault(body, paramMap.toString()));
        } else {
            log.info("[AccessHandler] [开始请求 URL({}), 无参数]", request.getRequestURI());
        }

        // 计时
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        request.setAttribute("stopWatch", stopWatch);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        StopWatch stopWatch = (StopWatch) request.getAttribute("stopWatch");
        stopWatch.stop();

        log.info("[AccessHandler] [完成 URL({}), 耗时({} ms)]", request.getRequestURI(), stopWatch.getLastTaskTimeMillis());
    }
}
