package com.happyhome.config;

import com.happyhome.security.LoginSuccessHandler;
import com.happyhome.security.OAuth2LoginSuccessHandler;
import com.happyhome.security.JwtAuthenticationFilter;
import com.happyhome.security.JwtProvider;
import com.happyhome.security.ProviderCompatibleAuthorizationRequestResolver;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            LoginSuccessHandler loginSuccessHandler,
            OAuth2LoginSuccessHandler oauth2LoginSuccessHandler,
            JwtProvider jwtProvider,
            ObjectProvider<ProviderCompatibleAuthorizationRequestResolver> authorizationRequestResolver,
            ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository
    )
            throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/bus-stops/sync").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/notices/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/notices/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/notices/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/members/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/members/me").authenticated()
                        .requestMatchers("/api/members/me/**").authenticated()
                        .requestMatchers("/api/favorites/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/loans/property-analysis").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/transfers/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/transfers/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/transfers/**").authenticated()
                        .requestMatchers(
                                "/", "/home", "/login", "/register", "/password-find",
                                "/prices", "/trades", "/rentals", "/rentals/*", "/analysis",
                                "/notices", "/notices/*", "/notices/*/*",
                                "/api/**", "/oauth2/**", "/login/oauth2/**",
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                                "/webjars/**", "/css/**", "/img/**"
                        ).permitAll()
                        .requestMatchers("/member/**").authenticated()
                        .anyRequest().permitAll())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("userId")
                        .passwordParameter("password")
                        .successHandler(loginSuccessHandler)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutRequestMatcher(PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/logout"))
                        .logoutSuccessUrl("/home")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"))
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                PathPatternRequestMatcher.pathPattern("/api/**"),
                                PathPatternRequestMatcher.pathPattern("/notices/**")
                        ))
                .exceptionHandling(exception -> exception
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                PathPatternRequestMatcher.pathPattern("/api/**")
                        ))
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtProvider),
                        UsernamePasswordAuthenticationFilter.class
                );

        if (clientRegistrationRepository.getIfAvailable() != null) {
            http.oauth2Login(oauth -> oauth
                    .loginPage("/login")
                    .authorizationEndpoint(endpoint -> endpoint
                            .authorizationRequestResolver(authorizationRequestResolver.getIfAvailable()))
                    .successHandler(oauth2LoginSuccessHandler)
                    .failureUrl("/login?oauthError"));
        }

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder("bcrypt", encoders);
        passwordEncoder.setDefaultPasswordEncoderForMatches(legacyPlainTextPasswordEncoder());
        return passwordEncoder;
    }

    private PasswordEncoder legacyPlainTextPasswordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword.toString();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return rawPassword != null
                        && encodedPassword != null
                        && encodedPassword.equals(rawPassword.toString());
            }
        };
    }
}
