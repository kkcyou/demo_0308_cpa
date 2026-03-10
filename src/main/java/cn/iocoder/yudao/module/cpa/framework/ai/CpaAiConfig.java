package cn.iocoder.yudao.module.cpa.framework.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * 多模型AI配置
 *
 * 所有国产大模型（千问、DeepSeek、智谱、豆包）都兼容OpenAI接口协议，
 * 所以只需要切换 base-url、api-key、model 三个参数即可。
 *
 * 支持的模型:
 * - openai:   GPT-4o / GPT-4 / GPT-3.5
 * - claude:   Claude Sonnet / Opus（通过兼容网关）
 * - qwen:     通义千问 qwen-plus / qwen-max / qwen-turbo
 * - deepseek: DeepSeek deepseek-chat / deepseek-reasoner
 * - zhipu:    智谱GLM glm-4-plus / glm-4
 * - doubao:   豆包 doubao-pro-32k
 * - ollama:   本地模型 qwen2.5 / llama3 等
 */
@Slf4j
@Configuration
public class CpaAiConfig {

    @Value("${cpa.ai.active-model:openai}")
    private String activeModel;

    // ====== OpenAI ======
    @Value("${cpa.ai.openai.api-key:}")
    private String openaiApiKey;
    @Value("${cpa.ai.openai.base-url:https://api.openai.com}")
    private String openaiBaseUrl;
    @Value("${cpa.ai.openai.model:gpt-4o}")
    private String openaiModel;

    // ====== Claude ======
    @Value("${cpa.ai.claude.api-key:}")
    private String claudeApiKey;
    @Value("${cpa.ai.claude.base-url:https://api.anthropic.com}")
    private String claudeBaseUrl;
    @Value("${cpa.ai.claude.model:claude-sonnet-4-20250514}")
    private String claudeModel;

    // ====== 通义千问 ======
    @Value("${cpa.ai.qwen.api-key:}")
    private String qwenApiKey;
    @Value("${cpa.ai.qwen.base-url:https://dashscope.aliyuncs.com/compatible-mode}")
    private String qwenBaseUrl;
    @Value("${cpa.ai.qwen.model:qwen-plus}")
    private String qwenModel;

    // ====== DeepSeek ======
    @Value("${cpa.ai.deepseek.api-key:}")
    private String deepseekApiKey;
    @Value("${cpa.ai.deepseek.base-url:https://api.deepseek.com}")
    private String deepseekBaseUrl;
    @Value("${cpa.ai.deepseek.model:deepseek-chat}")
    private String deepseekModel;

    // ====== 智谱GLM ======
    @Value("${cpa.ai.zhipu.api-key:}")
    private String zhipuApiKey;
    @Value("${cpa.ai.zhipu.base-url:https://open.bigmodel.cn/api/paas}")
    private String zhipuBaseUrl;
    @Value("${cpa.ai.zhipu.model:glm-4-plus}")
    private String zhipuModel;

    // ====== 豆包 ======
    @Value("${cpa.ai.doubao.api-key:}")
    private String doubaoApiKey;
    @Value("${cpa.ai.doubao.base-url:https://ark.cn-beijing.volces.com/api}")
    private String doubaoBaseUrl;
    @Value("${cpa.ai.doubao.model:doubao-pro-32k}")
    private String doubaoModel;

    // ====== Ollama 本地模型 ======
    @Value("${cpa.ai.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;
    @Value("${cpa.ai.ollama.model:qwen2.5}")
    private String ollamaModel;

    /**
     * 根据 active-model 配置创建对应的 ChatModel
     * 所有模型都兼容 OpenAI 接口协议，统一用 OpenAiChatModel 驱动
     */
    @Bean
    @Primary
    public ChatModel chatModel() {
        String apiKey;
        String baseUrl;
        String model;

        switch (activeModel.toLowerCase()) {
            case "claude" -> {
                apiKey = claudeApiKey;
                baseUrl = claudeBaseUrl;
                model = claudeModel;
            }
            case "qwen" -> {
                apiKey = qwenApiKey;
                baseUrl = qwenBaseUrl;
                model = qwenModel;
            }
            case "deepseek" -> {
                apiKey = deepseekApiKey;
                baseUrl = deepseekBaseUrl;
                model = deepseekModel;
            }
            case "zhipu" -> {
                apiKey = zhipuApiKey;
                baseUrl = zhipuBaseUrl;
                model = zhipuModel;
            }
            case "doubao" -> {
                apiKey = doubaoApiKey;
                baseUrl = doubaoBaseUrl;
                model = doubaoModel;
            }
            case "ollama" -> {
                apiKey = "ollama"; // Ollama不需要key，但不能为空
                baseUrl = ollamaBaseUrl + "/v1";
                model = ollamaModel;
            }
            default -> { // openai
                apiKey = openaiApiKey;
                baseUrl = openaiBaseUrl;
                model = openaiModel;
            }
        }

        log.info("===== AI模型配置 =====");
        log.info("当前模型: {} ({})", activeModel, model);
        log.info("API地址: {}", baseUrl);
        log.info("=======================");

        // 设置HTTP超时: 连接10秒，读取120秒（AI生成内容较慢）
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10_000);
        requestFactory.setReadTimeout(120_000);
        RestClient.Builder restClientBuilder = RestClient.builder()
                .requestFactory(requestFactory);

        OpenAiApi api = OpenAiApi.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .restClientBuilder(restClientBuilder)
                .build();

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(model)
                .temperature(0.7)
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(options)
                .build();
    }
}
