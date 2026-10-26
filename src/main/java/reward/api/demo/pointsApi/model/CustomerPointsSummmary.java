package reward.api.demo.pointsApi.model;

import lombok.Builder;

@Builder
public record CustomerPointsSummmary(
        long customerId,
        Integer totalPoints,
        Integer currentMonthTotal,
        Integer previousMonthTotal,
        Integer secondPreviousMonthTotal
) {
}
