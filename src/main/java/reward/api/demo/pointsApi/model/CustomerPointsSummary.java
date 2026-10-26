package reward.api.demo.pointsApi.model;

import lombok.Builder;

@Builder
public record CustomerPointsSummary(
        long customerId,
        Integer totalPoints,
        Integer currentMonthTotal,
        Integer previousMonthTotal,
        Integer secondPreviousMonthTotal
) {
}
