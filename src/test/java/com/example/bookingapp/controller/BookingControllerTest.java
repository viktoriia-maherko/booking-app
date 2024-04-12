package com.example.bookingapp.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bookingapp.dto.booking.CreateBookingRequestDto;
import com.example.bookingapp.dto.booking.UpdateBookingRequestDto;
import com.example.bookingapp.model.Booking;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookingControllerTest {
    protected static MockMvc mockMvc;
    private static final Long USER_ID = 1L;
    private static final Long ACCOMMODATION_ID = 1L;
    private static final Long BOOKING_ID = 1L;
    private static final LocalDate CREATE_CHECK_IN = LocalDate.of(2024, 10, 1);
    private static final LocalDate CREATE_CHECK_OUT = LocalDate.of(2024, 10, 10);
    private static final LocalDate BOOKING_CHECK_IN = LocalDate.of(2025, 3, 18);
    private static final LocalDate BOOKING_CHECK_OUT = LocalDate.of(2025, 3, 25);
    private static final LocalDate UPDATE_BOOKING_CHECK_IN = LocalDate.of(2025, 4, 18);
    private static final LocalDate UPDATE_BOOKING_CHECK_OUT = LocalDate.of(2025, 4, 25);

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void beforeEach(
            @Autowired DataSource dataSource,
            @Autowired WebApplicationContext applicationContext
    ) throws SQLException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource(
                            "database/users/add-user-bob-to-users-table.sql"
                    )
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/accommodations/add-accommodations"
                            + "-to-accommodations-table.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/bookings/add-bookings"
                            + "-to-bookings-table.sql")
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
                    new ClassPathResource(
                            "database/users/remove-user-bob-from-users-table.sql"
                    )
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/accommodations/remove-accommodations-"
                            + "from-accommodations-table.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/bookings/remove-bookings-"
                            + "from-bookings-table.sql")
            );
        }
    }

    @WithMockUser(username = "bob@gmail.com")
    @Test
    @DisplayName("Create a new booking")
    void createBooking_ValidRequestDto_Success() throws Exception {
        String jsonRequest = objectMapper.writeValueAsString(createBookingRequestDto());
        mockMvc.perform(
                        post("/bookings")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.checkInDate").value(CREATE_CHECK_IN.toString()))
                .andExpect(jsonPath("$.checkOutDate").value(CREATE_CHECK_OUT.toString()))
                .andExpect(jsonPath("$.accommodationId").value(ACCOMMODATION_ID))
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andReturn();
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Get bookings by valid userId and status")
    void getBookingsByUserIdAndStatus_ValidUserIdAndStatus_ReturnsAllBookings() throws Exception {
        String status = Booking.Status.PENDING.name();
        mockMvc.perform(
                        get("/bookings")
                                .param("userId", USER_ID.toString())
                                .param("status", status)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*].userId").value(USER_ID.intValue()))
                .andExpect(jsonPath("$.[*].status").value(status))
                .andReturn();
    }

    @WithMockUser(username = "bob@gmail.com")
    @Test
    @DisplayName("Get bookings of current user")
    void getBookingsOfCurrentUser_ReturnsListOfBookings() throws Exception {
        mockMvc.perform(get("/bookings/my"))
                .andExpect(jsonPath("$.[*].checkInDate").value(BOOKING_CHECK_IN.toString()))
                .andExpect(jsonPath("$.[*].checkOutDate").value(BOOKING_CHECK_OUT.toString()))
                .andExpect(jsonPath("$.[*].accommodationId").value(ACCOMMODATION_ID.intValue()))
                .andExpect(jsonPath("$.[*].userId").value(USER_ID.intValue()))
                .andReturn();
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName(("Get booking by booking id"))
    void getBookingById_ValidBookingId_ReturnsBookingDto() throws Exception {
        mockMvc.perform(
                        get("/bookings/{id}", BOOKING_ID)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkInDate").value(BOOKING_CHECK_IN.toString()))
                .andExpect(jsonPath("$.checkOutDate").value(BOOKING_CHECK_OUT.toString()))
                .andExpect(jsonPath("$.accommodationId").value(ACCOMMODATION_ID))
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andReturn();
    }

    @WithMockUser(username = "bob@gmail.com")
    @Test
    @DisplayName("Update a booking by id")
    void updateById_ValidBookingId_ReturnsUpdatedBookingDto() throws Exception {
        UpdateBookingRequestDto requestDto = new UpdateBookingRequestDto(
                UPDATE_BOOKING_CHECK_IN, UPDATE_BOOKING_CHECK_OUT, Booking.Status.CONFIRMED.name());
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(
                        put("/bookings/{id}", BOOKING_ID)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkInDate").value(UPDATE_BOOKING_CHECK_IN.toString()))
                .andExpect(jsonPath("$.checkOutDate").value(UPDATE_BOOKING_CHECK_OUT.toString()))
                .andExpect(jsonPath("$.status").value(Booking.Status.CONFIRMED.name()))
                .andReturn();
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Delete a booking by id")
    void deleteById_ValidBookingId_Success() throws Exception {
        Long bookingId = 1L;
        mockMvc.perform(
                        delete("/bookings/{id}",
                                bookingId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent())
                .andReturn();
    }

    private CreateBookingRequestDto createBookingRequestDto() {
        CreateBookingRequestDto createBookingRequestDto = new CreateBookingRequestDto();
        createBookingRequestDto.setAccommodationId(ACCOMMODATION_ID);
        createBookingRequestDto.setCheckInDate(CREATE_CHECK_IN);
        createBookingRequestDto.setCheckOutDate(CREATE_CHECK_OUT);
        return createBookingRequestDto;
    }
}
