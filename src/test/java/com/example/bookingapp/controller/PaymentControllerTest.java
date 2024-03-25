package com.example.bookingapp.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bookingapp.dto.payment.PaymentRequestDto;
import com.example.bookingapp.dto.payment.PaymentResponseDto;
import com.example.bookingapp.model.Payment;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentControllerTest {
    protected static MockMvc mockMvc;
    private static final Long USER_ID = 1L;
    private static final Long PAYMENT_ID = 1L;
    private static final Long BOOKING_ID = 1L;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void beforeEach(
            @Autowired DataSource dataSource,
            @Autowired WebApplicationContext applicationContext
    ) throws SQLException {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/payments/add-payments-to-payments-table.sql")
            );
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
                            "database/payments/remove-payments-from-payments-table.sql"
                    )
            );
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

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getPaymentInformationByUserId_ValidUserId_ReturnsReturnsAllPayments()
            throws Exception {
        List<PaymentResponseDto> expected = List.of(createPaymentResponseDto());

        mockMvc.perform(get("/payments/{id}", USER_ID)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(expected.size()));
    }

    @Test
    @WithMockUser(username = "bob@gmail.com")
    void initiatePaymentSession_ValidRequestDto_ReturnsValidPaymentResponseDto()
            throws Exception {
        PaymentResponseDto expected = createPaymentResponseDto();

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPaymentRequestDto())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bookingId").value(expected.getBookingId()))
                .andExpect(jsonPath("$.amountToPay").value(expected.getAmountToPay()))
                .andReturn();
    }

    @Test
    @WithMockUser(username = "bob@gmail.com")
    void successPayment_ValidSessionId_ReturnsMessageAboutSuccessfulPayment()
            throws Exception {
        String sessionId = "session1";
        mockMvc.perform(MockMvcRequestBuilders.get("/payments/success")
                        .param("sessionId", sessionId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .string("The payment was successful. Session ID: "
                        + sessionId));
    }

    @Test
    @WithMockUser(username = "bob@gmail.com")
    public void successPayment_InvalidSessionId_ReturnsStatusIsNotFound() throws Exception {
        String invalidSessionId = "invalidSessionId";
        mockMvc.perform(MockMvcRequestBuilders.get("/payments/success")
                        .param("sessionId", invalidSessionId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "bob@gmail.com")
    void cancelPayment_ValidSessionId_ReturnsMassageAboutCancellingPayment() throws Exception {
        String sessionId = "session1";
        mockMvc.perform(MockMvcRequestBuilders.get("/payments/cancel")
                        .param("sessionId", sessionId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .string("Payment cancelled. You can try again later. "
                + "Session available for 24 hours. Session ID: " + sessionId));
    }

    @Test
    @WithMockUser(username = "bob@gmail.com")
    public void cancelPayment_InvalidSessionId_ReturnsStatusIsNotFound() throws Exception {
        String invalidSessionId = "invalidSessionId";
        mockMvc.perform(MockMvcRequestBuilders.get("/payments/cancel")
                        .param("sessionId", invalidSessionId))
                .andExpect(status().isNotFound());
    }

    private PaymentRequestDto createPaymentRequestDto() {
        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setAmountToPay(BigDecimal.valueOf(100));
        requestDto.setBookingId(BOOKING_ID);
        return requestDto;
    }

    private PaymentResponseDto createPaymentResponseDto() throws MalformedURLException {
        PaymentResponseDto responseDto = new PaymentResponseDto();
        responseDto.setId(PAYMENT_ID);
        responseDto.setSessionId("session1");
        responseDto.setStatus(Payment.Status.PENDING);
        responseDto.setAmountToPay(BigDecimal.valueOf(100));
        responseDto.setBookingId(BOOKING_ID);
        URL sessionUrl = new URL("https://example.com/session1");
        responseDto.setSessionUrl(sessionUrl.toString());
        return responseDto;
    }
}
