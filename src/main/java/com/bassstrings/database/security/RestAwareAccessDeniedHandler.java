package com.bassstrings.database.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;

import java.io.IOException;
import java.time.Instant;

/** Mirrors {@link RestAwareAuthenticationEntryPoint}, but for the authenticated-yet-forbidden case. */
public class RestAwareAccessDeniedHandler implements AccessDeniedHandler {

    private final AccessDeniedHandler htmlHandler = new AccessDeniedHandlerImpl();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {
        if (isApiRequest(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("""
                    {"status":403,"message":"Not permitted","timestamp":"%s"}""".formatted(Instant.now()));
            return;
        }
        htmlHandler.handle(request, response, accessDeniedException);
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI().substring(request.getContextPath().length());
        return uri.startsWith("/api/");
    }
}
