package com.chakfong.blog.web;

import com.chakfong.blog.dto.request.LoginDto;
import com.chakfong.blog.dto.request.RegisterDto;
import com.chakfong.blog.dto.request.SingleParamDto;
import com.chakfong.blog.dto.response.CaptchaDto;
import com.chakfong.blog.dto.response.Result;
import com.chakfong.blog.dto.response.ResultBuilder;
import com.chakfong.blog.dto.response.UserDto;
import com.chakfong.blog.entity.User;
import com.chakfong.blog.security.RequestIPHolder;
import com.chakfong.blog.security.filter.JWTFilter;
import com.chakfong.blog.security.filter.TokenProvider;
import com.chakfong.blog.service.AuthenticationService;
import com.chakfong.blog.service.UserService;
import com.chakfong.blog.utils.RequestUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.chakfong.blog.utils.CheckUtils.notNull;

/**
 * Controller to authenticate users.
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class AuthenticationRestController {

    private final TokenProvider tokenProvider;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    @Autowired
    UserService userService;

    @Autowired
    AuthenticationService authenticationService;

    public AuthenticationRestController(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder) {
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
    }

    @PostMapping("/authenticate")
    public Result<JWTToken> authorize(@RequestBody LoginDto loginDto, HttpServletRequest request, HttpServletResponse response) {
        RequestIPHolder.setContext(RequestUtils.getClientIP(request));
        if (authenticationService.isOverLogin()) {
            notNull(loginDto.getCode(), "验证码不能为空");
            authenticationService.validateCaptcha(loginDto.getCode());
        }
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());

        //AuthenticationManager将配合 UserDetailsService.loadUserByUsername() 进行认证。
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.createToken(authentication, false);

        response.setHeader(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
        return ResultBuilder.onSuc(new JWTToken(userService.getUserWithAuthorities().toDto(), jwt));
    }

    @PostMapping("/register")
    public Result<JWTToken> register(@RequestBody RegisterDto registerDto, HttpServletRequest request, HttpServletResponse response) {
        User user = userService.register(registerDto);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getUsername(), registerDto.getPassword());
        authenticationToken.setDetails(new WebAuthenticationDetails(request));
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.createToken(authentication, false);

        response.setHeader(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
        return ResultBuilder.onSuc(new JWTToken(jwt));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/sudo/register")
    public Result<Boolean> sudoRegister(@RequestParam Integer createCount) {
        userService.sudoRegister(createCount);
        return ResultBuilder.onSuc(true);
    }

    @PostMapping("/register/email/send")
    public Result<String> sendEmailCaptcha(@RequestBody SingleParamDto<String> email) {
        userService.sendEmailCaptcha(email.get());
        return ResultBuilder.onSuc();
    }

    @GetMapping("/captcha")
    public Result<CaptchaDto> getCaptcha(HttpServletRequest request) {

        RequestIPHolder.setContext(RequestUtils.getClientIP(request));
        CaptchaDto result = new CaptchaDto();
        if (authenticationService.isOverLogin()) {
            result.setRequired(true);
            String captcha = authenticationService.createCaptcha();
            result.setCaptcha(captcha);
        } else {
            result.setRequired(false);
        }
        return ResultBuilder.onSuc(result);
    }


    /**
     * Object to return as body in JWT Authentication.
     */
    public static class JWTToken {


        private UserDto user;

        @JsonProperty("user")
        public UserDto getUser() {
            return user;
        }

        public void setUser(UserDto user) {
            this.user = user;
        }

        private String idToken;

        JWTToken(String idToken) {
            this.idToken = idToken;
        }

        public JWTToken(UserDto user, String idToken) {
            this.user = user;
            this.idToken = idToken;
        }

        @JsonProperty("id_token")
        String getIdToken() {
            return idToken;
        }

        void setIdToken(String idToken) {
            this.idToken = idToken;
        }
    }
}
