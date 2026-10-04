package com.ayurclinic.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/api/auth/login"
                        ).permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/clinics")
                        .hasRole("CLINIC_ADMIN")
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/appointments"
                        )
                        .hasAnyRole("CLINIC_ADMIN", "DOCTOR", "RECEPTIONIST")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/appointments"
                        )
                        .hasAnyRole("CLINIC_ADMIN", "DOCTOR", "RECEPTIONIST")
                        .requestMatchers("/api/clinics/**")
                        .authenticated()
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/appointments/**")
                        .hasAnyRole(
                                "CLINIC_ADMIN",
                                "DOCTOR",
                                "RECEPTIONIST")
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/patients"
                        )
                        .hasAnyRole("CLINIC_ADMIN", "DOCTOR", "RECEPTIONIST")
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/patients/*/history"
                        )
                        .hasAnyRole("CLINIC_ADMIN", "DOCTOR", "RECEPTIONIST")


                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/patients/*/history"
                        )
                        .hasAnyRole("CLINIC_ADMIN", "DOCTOR", "RECEPTIONIST")
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/doctors"
                        )
                        .hasRole("CLINIC_ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/doctors",
                                "/api/v1/doctors/*"
                        )
                        .hasAnyRole("CLINIC_ADMIN", "DOCTOR", "RECEPTIONIST")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/doctors/*"
                        )
                        .hasRole("CLINIC_ADMIN")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/doctors/*/status"
                        )
                        .hasRole("CLINIC_ADMIN")
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/doctors/*/availability"
                        )
                        .hasRole("CLINIC_ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/doctors/*/availability",
                                "/api/v1/doctors/availability/*"
                        )
                        .hasAnyRole("CLINIC_ADMIN", "DOCTOR", "RECEPTIONIST")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/doctors/availability/*"
                        )
                        .hasRole("CLINIC_ADMIN")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/doctors/availability/*/status"
                        )
                        .hasRole("CLINIC_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/public/clinic").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/public/doctors").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/public/doctors/*").permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/public/doctors/*/availability"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/public/doctors/*/slots"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/public/appointments"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/public/appointments/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                ) .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );



        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }


}