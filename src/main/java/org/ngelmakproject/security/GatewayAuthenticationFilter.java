package org.ngelmakproject.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class GatewayAuthenticationFilter extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(GatewayAuthenticationFilter.class);

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    String userId = request.getHeader("X-User-Id");
    String username = request.getHeader("X-User-Username");
    String authoritiesStr = request.getHeader("X-User-Authorities");

    log.info("\n" +
        "========< Gateway Auth Filter >=========\n" +
        "X-User-Id          : {}\n" +
        "X-User-Username    : {}\n" +
        "X-User-Authorities : {}\n" +
        "========================================", userId, username, authoritiesStr);

    if (userId != null && username != null && authoritiesStr != null) {

      Set<String> roles = Arrays.stream(authoritiesStr.split(","))
          .collect(Collectors.toSet());

      UserPrincipal principal = new UserPrincipal(Long.parseLong(userId), username, roles);

      List<SimpleGrantedAuthority> authorities = roles.stream()
          .map(SimpleGrantedAuthority::new)
          .toList();

      UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);

      SecurityContextHolder.getContext().setAuthentication(auth);
    }

    filterChain.doFilter(request, response);
  }
}
