package com.orange.demo.demos.web.handler;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @author TOTORO
 * @since 2024/11/29 14:32
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private AccessHandler accessHandler;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accessHandler)
                .addPathPatterns("/**") //拦截所有请求
                .excludePathPatterns("/static/**", "/css/**", "/js/**", "/images/**", "/fonts/**", "/plugins/**"); //放行静态资源
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedHeaders("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(false);
    }
}
