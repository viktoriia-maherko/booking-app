package com.example.bookingapp.service.impl;

import com.example.bookingapp.dto.user.RoleUpdateRequestDto;
import com.example.bookingapp.dto.user.UserRegistrationRequestDto;
import com.example.bookingapp.dto.user.UserResponseDto;
import com.example.bookingapp.dto.user.UserUpdateRequestDto;
import com.example.bookingapp.exception.EntityNotFoundException;
import com.example.bookingapp.exception.RegistrationException;
import com.example.bookingapp.mapper.UserMapper;
import com.example.bookingapp.model.Role;
import com.example.bookingapp.model.User;
import com.example.bookingapp.repository.RoleRepository;
import com.example.bookingapp.repository.UserRepository;
import com.example.bookingapp.service.UserService;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponseDto register(UserRegistrationRequestDto request)
            throws RegistrationException {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RegistrationException("Unable to complete registration.");
        }
        User user = new User();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    public UserResponseDto updateRoleById(Long id, RoleUpdateRequestDto requestDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with id "
                        + id + " not found"));

        Role newRole = roleRepository.findByRoleName(requestDto.getRoleNames());
        Set<Role> userRoles = new HashSet<>(user.getRoles());
        userRoles.add(newRole);

        user.setRoles(userRoles);

        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    @Override
    public UserResponseDto getProfile() {
        return userMapper.toDto(getAuthenticatedUserIfExists());
    }

    @Override
    public UserResponseDto updateProfile(UserUpdateRequestDto requestDto) {
        User user = getAuthenticatedUserIfExists();
        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());
        user.setEmail(requestDto.getEmail());
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    public User getAuthenticatedUserIfExists() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new RegistrationException("Can't find authenticated user");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RegistrationException("User with email "
                        + authentication.getName() + " doesn't exist"));
    }
}
