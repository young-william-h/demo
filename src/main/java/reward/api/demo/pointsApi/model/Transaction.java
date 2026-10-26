package reward.api.demo.pointsApi.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Transaction(
        long customerId,
        LocalDate transactionDate,
        BigDecimal transactionAmount,
        int transactionPoints
) {
    public Transaction(long customerId, LocalDate transactionDate, double transactionAmount, int transactionPoints) {
        this(customerId, transactionDate, BigDecimal.valueOf(transactionAmount), transactionPoints);
    }
}
