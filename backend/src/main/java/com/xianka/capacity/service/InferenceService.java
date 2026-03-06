package com.xianka.capacity.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xianka.capacity.config.AppConfig;
import com.xianka.capacity.model.ModelProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InferenceService {

    private static final Logger logger = LoggerFactory.getLogger(InferenceService.class);

    public static final String FIXED_RUNTIME_MODEL = "Pro/zai-org/GLM-4.7";
    private final AppConfig appConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public InferenceService(AppConfig appConfig, ObjectMapper objectMapper) {
        this.appConfig = appConfig;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
        
        if (appConfig.llmApiKey() == null || appConfig.llmApiKey().isEmpty()) {
            logger.warn("LLM API Key is NOT configured in AppConfig!");
        } else {
            String maskedKey = appConfig.llmApiKey().length() > 8 
                ? appConfig.llmApiKey().substring(0, 4) + "****" + appConfig.llmApiKey().substring(appConfig.llmApiKey().length() - 4) 
                : "****";
            logger.info("InferenceService initialized with API Key: {}", maskedKey);
            logger.info("LLM Base URL: {}", appConfig.llmBaseUrl());
        }
    }

    public ModelProfile inferModelProfile(String inputModelName) {
        String systemPrompt = "你是一个专业的LLM模型参数提取助手。请根据用户输入的模型名称，提取模型参数（B）、激活参数（B）、量化精度（如INT8/FP16）以及量化因子（字节数）。\n" +
                "如果模型名称中包含MoE架构（如A14B），请正确区分总参数和激活参数；如果是Dense模型，则两者相同。\n" +
                "量化因子参考：INT4=0.5, INT8/FP8=1, BF16/FP16=2, FP32=4。\n" +
                "请返回纯JSON字符串，不要包含markdown标记或其他文本。不要输出```json前缀。";
        String userPrompt = "请提取模型信息：" + inputModelName;

        return callLlm(systemPrompt, userPrompt, ModelProfile.class);
    }

    public double inferTargetTokensPerSecond(String scenarioDescription) {
        String systemPrompt = "你是一个吞吐量评估助手。请根据使用场景描述，推算合理的目标生成速度（Tokens/s）。\n" +
                "参考规则：\n" +
                "- 实时/对话/客服类：120\n" +
                "- 批处理/离线类：60\n" +
                "- 高并发/直播类：180\n" +
                "- 其他默认：100\n" +
                "请返回纯JSON字符串，不要包含markdown标记或其他文本。不要输出```json前缀。";
        String userPrompt = "场景描述：" + scenarioDescription;

        TokensPerSecondResponse response = callLlm(systemPrompt, userPrompt, TokensPerSecondResponse.class);
        return response != null ? response.getTokensPerSecond() : 100.0;
    }

    private <T> T callLlm(String systemPrompt, String userPrompt, Class<T> responseType) {
        if (appConfig.llmApiKey() == null || appConfig.llmApiKey().isEmpty()) {
            throw new RuntimeException("LLM API Key is not configured");
        }

        String url = appConfig.llmBaseUrl() + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(appConfig.llmApiKey());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", FIXED_RUNTIME_MODEL);
        requestBody.put("temperature", 0.0);
        
        // Note: response_format json_object might not be supported by all providers or older OpenAI API versions
        // But SiliconFlow supports it.
        Map<String, String> responseFormat = new HashMap<>();
        responseFormat.put("type", "json_object");
        requestBody.put("response_format", responseFormat);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userPrompt);
        messages.add(userMsg);

        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            logger.info("Sending request to LLM: {}", url);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("Received successful response from LLM");
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode contentNode = root.path("choices").get(0).path("message").path("content");
                if (!contentNode.isMissingNode()) {
                    String content = contentNode.asText();
                    return objectMapper.readValue(content, responseType);
                } else {
                    logger.warn("Response missing content node: {}", response.getBody());
                }
            } else {
                logger.error("LLM API request failed: Status={}, Body={}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            logger.error("Exception calling LLM API", e);
            throw new RuntimeException("Failed to call LLM API: " + e.getMessage(), e);
        }
        return null;
    }

    public static class TokensPerSecondResponse {
        private double tokensPerSecond;

        public TokensPerSecondResponse() {
        }
        
        public TokensPerSecondResponse(double tokensPerSecond) {
            this.tokensPerSecond = tokensPerSecond;
        }

        public double getTokensPerSecond() {
            return tokensPerSecond;
        }

        public void setTokensPerSecond(double tokensPerSecond) {
            this.tokensPerSecond = tokensPerSecond;
        }
        
        // compatibility accessor
        public double tokensPerSecond() { return tokensPerSecond; }
    }
}
