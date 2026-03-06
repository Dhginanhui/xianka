package com.xianka.capacity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xianka.capacity.config.AppConfig;
import com.xianka.capacity.model.EstimateRequest;
import com.xianka.capacity.model.EstimateResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CapacityEstimateServiceTest {

    @Test
    void shouldCalculateCurveAndExtractModelProfile() {
        InferenceService inferenceService = new InferenceService(new AppConfig("https://api.siliconflow.cn/v1", ""), new ObjectMapper());
        CapacityEstimateService service = new CapacityEstimateService(inferenceService);
        EstimateRequest request = new EstimateRequest(
                "cyankiwi/Qwen3.5-27B-AWQ-BF16-INT8",
                1024,
                256,
                "实时客服对话，高并发"
        );
        EstimateResponse response = service.estimate(request);

        Assertions.assertEquals("Qwen/Qwen3.5-397B-A17B", response.fixedRuntimeModel());
        Assertions.assertEquals(27.0, response.modelProfile().modelParamsB());
        Assertions.assertEquals(27.0, response.modelProfile().activatedParamsB());
        Assertions.assertEquals("INT8", response.modelProfile().quantizationPrecision());
        Assertions.assertEquals(128, response.points().size());
        Assertions.assertTrue(response.points().get(0).vramGb() > 0);
        Assertions.assertTrue(response.points().get(0).computeTflops() > 0);
    }
}
