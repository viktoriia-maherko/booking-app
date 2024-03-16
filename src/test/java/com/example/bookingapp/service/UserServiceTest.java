package com.example.bookingapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.bookingapp.dto.user.RoleUpdateRequestDto;
import com.example.bookingapp.dto.user.UserRegistrationRequestDto;
import com.example.bookingapp.dto.user.UserResponseDto;
import com.example.bookingapp.dto.user.UserUpdateRequestDto;
import com.example.bookingapp.exception.RegistrationException;
import com.example.bookingapp.mapper.UserMapper;
import com.example.bookingapp.model.Role;
import com.example.bookingapp.model.User;
import com.example.bookingapp.repository.RoleRepository;
import com.example.bookingapp.repository.UserRepository;
import com.example.bookingapp.service.impl.UserServiceImpl;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Verify user registration")
    void register_WhenUserDoesNotExist_ShouldRegisterUserSuccessfully() {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("bob@gmail.com");
        requestDto.setPassword("qwerty12345");
        requestDto.setFirstName("Bob");
        requestDto.setLastName("Smith");
        User user = createUser();
        UserResponseDto expected = createUserResponseDto();

        when(userRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.empty());
        doReturn(user).when(userRepository).save(any(User.class));
        when(userMapper.toDto(user)).thenReturn(expected);

        UserResponseDto actual = userService.register(requestDto);

        assertEquals(expected, actual);
        assertNotNull(actual);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("Verify updating role by id")
    void updateRoleById_ShouldUpdateUserRoleSuccessfully() {
        Role updatedRole = new Role();
        updatedRole.setId(1L);
        updatedRole.setRoleName(Role.RoleName.ROLE_USER);
        RoleUpdateRequestDto requestDto = new RoleUpdateRequestDto();
        requestDto.setRoleNames(updatedRole.getRoleName());
        User user = createUser();

        UserResponseDto expected = createUserResponseDto();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(roleRepository.findByRoleName(any())).thenReturn(updatedRole);
        doReturn(user).when(userRepository).save(any(User.class));
        when(userMapper.toDto(user)).thenReturn(expected);

        UserResponseDto actual = userService.updateRoleById(user.getId(), requestDto);

        assertEquals(expected, actual);
        assertNotNull(actual);
        verifyNoMoreInteractions(userRepository, userMapper, roleRepository);
    }

    @Test
    @DisplayName("Verify the correct user's profile was returned")
    void getProfile_ShouldReturnUserProfileSuccessfully() {
        User authenticatedUser = createUser();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                authenticatedUser.getEmail(), null
        );
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);

        UserResponseDto expectedProfile = createUserResponseDto();

        Mockito.when(userRepository.findByEmail(authenticatedUser.getEmail()))
                .thenReturn(Optional.of(authenticatedUser));
        Mockito.when(userMapper.toDto(authenticatedUser)).thenReturn(expectedProfile);

        UserResponseDto actualProfile = userService.getProfile();

        assertEquals(expectedProfile, actualProfile);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("Verify the user's profile was updated correctly")
    void updateProfile_ShouldUpdateUserProfileSuccessfully() {
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto();
        requestDto.setEmail("john.doe@example.com");
        requestDto.setFirstName("John");
        requestDto.setLastName("Smith");
        requestDto.setPassword(passwordEncoder.encode("qwerty12345678"));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("bob@gmail.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = createUser();

        when(userRepository.findByEmail("bob@gmail.com")).thenReturn(Optional.of(user));

        User updatedUser = createUser();
        updatedUser.setEmail("john.doe@example.com");
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserResponseDto expected = createUserResponseDto();
        expected.setEmail("john.doe@example.com");

        when(userMapper.toDto(updatedUser)).thenReturn(expected);

        UserResponseDto actual = userService.updateProfile(requestDto);

        assertEquals(expected, actual);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("Verify that user exists by ID")
    void existsById_WhenIdExists_ShouldReturnTrue() {
        Long id = 1L;
        when(userRepository.existsById(id)).thenReturn(true);
        boolean actual = userService.existsById(id);
        assertTrue(actual);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Verify that user does not exist by ID")
    void existsById_WhenIdDoesNotExist_ShouldReturnFalse() {
        Long id = 2L;
        when(userRepository.existsById(id)).thenReturn(false);
        boolean actual = userService.existsById(id);
        assertFalse(actual);
        verify(userRepository, times(1)).existsById(id);
    }

    @Test
    @DisplayName("Verify that authenticated user exists")
    void getAuthenticatedUserIfExists_WhenAuthenticatedUserExists_ShouldReturnUser() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("bob@gmail.com");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        User expected = createUser();
        when(userRepository.findByEmail("bob@gmail.com")).thenReturn(Optional.of(expected));

        User actual = userService.getAuthenticatedUserIfExists();

        assertNotNull(actual);
        assertEquals(expected.getEmail(), actual.getEmail());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Verify that no authenticated user exists")
    void getAuthenticatedUserIfExists_WhenNoAuthenticatedUser_ShouldThrowRegistrationException() {
        SecurityContextHolder.clearContext();
        assertThrows(RegistrationException.class, ()
                -> userService.getAuthenticatedUserIfExists());
    }

    @Test
    @DisplayName("Verify that authenticated user not found")
    void getAuthenticatedUserIfExists_WhenUserNotFound_ShouldThrowRegistrationException() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("nonexistent@example.com");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(RegistrationException.class, () -> userService.getAuthenticatedUserIfExists());
    }

    private User createUser() {
        Role role = new Role();
        role.setId(1L);
        role.setRoleName(Role.RoleName.ROLE_ADMIN);
        User user = new User();
        user.setId(1L);
        user.setRoles(Set.of(role));
        user.setEmail("bob@gmail.com");
        user.setPassword(passwordEncoder.encode("qwerty12345"));
        user.setFirstName("Bob");
        user.setLastName("Smith");
        return user;
    }

    private UserResponseDto createUserResponseDto() {
        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(1L);
        responseDto.setEmail("bob@gmail.com");
        responseDto.setFirstName("Bob");
        responseDto.setLastName("Smith");
        return responseDto;
    }
}
