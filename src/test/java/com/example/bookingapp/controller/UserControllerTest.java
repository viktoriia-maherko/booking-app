package com.example.bookingapp.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bookingapp.dto.user.RoleUpdateRequestDto;
import com.example.bookingapp.dto.user.UserUpdateRequestDto;
import com.example.bookingapp.model.Role;
import com.example.bookingapp.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {
    protected static MockMvc mockMvc;
    private static final Long USER_ID = 1L;
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    @Autowired
    private ObjectMapper objectMapper;

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
    @WithMockUser(username = "bob@gmail.com")
    @DisplayName("Update a role")
    void updateRoleById_ValidUserIdAndRequestDto_ReturnsRoleUpdateRequestDto()
            throws Exception {
        User user = createUser();
        Role updatedRole = new Role();
        updatedRole.setId(1L);
        updatedRole.setRoleName(Role.RoleName.ROLE_ADMIN);
        Set<Role> userRoles = new HashSet<>(user.getRoles());
        userRoles.add(updatedRole);
        user.setRoles(userRoles);

        RoleUpdateRequestDto requestDto = new RoleUpdateRequestDto();
        requestDto.setRoleNames(Role.RoleName.ROLE_ADMIN);

        List<String> expectedRoleNames = user.getRoles().stream()
                .map(Role::getRoleName)
                .map(Enum::name)
                .toList();

        mockMvc.perform(
                        put("/users/{id}/role", USER_ID)
                                .content(objectMapper.writeValueAsString(requestDto))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleNames",
                        containsInAnyOrder(expectedRoleNames.toArray())));
    }

    @Test
    @WithMockUser(username = "bob@gmail.com")
    @DisplayName("Get a profile")
    void getCurrentUserProfile_ReturnsUserResponseDto() throws Exception {
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

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(put("/users/me")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(status().isOk())
                .andReturn();
    }

    private User createUser() {
        Role role = new Role();
        role.setId(1L);
        role.setRoleName(Role.RoleName.ROLE_USER);
        User user = new User();
        user.setId(1L);
        user.setRoles(Set.of(role));
        user.setEmail("bob@gmail.com");
        user.setPassword(passwordEncoder.encode("qwerty12345"));
        user.setFirstName("Bob");
        user.setLastName("Smith");
        return user;
    }
}
