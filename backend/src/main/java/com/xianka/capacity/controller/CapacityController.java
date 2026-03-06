package com.xianka.capacity.controller;

import com.xianka.capacity.model.EstimateRequest;
import com.xianka.capacity.model.EstimateResponse;
import com.xianka.capacity.service.CapacityEstimateService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/capacity")
@CrossOrigin(origins = "*")
public class CapacityController {

    private final CapacityEstimateService capacityEstimateService;

    public CapacityController(CapacityEstimateService capacityEstimateService) {
        this.capacityEstimateService = capacityEstimateService;
    }

    @PostMapping("/estimate")
    public EstimateResponse estimate(@Valid @RequestBody EstimateRequest request) {
        return capacityEstimateService.estimate(request);
    }
}
