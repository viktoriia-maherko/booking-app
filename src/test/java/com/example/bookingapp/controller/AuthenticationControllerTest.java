package com.example.bookingapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bookingapp.dto.user.UserLoginRequestDto;
import com.example.bookingapp.dto.user.UserLoginResponseDto;
import com.example.bookingapp.dto.user.UserRegistrationRequestDto;
import com.example.bookingapp.dto.user.UserResponseDto;
import com.example.bookingapp.security.AuthenticationService;
import com.example.bookingapp.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationControllerTest {
    protected static MockMvc mockMvc;
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserService userService;
    @MockBean
    private AuthenticationService authenticationService;

    @BeforeEach
    void beforeEach(
            @Autowired DataSource dataSource,
            @Autowired WebApplicationContext applicationContext
    ) throws SQLException {
        when(passwordEncoder.encode("qwerty12345678")).thenReturn("encodedPassword");
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/users/add-user-bob-to-users-table.sql")
            );
        }
    }

    @AfterEach
    void afterEach(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/users/remove-user-bob-from-users-table.sql")
            );
        }
    }

    @Test
    @DisplayName("Verify user authentication")
    void login_ValidCredentials_ReturnsUserLoginResponseDto() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto("bob@gmail.com", "password12345");

        when(authenticationService.authenticate(any(UserLoginRequestDto.class)))
                .thenReturn(new UserLoginResponseDto("mock_jwt_token"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock_jwt_token"));

        verify(authenticationService).authenticate(any(UserLoginRequestDto.class));
    }

    @Test
    @DisplayName("Verify authentication fails with invalid data")
    void login_InvalidCredentials_ReturnsStatusIsBadRequest() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto(
                "invalid_email@gmail.com", "invalid_password"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Verify user registration")
    void register_ValidCredentials_ReturnsUserResponseDto() throws Exception {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("alice@gmail.com");
        requestDto.setFirstName("Alice");
        requestDto.setLastName("Alison");
        requestDto.setPassword("qwerty111");
        requestDto.setRepeatPassword("qwerty111");

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setEmail("alice@gmail.com");
        responseDto.setFirstName("Alice");
        responseDto.setLastName("Alison");

        when(userService.register(any(UserRegistrationRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(requestDto.getEmail()));
        verify(userService).register(any(UserRegistrationRequestDto.class));
    }

    @Test
    @DisplayName("Verify registration fails with invalid data")
    void registerWithInvalidData() throws Exception {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("bob@gmail.com");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }
}
