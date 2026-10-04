package com.ayurclinic.auth.security;

import com.ayurclinic.user.entity.User;
import com.ayurclinic.user.entity.UserStatus;
import com.ayurclinic.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found"
                        ));

        return new CustomUserPrincipal(
                user.getId(),
                user.getTenantId(),
                user.getEmail(),
                user.getPasswordHash(),
                java.util.List.of(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_" + user.getRole().name()
                        )
                ),
                user.getStatus() == UserStatus.ACTIVE
        );
    }
}
