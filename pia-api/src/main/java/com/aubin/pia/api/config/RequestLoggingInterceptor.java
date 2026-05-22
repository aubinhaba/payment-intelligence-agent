package com.aubin.pia.api.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
    private static final String START_TIME_ATTR = "pia.requestStart";

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {
        long durationMs = System.currentTimeMillis() - (long) request.getAttribute(START_TIME_ATTR);
        int status = response.getStatus();

        if (ex != null || status >= 500) {
            log.error(
                    "http method={} path={} status={} duration_ms={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    status,
                    durationMs);
        } else if (status >= 400) {
            log.warn(
                    "http method={} path={} status={} duration_ms={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    status,
                    durationMs);
        } else {
            log.info(
                    "http method={} path={} status={} duration_ms={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    status,
                    durationMs);
        }
    }
}
