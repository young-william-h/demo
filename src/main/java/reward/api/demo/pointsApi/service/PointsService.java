package reward.api.demo.pointsApi.service;

import org.springframework.stereotype.Service;
import reward.api.demo.pointsApi.model.CustomerPointsSummary;
import reward.api.demo.pointsApi.model.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static reward.api.demo.pointsApi.data.MockTransactions.getTransactions;

@Service
public class PointsService {

    private static final BigDecimal FIFTY = BigDecimal.valueOf(50);
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    public Optional<CustomerPointsSummary> getCustomerPointsSummary(Long customerId, LocalDate currentDate) {
        return getCustomerPointsSummary(customerId, currentDate, getTransactions());
    }

    public Optional<CustomerPointsSummary> getCustomerPointsSummary(Long customerId, LocalDate currentDate, List<Transaction> transactions) {
        if (customerId == null || currentDate == null || transactions == null) {
            return Optional.empty();
        }

        YearMonth currentMonth = YearMonth.from(currentDate);
        YearMonth prevMonth = currentMonth.minusMonths(1);
        YearMonth secondPrevMonth = currentMonth.minusMonths(2);

        // Single pass: filter by customer and 3-month window, then group by YearMonth
        Map<YearMonth, List<Transaction>> txByMonth = transactions.stream()
                .filter(t -> t.customerId() == customerId)
                .filter(t -> {
                    YearMonth ym = YearMonth.from(t.transactionDate());
                    return ym.equals(currentMonth) || ym.equals(prevMonth) || ym.equals(secondPrevMonth);
                })
                .collect(Collectors.groupingBy(t -> YearMonth.from(t.transactionDate())));

        // If no transactions found for this customer in the 3-month window
        if (txByMonth.isEmpty()) {
            return Optional.empty();
        }

        int currentMonthPoints = calculatePoints(txByMonth.get(currentMonth));
        int previousMonthTotal = calculatePoints(txByMonth.get(prevMonth));
        int secondPreviousMonthTotal = calculatePoints(txByMonth.get(secondPrevMonth));
        int totalPoints = currentMonthPoints + previousMonthTotal + secondPreviousMonthTotal;

        return Optional.of(CustomerPointsSummary.builder()
                .customerId(customerId)
                .totalPoints(totalPoints)
                .currentMonthTotal(currentMonthPoints)
                .previousMonthTotal(previousMonthTotal)
                .secondPreviousMonthTotal(secondPreviousMonthTotal)
                .build());
    }

    private int calculatePoints(List<Transaction> monthTransactions) {
        if (monthTransactions == null || monthTransactions.isEmpty()) {
            return 0;
        }
        return monthTransactions.stream()
                .mapToInt(this::findPoints)
                .sum();
    }

    private int findPoints(Transaction transaction) {
        if (transaction == null || transaction.transactionAmount() == null) {
            return 0;
        }

        BigDecimal amount = transaction.transactionAmount();

        if (amount.compareTo(ONE_HUNDRED) > 0) {
            return amount.subtract(ONE_HUNDRED)
                    .setScale(0, RoundingMode.FLOOR)
                    .intValue() * 2 + 50;
        }

        if (amount.compareTo(FIFTY) > 0) {
            return amount.subtract(FIFTY)
                    .setScale(0, RoundingMode.FLOOR)
                    .intValue();
        }

        return 0;
    }
}
