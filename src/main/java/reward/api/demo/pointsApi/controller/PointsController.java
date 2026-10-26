package reward.api.demo.pointsApi.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reward.api.demo.pointsApi.model.CustomerPointsSummmary;
import reward.api.demo.pointsApi.service.PointsService;

import java.time.LocalDate;

@RestController
public class PointsController {

    private final PointsService pointsService;

    public PointsController(PointsService pointsService) {
        this.pointsService = pointsService;
    }

    @GetMapping("/api/hello")
    public String sayHello() {
        return "Hello, your REST API backend is working perfectly!";
    }

    @GetMapping("/points")
    public ResponseEntity<CustomerPointsSummmary> getCustomerPointsSummary(
            @RequestHeader("customerId") Long customerId,
            @RequestHeader("currentDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate currentDate
    ) {
        return pointsService.getCustomerPointsSummmary(customerId, currentDate)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
