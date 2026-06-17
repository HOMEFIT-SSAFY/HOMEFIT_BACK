package com.happyhome.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpaForwardFilter extends OncePerRequestFilter {

    private static final Set<String> SPA_ROOT_SEGMENTS = Set.of(
            "home",
            "prices",
            "trades",
            "rentals",
            "transfers",
            "lh-calendar",
            "analysis",
            "notices",
            "login",
            "register",
            "member"
    );

    private final Resource indexResource = new ClassPathResource("static/app/index.html");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!isSpaRoute(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!indexResource.exists()) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Frontend build is not installed.");
            return;
        }
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        indexResource.getInputStream().transferTo(response.getOutputStream());
    }

    private boolean isSpaRoute(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.equals("/") || path.isBlank()) {
            return true;
        }
        if (path.startsWith("/api/")
                || path.equals("/api")
                || path.startsWith("/app/")
                || path.startsWith("/uploads/")
                || path.startsWith("/login/oauth2/")
                || path.startsWith("/oauth2/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.contains(".")) {
            return false;
        }
        String firstSegment = path.substring(1).split("/", 2)[0];
        return SPA_ROOT_SEGMENTS.contains(firstSegment);
    }
}
