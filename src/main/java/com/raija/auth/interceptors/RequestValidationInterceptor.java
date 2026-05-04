package com.raija.auth.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class RequestValidationInterceptor implements HandlerInterceptor {

    private static final List<String> BLOCKED_METHODS =
            List.of("TRACE", "TRACK", "OPTIONS");
    private static final List<String> BLOCKED_USER_AGENTS =
            List.of("python-requests", "curl", "Go-http-client", "scrapy");
    private static final Pattern SQL_INJECTION =
            Pattern.compile("(?i)(union|select|insert|drop|exec|script|--|\\/\\*)");
    private static final Pattern PATH_TRAVERSAL =
            Pattern.compile("(\\.\\./|%2e%2e%2f|%252e%252e)");
    private static final List<String> allowedWithoutHeaders = new ArrayList<>(List.of("/api/auth/login"));

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        if (BLOCKED_METHODS.contains(request.getMethod().toUpperCase())) {
            sendError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    "Method not allowed");
            return false;
        }
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isBlank()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Missing User-Agent");
            return false;
        }
        if (BLOCKED_USER_AGENTS.stream().anyMatch(userAgent::startsWith)) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Client not allowed");
            return false;
        }
        String apiAuth = request.getHeader("X-Api-Authorization");
        if ((apiAuth == null || apiAuth.isBlank()) && !allowedWithoutHeaders.contains(request.getRequestURI())) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Missing API Authorization");
            return false;
        }
        String query = request.getQueryString();
        if (query != null && SQL_INJECTION.matcher(query).find()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid request parameters");
            return false;
        }
        String uri = request.getRequestURI();
        if (PATH_TRAVERSAL.matcher(uri).find()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid request path");
            return false;
        }
        String method = request.getMethod();
        if (List.of("POST", "PUT", "PATCH").contains(method.toUpperCase())) {
            String contentType = request.getContentType();
            if (contentType == null || !contentType.contains("application/json")) {
                sendError(response, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                        "Content-Type must be application/json");
                return false;
            }
        }
        if (handler instanceof HandlerMethod handlerMethod) {
            String controllerName = handlerMethod.getBeanType().getSimpleName();
            String methodName = handlerMethod.getMethod().getName();
            // can add controller/method specific logic here... like extra validation only for AdminController
            if (controllerName.equals("AdminController")) {
                String adminKey = request.getHeader("X-Admin-Key");
                if (adminKey == null) {
                    sendError(response, HttpServletResponse.SC_FORBIDDEN,
                            "Missing admin key");
                    return false;
                }
            }
        }
        return true;
    }

    //running after controller good for logging
    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        // logging every request with status
        System.out.printf("[%s] %s %s → %d%n",
                LocalDateTime.now(),
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus());
    }

    private void sendError(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}
