package com.chakfong.blog.exception;

import com.chakfong.blog.dto.response.Result;
import com.chakfong.blog.dto.response.ResultBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice(annotations = {RestController.class, Service.class})
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {ServiceException.class})
    public final ResponseEntity<Result<?>> handleServiceException(ServiceException ex, HttpServletRequest request) {

        logError(ex, request);

        HttpHeaders headers = buildUTF8Headers();
        Result<?> result = ResultBuilder.onError(ex.getCode().getCode(), ex.getMessage());
        return new ResponseEntity<>(result, headers, HttpStatus.valueOf(ex.getCode().getHttpStatus()));
    }

    @ExceptionHandler(value = {AuthenticationException.class})
    public final ResponseEntity<Result<?>> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        return handleSecurityAuthentication(ex, request);
    }


    @ExceptionHandler(value = {AccessDeniedException.class})
    public final ResponseEntity<Result<?>> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        return handleSecurityAuthentication(ex, request);
    }

    private ResponseEntity<Result<?>> handleSecurityAuthentication(Exception ex, HttpServletRequest request) {
        logError(ex, request);

        HttpHeaders headers = buildUTF8Headers();
        Result<?> result = ResultBuilder.onError(ErrorCode.UNAUTHORIZED.getCode(), ex.getMessage());
        return new ResponseEntity<>(result, headers, HttpStatus.valueOf(ErrorCode.UNAUTHORIZED.getHttpStatus()));
    }
    /**
     * 处理 {@link CheckException}
     */
    @ExceptionHandler(value = {CheckException.class})
    public final ResponseEntity<Result<?>> handleCheckException(CheckException ex, HttpServletRequest request) {
        // 注入servletRequest，用于出错时打印请求URL与来源地址
        logError(ex, request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/json; charset=UTF-8"));
        Result<?> result = ResultBuilder.onError(ErrorCode.BAD_REQUEST.getCode(), ex.getMessage());
        return new ResponseEntity<>(result, headers, HttpStatus.valueOf(ErrorCode.BAD_REQUEST.getHttpStatus()));
    }

    @ExceptionHandler(value = {Exception.class})
    public final ResponseEntity<Result<?>> handleException(Exception ex, HttpServletRequest request) {
        // 注入servletRequest，用于出错时打印请求URL与来源地址
        logError(ex, request);
        log.error("catch Exception", ex);

        HttpHeaders headers = buildUTF8Headers();
        Result<?> result = ResultBuilder.onError(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), ex.getMessage());
        return new ResponseEntity<>(result, headers, HttpStatus.valueOf(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus()));
    }

    /**
     * 重载ResponseEntityExceptionHandler的方法，加入日志
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatus status, WebRequest request) {

        logError(ex);

        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute("javax.servlet.error.exception", ex, WebRequest.SCOPE_REQUEST);
        }

        Result<?> result = ResultBuilder.onError(body, status.value(), ex.getMessage());
        return new ResponseEntity<>(result, headers, status);
    }

    private void logError(Exception ex) {
        Map<String, String> map = new HashMap<>();
        map.put("message", ex.getMessage());

        log.error(map.toString());

    }

    private void logError(Exception ex, HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        map.put("message", ex.getMessage());
        map.put("from", request.getRemoteAddr());
        String queryString = request.getQueryString();
        map.put("path", queryString != null ? (request.getRequestURI() + "?" + queryString) : request.getRequestURI());

        log.error(map.toString());
    }

    private HttpHeaders buildUTF8Headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/json; charset=UTF-8"));
        return headers;
    }


}
