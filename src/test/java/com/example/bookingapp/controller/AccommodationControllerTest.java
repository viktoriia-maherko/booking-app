package com.example.bookingapp.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bookingapp.dto.accommodation.AccommodationResponseDto;
import com.example.bookingapp.dto.accommodation.AccommodationUpdateRequestDto;
import com.example.bookingapp.dto.accommodation.CreateAccommodationRequestDto;
import com.example.bookingapp.model.Accommodation;
import com.example.bookingapp.model.Address;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
class AccommodationControllerTest {
    protected static MockMvc mockMvc;
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
                    new ClassPathResource("database/accommodations/add-accommodations"
                            + "-to-accommodations-table.sql")
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
                    new ClassPathResource("database/accommodations/remove-accommodations-"
                            + "from-accommodations-table.sql")
            );
        }
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Create a new accommodation")
    void createAccommodation_ValidRequestDto_Success() throws Exception {
        String jsonRequest = objectMapper.writeValueAsString(createAccommodationRequestDto());

        mockMvc.perform(
                        post("/accommodations")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value(Accommodation.Type.APARTMENT.name()))
                .andExpect(jsonPath("$.dailyRate").value(BigDecimal.valueOf(100)))
                .andExpect(jsonPath("$.size").value("Small"))
                .andExpect(jsonPath("$.availability").value(2))
                .andReturn();
    }

    @WithMockUser(username = "user", roles = {"USER"})
    @Test
    @DisplayName("Get all accommodations")
    void getAll_GivenAccommodations_ReturnsAllAccommodations() throws Exception {
        AccommodationResponseDto accommodationDto2 = createAccommodationResponseDto();
        accommodationDto2.setType(Accommodation.Type.HOUSE);
        accommodationDto2.setAddress(createAddress2());
        accommodationDto2.setSize("Medium");
        accommodationDto2.setDailyRate(BigDecimal.valueOf(150));
        accommodationDto2.setAvailability(3);
        accommodationDto2.setAmenities(List.of("Bath", "Pool"));

        AccommodationResponseDto accommodationDto1 = createAccommodationResponseDto();

        List<AccommodationResponseDto> expected = new ArrayList<>();
        expected.add(accommodationDto1);
        expected.add(accommodationDto2);
        String params = "?page=0&size=2";

        mockMvc.perform(
                        get("/accommodations" + params)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(expected.size()))
                .andReturn();
    }

    @WithMockUser(username = "user", roles = {"USER"})
    @Test
    @DisplayName(("Get accommodation by accommodation id"))
    void getAccommodationById_ValidAccommodationId_ReturnsAccommodationDto() throws Exception {
        AccommodationResponseDto expected = createAccommodationResponseDto();
        Long accommodationId = expected.getId();
        mockMvc.perform(
                        get("/accommodations/{id}", accommodationId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value(Accommodation.Type.APARTMENT.name()))
                .andExpect(jsonPath("$.dailyRate").value(BigDecimal.valueOf(100).intValue()))
                .andExpect(jsonPath("$.size").value("Small"))
                .andExpect(jsonPath("$.availability").value(2))
                .andReturn();
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Update an accommodation by id")
    void updateById_ValidAccommodationId_ReturnsUpdatedAccommodationDto() throws Exception {
        AccommodationUpdateRequestDto accommodationRequestDto = new AccommodationUpdateRequestDto();
        accommodationRequestDto.setDailyRate(BigDecimal.valueOf(200));
        accommodationRequestDto.setAmenities(List.of("Gym"));
        accommodationRequestDto.setAvailability(1);
        AccommodationResponseDto expected = createAccommodationResponseDto();
        expected.setDailyRate(BigDecimal.valueOf(200));
        expected.setAmenities(List.of("Gym"));
        expected.setAvailability(1);
        Long accommodationId = expected.getId();

        String jsonRequest = objectMapper.writeValueAsString(accommodationRequestDto);

        mockMvc.perform(
                        put("/accommodations/{id}", accommodationId)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyRate").value(BigDecimal.valueOf(200).intValue()))
                .andExpect(jsonPath("$.availability").value(1))
                .andReturn();
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Delete an accommodation by id")
    void deleteById_ValidAccommodationId_Success() throws Exception {
        Long accommodationId = 1L;
        mockMvc.perform(
                        delete("/accommodations/{id}",
                                accommodationId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent())
                .andReturn();
    }

    private CreateAccommodationRequestDto createAccommodationRequestDto() {
        CreateAccommodationRequestDto requestDto = new CreateAccommodationRequestDto();
        requestDto.setAddress(createAddress1());
        requestDto.setAmenities(List.of("Bath", "Big kitchen", "Wi-fi"));
        requestDto.setType(Accommodation.Type.APARTMENT);
        requestDto.setDailyRate(BigDecimal.valueOf(100));
        requestDto.setSize("Small");
        requestDto.setAvailability(2);
        return requestDto;
    }

    private Address createAddress1() {
        Address address = new Address();
        address.setId(1L);
        address.setRegion("USA");
        address.setCity("New York");
        address.setStreet("John Glen Blvd, 10");
        return address;
    }

    private Address createAddress2() {
        Address address = new Address();
        address.setId(2L);
        address.setRegion("Ukraine");
        address.setCity("Lvov");
        address.setStreet("Shevchenka, 5");
        return address;
    }

    private AccommodationResponseDto createAccommodationResponseDto() {
        AccommodationResponseDto responseDto = new AccommodationResponseDto();
        responseDto.setId(1L);
        responseDto.setAddress(createAddress1());
        responseDto.setAmenities(List.of("Bath", "Big kitchen", "Wi-fi"));
        responseDto.setType(Accommodation.Type.APARTMENT);
        responseDto.setDailyRate(BigDecimal.valueOf(100));
        responseDto.setSize("Small");
        responseDto.setAvailability(2);
        return responseDto;
    }
}
