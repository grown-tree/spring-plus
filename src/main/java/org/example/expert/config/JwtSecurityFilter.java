package org.example.expert.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtSecurityFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String bearerJwt = request.getHeader("Authorization");

        if (bearerJwt != null) {
            try {
                String jwt = jwtUtil.substringToken(bearerJwt);
                Claims claims = jwtUtil.extractClaims(jwt);

                if (claims != null) {
                    UserRole userRole = UserRole.valueOf(claims.get("userRole", String.class));

                    // 기존 setAttribute 대신 SecurityContext에 저장!
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    claims.getSubject(),  // userId
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + userRole.name()))
                            );

                    SecurityContextHolder.getContext().setAuthentication(auth);
                    request.setAttribute("email", claims.get("email"));
                    request.setAttribute("nickname", claims.get("nickname"));
                }
            } catch (ExpiredJwtException e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "만료된 JWT 토큰입니다.");
                return;
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않는 JWT 토큰입니다.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
