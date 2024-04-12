package com.example.bookingapp.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bookingapp.dto.user.RoleUpdateRequestDto;
import com.example.bookingapp.dto.user.UserResponseDto;
import com.example.bookingapp.dto.user.UserResponseDtoWithRoles;
import com.example.bookingapp.dto.user.UserUpdateRequestDto;
import com.example.bookingapp.model.Role;
import com.example.bookingapp.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {
    protected static MockMvc mockMvc;
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserService userService;

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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Update a role")
    void updateRoleById_ValidUserIdAndRequestDto_ReturnsRoleUpdateRequestDto()
            throws Exception {
        Long userId = 1L;
        Role.RoleName roleName = Role.RoleName.ROLE_ADMIN;
        RoleUpdateRequestDto requestDto = new RoleUpdateRequestDto();
        requestDto.setRoleNames(Set.of(roleName.name()));

        UserResponseDtoWithRoles responseDto = new UserResponseDtoWithRoles();
        responseDto.setRoleNames(Set.of(roleName.name()));

        when(userService.updateRoleById(userId, requestDto)).thenReturn(responseDto);

        // Act
        ResultActions resultActions = mockMvc.perform(
                put("/users/{id}/role", userId)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.roleNames").isArray())
                .andExpect(jsonPath("$.roleNames[0]").value(roleName.name()));

        Mockito.verify(userService, times(1)).updateRoleById(userId, requestDto);
        verifyNoMoreInteractions(userService);
    }

    @Test
    @WithMockUser(username = "bob@gmail.com")
    @DisplayName("Get a profile")
    void getCurrentUserProfile_ReturnsUserResponseDto() throws Exception {
        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(1L);
        responseDto.setEmail("bob@gmail.com");
        responseDto.setFirstName("Bob");
        responseDto.setLastName("Smith");
        when(userService.getProfile()).thenReturn(responseDto);
        mockMvc.perform(get("/users/me"))
                .andExpect(jsonPath("$.email").value("bob@gmail.com"))
                .andExpect(jsonPath("$.firstName").value("Bob"))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    @WithMockUser(username = "bob@gmail.com")
    @DisplayName("Update a profile")
    void updateCurrentUserProfile_ReturnsUserResponseDto() throws Exception {
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto();
        requestDto.setEmail("john.doe@example.com");
        requestDto.setFirstName("John");
        requestDto.setLastName("Smith");
        requestDto.setPassword(passwordEncoder.encode("qwerty12345678"));
        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(1L);
        responseDto.setEmail("john.doe@example.com");
        responseDto.setFirstName("John");
        responseDto.setLastName("Smith");

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        when(userService.updateProfile(requestDto)).thenReturn(responseDto);

        mockMvc.perform(put("/users/me")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(status().isOk())
                .andReturn();
    }
}
