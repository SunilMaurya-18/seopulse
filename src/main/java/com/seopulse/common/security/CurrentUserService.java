package com.seopulse.common.security;

import com.seopulse.common.exception.ResourceNotFoundException;
import com.seopulse.user.entity.User;
import com.seopulse.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor

public class CurrentUserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Long getUserId(Authentication authentication) {
        if (authentication == null || authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("User not found");
        }
        return userRepository
                .findByEmail(authentication.getName())
                .map(User::getId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
