package com.example.Ecomerce.service;

import com.example.Ecomerce.dto.auth.AuthResponse;
import com.example.Ecomerce.dto.auth.LoginRequest;
import com.example.Ecomerce.dto.auth.RegisterRequest;
import com.example.Ecomerce.exception.EmailAlreadyInUseException;
import com.example.Ecomerce.model.Role;
import com.example.Ecomerce.model.User;
import com.example.Ecomerce.repository.UserRepository;
import com.example.Ecomerce.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyInUseException("Já existe uma conta com esse email");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.CUSTOMER)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(toUserDetails(user));
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado após autenticação"));

        String token = jwtService.generateToken(toUserDetails(user));
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    private UserDetails toUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .build();
    }
}
