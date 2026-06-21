package io.skillia.security.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Workaround to minimize Vaadin intervention on the REST API endpoints
public class MyBearerSecFilter extends OncePerRequestFilter {
    private final AuthenticationEntryPoint authenticationEntryPoint = new BearerTokenAuthenticationEntryPoint();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String servletPath = request.getServletPath();
        // Ignore non-API / API auth requests
        if (!servletPath.startsWith(("/api/")) || servletPath.startsWith(("/api/auth/"))) {
            filterChain.doFilter(request, response);
            return;
        }

        String auth = request.getHeader("Authorization");
        boolean hasBearer = auth != null && auth.startsWith("Bearer ");

        if (!hasBearer) {
            this.authenticationEntryPoint.commence(request, response, new AuthenticationException("Missing bearer token") {
                @Override
                public String getMessage() {
                    return "Missing bearer token";
                }
            });
            return;
        }

        // If bearer present, we continue to the actual Bearer token filter
        // No role checking here, we would still wrongly fallback to Vaadin login for that

        filterChain.doFilter(request, response);
    }

}
