package com.petfeeding.platform.security.config;

import com.petfeeding.platform.security.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import javax.servlet.http.HttpServletResponse;

/**
 * Spring Security 配置
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（前后端分离 + JWT 不需要）
            .csrf().disable()
            // 无状态会话
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // 请求权限配置
            .authorizeRequests()
            // 放行登录、小程序所有API、API 文档
            .antMatchers(
                    "/api/user/login",
                    "/api/miniapp/**",            // 小程序全部接口放行（Token 校验在 Controller 层处理）
                    "/doc.html",
                    "/webjars/**",
                    "/v3/api-docs/**",
                    "/swagger-resources/**"
            ).permitAll()
            // OPTIONS 预检请求放行（避免带 Token 的跨域预检被拦截）
            .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            // 管理接口仅管理员可访问（杜绝越权操作）
            .antMatchers(
                    "/api/user/**",
                    "/api/feeder/**",
                    "/api/order/**",
                    "/api/review/**",
                    "/api/payment/**",
                    "/api/dashboard/**",
                    "/api/admin/**",
                    "/api/pet/**"
            ).hasRole("ADMIN")
            // 其余接口需要认证
            .anyRequest().authenticated()
            .and()
            // 未认证统一返回 401（而非 Spring Security 默认的 403），便于前端识别并跳转登录
            .exceptionHandling()
                .authenticationEntryPoint((req, resp, ex) ->
                    resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "未认证或登录已过期"))
            .and()
            // 添加 JWT 过滤器（在 UsernamePasswordAuthenticationFilter 之前）
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
