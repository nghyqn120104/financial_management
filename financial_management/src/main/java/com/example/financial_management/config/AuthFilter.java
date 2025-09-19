package com.example.financial_management.config;

import com.example.financial_management.model.AbstractResponse;
import com.example.financial_management.model.auth.Auth;
import com.example.financial_management.util.GsonUtil;
import com.example.financial_management.util.JwtTokenUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            handleUnauthorized(response, "Authorization header missing or invalid format");
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtTokenUtil.validateToken(token)) {
            handleUnauthorized(response, "Invalid or expired JWT token");
            return;
        }

        Auth auth = jwtTokenUtil.extractAuth(token);
        if (auth == null || !auth.isActive()) {
            handleUnauthorized(response, "Invalid user information in token");
            return;
        }

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + auth.getProfile()));

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(auth, token,
                authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();

        return SecurityConfig.PUBLIC_ENDPOINTS.stream()
                .anyMatch(endpoint -> {
                    if (endpoint.endsWith("/**")) {
                        return path.startsWith(endpoint.substring(0, endpoint.length() - 3));
                    }
                    return path.equals(endpoint) || path.startsWith(endpoint);
                });
    }

    private void handleUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String jsonResponse = GsonUtil.GSON.toJson(
                new AbstractResponse<>().setSuccess(false).setMessage(message));
        response.getWriter().write(jsonResponse);
    }
}