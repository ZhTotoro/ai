package com.orange.demo.demos.web.handler;

import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.Servlet;
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
        log.info("[开始请求，Request Url:{},Parameter:{}]", request.getRequestURL(), JSONUtil.toJsonStr(request.getParameterMap()));
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        log.info("[结束请求：Request Url:{},Parameter:{}]", request.getRequestURL(), JSONUtil.toJsonStr(request.getParameterMap()));
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
