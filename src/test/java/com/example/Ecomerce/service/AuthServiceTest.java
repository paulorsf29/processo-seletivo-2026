package com.example.Ecomerce.service;

import com.example.Ecomerce.dto.auth.AuthResponse;
import com.example.Ecomerce.dto.auth.LoginRequest;
import com.example.Ecomerce.dto.auth.RegisterRequest;
import com.example.Ecomerce.exception.EmailAlreadyInUseException;
import com.example.Ecomerce.model.Role;
import com.example.Ecomerce.model.User;
import com.example.Ecomerce.repository.UserRepository;
import com.example.Ecomerce.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_createsCustomerAndReturnsToken() {
        RegisterRequest request = new RegisterRequest("Paulo", "paulo@example.com", "senha123");
        when(userRepository.existsByEmail("paulo@example.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.email()).isEqualTo("paulo@example.com");
        assertThat(response.role()).isEqualTo(Role.CUSTOMER);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("hashed");
        assertThat(captor.getValue().getRole()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    void register_throwsWhenEmailAlreadyInUse() {
        RegisterRequest request = new RegisterRequest("Paulo", "paulo@example.com", "senha123");
        when(userRepository.existsByEmail("paulo@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyInUseException.class);

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_authenticatesAndReturnsToken() {
        LoginRequest request = new LoginRequest("paulo@example.com", "senha123");
        User user = User.builder().id(1L).name("Paulo").email("paulo@example.com").password("hashed").role(Role.CUSTOMER).build();

        when(userRepository.findByEmail("paulo@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.userId()).isEqualTo(1L);
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void login_propagatesBadCredentials() {
        LoginRequest request = new LoginRequest("paulo@example.com", "wrong");
        doThrow(new BadCredentialsException("bad")).when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(userRepository, never()).findByEmail(anyString());
    }
}
