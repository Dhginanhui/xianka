package com.xianka.capacity.service;

import com.xianka.capacity.model.CurvePoint;
import com.xianka.capacity.model.EstimateRequest;
import com.xianka.capacity.model.EstimateResponse;
import com.xianka.capacity.model.ModelProfile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CapacityEstimateService {

    private final InferenceService inferenceService;

    public CapacityEstimateService(InferenceService inferenceService) {
        this.inferenceService = inferenceService;
    }

    public EstimateResponse estimate(EstimateRequest request) {
        ModelProfile modelProfile = inferenceService.inferModelProfile(request.model());
        double targetTokensPerSecond = inferenceService.inferTargetTokensPerSecond(request.scenarioDescription());
        // 公式：上下文系数： 0.000003 * 模型参数（单位B） + 0.0001
        double contextCoefficient = 0.000003 * modelProfile.modelParamsB() + 0.0001;

        List<CurvePoint> points = new ArrayList<>();
        // 假设并发量范围1-128
        for (int concurrency = 1; concurrency <= 128; concurrency++) {
            // 显存=[模型参数*量化精度+并发量*（单次平均输入token+单次平均输出token）*上下文系数]*1.2
            // 注意：这里的"量化精度"实际上是指量化因子（字节数），因为要算出GB
            double vramGb = (modelProfile.modelParamsB() * modelProfile.quantizationFactor()
                    + concurrency * (request.avgInputTokens() + request.avgOutputTokens()) * contextCoefficient) * 1.2;

            // 算力=激活参数*并发量*目标生成数量（Tokens/s）*（1+单次平均输入token/单次平均输出token）*0.005
            double ioRatio = 1 + (double) request.avgInputTokens() / request.avgOutputTokens();
            double computeTflops = modelProfile.activatedParamsB() * concurrency * targetTokensPerSecond * ioRatio * 0.005;
            
            points.add(new CurvePoint(concurrency, round(vramGb), round(computeTflops)));
        }

        return new EstimateResponse(
                InferenceService.FIXED_RUNTIME_MODEL,
                modelProfile,
                round(targetTokensPerSecond),
                round(contextCoefficient),
                points
        );
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
