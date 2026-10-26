package reward.api.demo.pointsApi.service;

import org.springframework.stereotype.Service;
import reward.api.demo.pointsApi.model.CustomerPointsSummmary;
import reward.api.demo.pointsApi.model.Transaction;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static reward.api.demo.pointsApi.data.MockTransactions.getTransactions;

@Service
public class PointsService {

    public Optional<CustomerPointsSummmary> getCustomerPointsSummmary(Long customerId, LocalDate currentDate) {
        return getCustomerPointsSummmary(customerId, currentDate, getTransactions());
    }

    public Optional<CustomerPointsSummmary> getCustomerPointsSummmary(Long customerId, LocalDate currentDate, List<Transaction> transactions) {

        List<Transaction> currentMonthTx = getMonthTransactions(customerId, currentDate, 0, transactions);
        List<Transaction> prevMonthTx = getMonthTransactions(customerId, currentDate, 1, transactions);
        List<Transaction> secondPrevMonthTx = getMonthTransactions(customerId, currentDate, 2, transactions);

        // If no transactions are found for this customer in the 3-month window
        if (currentMonthTx.isEmpty() && prevMonthTx.isEmpty() && secondPrevMonthTx.isEmpty()) {
            return Optional.empty();
        }

        int currentMonthPoints = currentMonthTx.stream().mapToInt(this::findPoints).sum();
        int previousMonthTotal = prevMonthTx.stream().mapToInt(this::findPoints).sum();
        int secondPreviousMonthTotal = secondPrevMonthTx.stream().mapToInt(this::findPoints).sum();
        int totalPoints = currentMonthPoints + previousMonthTotal + secondPreviousMonthTotal;

        return Optional.of(CustomerPointsSummmary.builder()
                .customerId(customerId)
                .totalPoints(totalPoints)
                .currentMonthTotal(currentMonthPoints)
                .previousMonthTotal(previousMonthTotal)
                .secondPreviousMonthTotal(secondPreviousMonthTotal)
                .build());
    }

    private List<Transaction> getMonthTransactions(Long customerId, LocalDate currentDate, Integer monthDifference, List<Transaction> transactions) {
        YearMonth targetMonth = YearMonth.from(currentDate).minusMonths(monthDifference);

        return transactions.stream().filter(transaction ->
                transaction.customerId() == customerId &&
                YearMonth.from(transaction.transactionDate()).equals(targetMonth)
        ).toList();
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
