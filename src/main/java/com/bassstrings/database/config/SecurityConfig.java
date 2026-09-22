package com.bassstrings.database.config;

import com.bassstrings.database.security.AccountProvisioningOAuth2UserService;
import com.bassstrings.database.security.AccountProvisioningOidcUserService;
import com.bassstrings.database.security.AppleAwareAuthorizationRequestResolver;
import com.bassstrings.database.security.RestAwareAccessDeniedHandler;
import com.bassstrings.database.security.RestAwareAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Three subscriber roles drive authorization here: USER (default on sign-up), MONITOR
 * (can moderate/remove reviews and comments), and ADMIN (full catalog management plus
 * everything MONITOR can do). Role checks on individual resources you don't own (e.g.
 * "is this reviewer, or a monitor/admin, allowed to delete this review?") live in the
 * service layer, since they need the resource loaded first to know its author - URL
 * patterns below only express what a role can do in general.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                     ClientRegistrationRepository clientRegistrationRepository,
                                                     AccountProvisioningOidcUserService oidcUserService,
                                                     AccountProvisioningOAuth2UserService oAuth2UserService) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/error",
                                "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        // Browsing the catalog never requires a subscriber account...
                        .requestMatchers(HttpMethod.GET, "/strings", "/strings/**", "/models", "/models/**")
                        .permitAll()
                        // ...but posting a review, a comment, or moderating one does. The web forms post
                        // straight to these paths (not through /api/v1) and are handled by the web MVC
                        // controllers in the `web` package, which call the same service layer as the REST API.
                        .requestMatchers(HttpMethod.POST, "/strings/**", "/reviews/**", "/comments/**")
                        .authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/bass-models", "/api/v1/bass-strings")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/bass-models/**", "/api/v1/bass-strings/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/bass-models/**", "/api/v1/bass-strings/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/bass-strings/*/reviews", "/api/v1/reviews/*/comments")
                        .authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/reviews/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/reviews/**", "/api/v1/comments/**").authenticated()
                        .requestMatchers("/api/v1/users/me").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .authorizationEndpoint(ae -> ae.authorizationRequestResolver(
                                new AppleAwareAuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization")))
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(oidcUserService)
                                .userService(oAuth2UserService))
                        .defaultSuccessUrl("/", true)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        // Apple POSTs the authorization response (response_mode=form_post) straight
                        // from appleid.apple.com, so it carries no CSRF token of ours to check.
                        .ignoringRequestMatchers("/login/oauth2/code/apple")
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new RestAwareAuthenticationEntryPoint())
                        .accessDeniedHandler(new RestAwareAccessDeniedHandler())
                );

        return http.build();
    }
}
