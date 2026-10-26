package reward.api.demo.pointsApi.model;

import java.time.LocalDate;

public record Transaction(
        long customerId,
        LocalDate transactionDate,
        double transactionAmount,
        int transactionPoints
) {
}
