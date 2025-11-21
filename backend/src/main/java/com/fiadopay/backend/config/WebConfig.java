package com.fiadopay.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new FakeAuthInterceptor());
    }

    static class FakeAuthInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            String uri = request.getRequestURI();
            if (uri.startsWith("/h2")
                    || uri.startsWith("/swagger-ui")
                    || uri.equals("/swagger-ui.html")
                    || uri.startsWith("/v3/api-docs")
                    || uri.startsWith("/webhook")) {
                return true;
            }
            String auth = request.getHeader("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                response.setStatus(401);
                return false;
            }
            return true;
        }
    }
}