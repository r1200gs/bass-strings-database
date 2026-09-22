package com.bassstrings.database.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.io.IOException;
import java.time.Instant;

/**
 * A browser hitting a protected page without a session gets redirected to {@code /login}
 * (the normal web UX); an API client hitting a protected {@code /api/**} endpoint gets a
 * plain 401 JSON body instead of that same redirect, which would otherwise look like a
 * confusing 200-with-HTML response to a REST client.
 */
public class RestAwareAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final AuthenticationEntryPoint htmlEntryPoint = new LoginUrlAuthenticationEntryPoint("/login");

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        if (isApiRequest(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
                    {"status":401,"message":"Authentication required","timestamp":"%s"}""".formatted(Instant.now()));
            return;
        }
        htmlEntryPoint.commence(request, response, authException);
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI().substring(request.getContextPath().length());
        return uri.startsWith("/api/");
    }
}
