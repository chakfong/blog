package com.chakfong.blog.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // This is invoked when user tries to access a secured REST resource without supplying any credentials
        // We should just send a 401 Unauthorized response because there is no 'login page' to redirect to
        // Here you can place any message you want
        log.error("{} {}",authException.getMessage(),authException.getClass());
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
//      throw new ServiceException(authException.getMessage(), ErrorCode.UNAUTHORIZED);
//      Result<?> result = ResultBuilder.onError(ErrorCode.UNAUTHORIZED.getCode(), authException.getMessage());
//      response.setHeader("Content-Type","application/json; charset=UTF-8");
//      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//      response.getWriter().print(result);
    }
}
