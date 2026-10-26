package reward.api.demo.pointsApi.service;

import org.springframework.stereotype.Service;
import reward.api.demo.pointsApi.model.CustomerPointsSummary;
import reward.api.demo.pointsApi.model.Transaction;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static reward.api.demo.pointsApi.data.MockTransactions.getTransactions;

@Service
public class PointsService {

    public Optional<CustomerPointsSummary> getCustomerPointsSummary(Long customerId, LocalDate currentDate) {
        // using static data for demo purposes
        return getCustomerPointsSummary(customerId, currentDate, getTransactions());
    }

    public Optional<CustomerPointsSummary> getCustomerPointsSummary(Long customerId, LocalDate currentDate, List<Transaction> transactions) {
        if (customerId == null || currentDate == null || transactions == null) {
            return Optional.empty();
        }

        YearMonth currentMonth = YearMonth.from(currentDate);
        YearMonth prevMonth = currentMonth.minusMonths(1);
        YearMonth secondPrevMonth = currentMonth.minusMonths(2);

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
        if (transaction.transactionAmount() > 100) {
            return (int) (Math.floor(transaction.transactionAmount() - 100) * 2 + 50);
        }

        if (transaction.transactionAmount() > 50) {
            return (int) (Math.floor(transaction.transactionAmount() - 50));
        }

        return 0;
    }
}
