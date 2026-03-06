package com.talkhub.backend.auth;

import com.talkhub.backend.domain.user.UserEntity;
import com.talkhub.backend.domain.user.UserRepository;
import com.talkhub.backend.security.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername().trim();
        String password = request.getPassword();

        UserEntity user = userRepository.findByUsername(username)
            .orElseThrow(() -> new InvalidCredentialsException("Username or password is incorrect"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Username or password is incorrect");
        }

        String token = jwtService.generateToken(user.getId(), user.getUsername());
        LoginResponse.UserProfile profile = new LoginResponse.UserProfile(
            user.getId(),
            user.getUsername(),
            user.getNickname(),
            user.getAvatarUrl()
        );
        return new LoginResponse(token, profile);
    }
}
