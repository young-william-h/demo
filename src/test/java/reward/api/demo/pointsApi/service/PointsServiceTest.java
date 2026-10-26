package reward.api.demo.pointsApi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reward.api.demo.pointsApi.model.CustomerPointsSummary;
import reward.api.demo.pointsApi.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PointsServiceTest {

    private PointsService pointsService;

    @BeforeEach
    void setUp() {
        pointsService = new PointsService();
    }

    @Test
    @DisplayName("Should calculate monthly and total points for Customer 1 across 3 months")
    void testGetCustomerPointsSummaryForCustomer1() {
        LocalDate currentDate = LocalDate.of(2026, 10, 25);

        Optional<CustomerPointsSummary> summaryOpt = pointsService.getCustomerPointsSummary(1L, currentDate);

        assertThat(summaryOpt).isPresent();
        CustomerPointsSummary summary = summaryOpt.get();

        assertThat(summary.customerId()).isEqualTo(1L);
        assertThat(summary.currentMonthTotal()).isEqualTo(365);
        assertThat(summary.previousMonthTotal()).isEqualTo(190);
        assertThat(summary.secondPreviousMonthTotal()).isEqualTo(85);
        assertThat(summary.totalPoints()).isEqualTo(640);
    }

    @Test
    @DisplayName("Should calculate points for Customer 2 with transactions only in the current month")
    void testGetCustomerPointsSummaryForCustomer2() {
        LocalDate currentDate = LocalDate.of(2026, 10, 25);

        Optional<CustomerPointsSummary> summaryOpt = pointsService.getCustomerPointsSummary(2L, currentDate);

        assertThat(summaryOpt).isPresent();
        CustomerPointsSummary summary = summaryOpt.get();

        assertThat(summary.customerId()).isEqualTo(2L);
        assertThat(summary.currentMonthTotal()).isEqualTo(110);
        assertThat(summary.previousMonthTotal()).isEqualTo(0);
        assertThat(summary.secondPreviousMonthTotal()).isEqualTo(0);
        assertThat(summary.totalPoints()).isEqualTo(110);
    }

    @Test
    @DisplayName("Should return empty Optional when customer does not exist in transactions")
    void testGetCustomerPointsSummaryForUnknownCustomer() {
        LocalDate currentDate = LocalDate.of(2026, 10, 25);

        Optional<CustomerPointsSummary> summary = pointsService.getCustomerPointsSummary(999L, currentDate);

        assertThat(summary).isEmpty();
    }

    @Test
    @DisplayName("Should return empty Optional when no transactions exist in the 3-month window")
    void testGetCustomerPointsSummaryForDateWithNoTransactions() {
        LocalDate currentDate = LocalDate.of(2024, 1, 1);

        Optional<CustomerPointsSummary> summary = pointsService.getCustomerPointsSummary(1L, currentDate);

        assertThat(summary).isEmpty();
    }

    @Test
    @DisplayName("Should correctly handle Year-End rollover across January into December and November of the previous year")
    void testYearEndBoundaryRollover() {
        // Current date is in January 2027
        LocalDate currentDate = LocalDate.of(2027, 1, 15);

        List<Transaction> customTransactions = List.of(
                // Month 0: January 2027 -> $120 (90 pts)
                new Transaction(10L, LocalDate.of(2027, 1, 10), 120.0, 0),
                // Month 1: December 2026 -> $150 (150 pts)
                new Transaction(10L, LocalDate.of(2026, 12, 18), 150.0, 0),
                // Month 2: November 2026 -> $65 (15 pts)
                new Transaction(10L, LocalDate.of(2026, 11, 5), 65.0, 0),
                // December 2027: Should NOT be included (different year!)
                new Transaction(10L, LocalDate.of(2027, 12, 1), 200.0, 0),
                // October 2026: Outside the 3-month window (3 months ago) -> Should NOT be included
                new Transaction(10L, LocalDate.of(2026, 10, 31), 100.0, 0)
        );

        Optional<CustomerPointsSummary> summaryOpt = pointsService.getCustomerPointsSummary(10L, currentDate, customTransactions);

        assertThat(summaryOpt).isPresent();
        CustomerPointsSummary summary = summaryOpt.get();

        assertThat(summary.customerId()).isEqualTo(10L);
        assertThat(summary.currentMonthTotal()).isEqualTo(90);             // Jan 2027
        assertThat(summary.previousMonthTotal()).isEqualTo(150);          // Dec 2026 (previous year!)
        assertThat(summary.secondPreviousMonthTotal()).isEqualTo(15);     // Nov 2026 (previous year!)
        assertThat(summary.totalPoints()).isEqualTo(255);                 // 90 + 150 + 15
    }

    @Test
    @DisplayName("Should accurately calculate points with BigDecimal cents and boundary amounts")
    void testBigDecimalCalculations() {
        LocalDate currentDate = LocalDate.of(2026, 10, 25);

        List<Transaction> transactionsWithCents = List.of(
                // $120.75 -> 2x(20) + 50 = 90 pts
                new Transaction(20L, LocalDate.of(2026, 10, 1), new BigDecimal("120.75"), 0),
                // $99.99 -> 1x(49) = 49 pts
                new Transaction(20L, LocalDate.of(2026, 10, 2), new BigDecimal("99.99"), 0),
                // $50.00 -> 0 pts
                new Transaction(20L, LocalDate.of(2026, 10, 3), new BigDecimal("50.00"), 0),
                // $100.00 -> 1x(50) = 50 pts
                new Transaction(20L, LocalDate.of(2026, 10, 4), new BigDecimal("100.00"), 0)
        );

        Optional<CustomerPointsSummary> summaryOpt = pointsService.getCustomerPointsSummary(20L, currentDate, transactionsWithCents);

        assertThat(summaryOpt).isPresent();
        CustomerPointsSummary summary = summaryOpt.get();

        // 90 + 49 + 0 + 50 = 189
        assertThat(summary.currentMonthTotal()).isEqualTo(189);
        assertThat(summary.totalPoints()).isEqualTo(189);
    }
}
