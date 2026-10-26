package reward.api.demo.pointsApi.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PointsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/hello should return 200 OK and greeting message")
    void testSayHello() throws Exception {
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, your REST API backend is working perfectly!"));
    }

    @Test
    @DisplayName("GET /points with valid customerId and currentDate headers should return 200 OK with correct points summary")
    void testGetPointsWithHeaders() throws Exception {
        mockMvc.perform(get("/points")
                        .header("customerId", 1L)
                        .header("currentDate", "2026-10-25")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.currentMonthTotal").value(365))
                .andExpect(jsonPath("$.previousMonthTotal").value(190))
                .andExpect(jsonPath("$.secondPreviousMonthTotal").value(85))
                .andExpect(jsonPath("$.totalPoints").value(640));
    }

    @Test
    @DisplayName("GET /points for customer 2 should return correct points for current month only")
    void testGetPointsForCustomer2() throws Exception {
        mockMvc.perform(get("/points")
                        .header("customerId", 2L)
                        .header("currentDate", "2026-10-25")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(2))
                .andExpect(jsonPath("$.currentMonthTotal").value(110))
                .andExpect(jsonPath("$.previousMonthTotal").value(0))
                .andExpect(jsonPath("$.secondPreviousMonthTotal").value(0))
                .andExpect(jsonPath("$.totalPoints").value(110));
    }

    @Test
    @DisplayName("GET /points for non-existent customer should return 404 Not Found")
    void testGetPointsCustomerNotFound() throws Exception {
        mockMvc.perform(get("/points")
                        .header("customerId", 999L)
                        .header("currentDate", "2026-10-25"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /points when customer has no transactions in 3-month window should return 404 Not Found")
    void testGetPointsNoTransactionsFoundInWindow() throws Exception {
        mockMvc.perform(get("/points")
                        .header("customerId", 1L)
                        .header("currentDate", "2024-01-01"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /points without customerId header should return 400 Bad Request")
    void testGetPointsMissingCustomerIdHeader() throws Exception {
        mockMvc.perform(get("/points")
                        .header("currentDate", "2026-10-25"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /points without currentDate header should return 400 Bad Request")
    void testGetPointsMissingCurrentDateHeader() throws Exception {
        mockMvc.perform(get("/points")
                        .header("customerId", 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /points with invalid date format should return 400 Bad Request")
    void testGetPointsInvalidDateFormat() throws Exception {
        mockMvc.perform(get("/points")
                        .header("customerId", 1L)
                        .header("currentDate", "invalid-date-format"))
                .andExpect(status().isBadRequest());
    }
}
