package reward.api.demo.pointsApi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reward.api.demo.pointsApi.model.CustomerPointsSummary;
import reward.api.demo.pointsApi.service.PointsService;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointsControllerTest {

    @Mock
    private PointsService pointsService;

    @InjectMocks
    private PointsController pointsController;

    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.now();
    }

    @Test
    @DisplayName("Should return 200 OK with summary when customer and transactions are found")
    void testGetCustomerPointsSummaryFound() {
        CustomerPointsSummary mockSummary = CustomerPointsSummary.builder()
                .customerId(1L)
                .totalPoints(640)
                .currentMonthTotal(365)
                .previousMonthTotal(190)
                .secondPreviousMonthTotal(85)
                .build();

        when(pointsService.getCustomerPointsSummary(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(mockSummary));

        ResponseEntity<CustomerPointsSummary> response = pointsController.getCustomerPointsSummary(1L, testDate);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().customerId()).isEqualTo(1L);
        assertThat(response.getBody().totalPoints()).isEqualTo(640);
        verify(pointsService).getCustomerPointsSummary(1L, testDate);
    }

    @Test
    @DisplayName("Should return 404 Not Found when service returns empty Optional")
    void testGetCustomerPointsSummaryNotFound() {
        when(pointsService.getCustomerPointsSummary(eq(999L), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        ResponseEntity<CustomerPointsSummary> response = pointsController.getCustomerPointsSummary(999L, testDate);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
        verify(pointsService).getCustomerPointsSummary(999L, testDate);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when customerId is zero or negative")
    void testGetCustomerPointsSummaryInvalidCustomerId() {
        assertThrows(IllegalArgumentException.class, () ->
                pointsController.getCustomerPointsSummary(0L, testDate)
        );

        assertThrows(IllegalArgumentException.class, () ->
                pointsController.getCustomerPointsSummary(-5L, testDate)
        );
    }
}
