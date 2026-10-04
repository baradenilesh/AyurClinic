package com.ayurclinic.auth.service;

import com.ayurclinic.auth.dto.LoginRequest;
import com.ayurclinic.auth.dto.LoginResponse;
import com.ayurclinic.auth.security.JwtService;
import com.ayurclinic.user.entity.User;
import com.ayurclinic.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = userRepository
                .findByEmail(request.email())
                .orElseThrow();

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                token,
                "Bearer",
                3600,
                user.getId(),
                user.getTenantId(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
