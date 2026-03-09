package cn.iocoder.yudao.module.cpa.service.ai;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.cpa.dal.dataobject.*;
import cn.iocoder.yudao.module.cpa.dal.mysql.CpaChatConversationMapper;
import cn.iocoder.yudao.module.cpa.dal.mysql.CpaChatMessageMapper;
import cn.iocoder.yudao.module.cpa.enums.CpaSubjectEnum;
import cn.iocoder.yudao.module.cpa.service.ai.prompt.CpaPromptTemplates;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CPA AI服务实现
 */
@Slf4j
@Service
public class CpaAiServiceImpl implements CpaAiService {

    @Resource
    private ChatModel chatModel;

    @Resource
    private CpaChatConversationMapper conversationMapper;

    @Resource
    private CpaChatMessageMapper messageMapper;

    @Override
    public Long matchNewsToKnowledgePoint(CpaDailyNewsDO news, List<CpaKnowledgePointDO> candidates) {
        // 构建候选考点JSON
        String candidatesJson = candidates.stream()
                .map(p -> String.format("{\"id\":%d,\"subject\":\"%s\",\"title\":\"%s\",\"keywords\":\"%s\"}",
                        p.getId(),
                        CpaSubjectEnum.getByCode(p.getSubject()).getName(),
                        p.getTitle(),
                        p.getKeywords()))
                .collect(Collectors.joining(",", "[", "]"));

        String prompt = String.format(CpaPromptTemplates.NEWS_MATCH_PROMPT,
                news.getTitle() + "。" + news.getSummary(),
                candidatesJson);

        try {
            String result = ChatClient.create(chatModel).prompt()
                    .user(prompt)
                    .call()
                    .content();
            return Long.parseLong(result.trim());
        } catch (Exception e) {
            log.warn("AI匹配考点失败，使用默认选择: {}", e.getMessage());
            return candidates.get(0).getId();
        }
    }

    @Override
    public String generatePointExplanation(CpaKnowledgePointDO point, CpaDailyNewsDO news) {
        CpaSubjectEnum subject = CpaSubjectEnum.getByCode(point.getSubject());
        String prompt = String.format(CpaPromptTemplates.POINT_EXPLAIN_PROMPT,
                news.getTitle() + "：" + news.getSummary(),
                subject != null ? subject.getName() : "未知",
                point.getChapter(),
                point.getTitle());

        return ChatClient.create(chatModel).prompt()
                .user(prompt)
                .call()
                .content();
    }

    @Override
    public String generateQuestionAnalysis(CpaExamQuestionDO question, CpaKnowledgePointDO point) {
        CpaSubjectEnum subject = CpaSubjectEnum.getByCode(question.getSubject());
        String prompt = String.format(CpaPromptTemplates.QUESTION_EXPLAIN_PROMPT,
                subject != null ? subject.getName() : "未知",
                question.getYear(),
                question.getQuestionType().toString(),
                question.getContent(),
                question.getOptions() != null ? question.getOptions() : "无",
                question.getAnswer());

        return ChatClient.create(chatModel).prompt()
                .user(prompt)
                .call()
                .content();
    }

    @Override
    public String generateMnemonic(CpaKnowledgePointDO point) {
        CpaSubjectEnum subject = CpaSubjectEnum.getByCode(point.getSubject());
        String prompt = String.format(CpaPromptTemplates.MNEMONIC_PROMPT,
                subject != null ? subject.getName() : "未知",
                point.getTitle(),
                point.getContent() != null ? point.getContent() : point.getTitle());

        return ChatClient.create(chatModel).prompt()
                .user(prompt)
                .call()
                .content();
    }

    @Override
    public String chat(Long conversationId, String userMessage, Long userId) {
        List<Message> messages = buildChatMessages(conversationId, userMessage);

        String result = ChatClient.create(chatModel).prompt()
                .messages(messages)
                .call()
                .content();

        // 保存用户消息和AI回复
        saveChatMessage(conversationId, userId, "user", userMessage);
        saveChatMessage(conversationId, userId, "assistant", result);

        // 更新会话消息计数
        CpaChatConversationDO conversation = conversationMapper.selectById(conversationId);
        if (conversation != null) {
            conversation.setMessageCount(conversation.getMessageCount() + 2);
            conversationMapper.updateById(conversation);
        }

        return result;
    }

    @Override
    public Flux<String> chatStream(Long conversationId, String userMessage, Long userId) {
        List<Message> messages = buildChatMessages(conversationId, userMessage);

        // 保存用户消息
        saveChatMessage(conversationId, userId, "user", userMessage);

        StringBuilder fullResponse = new StringBuilder();

        return ChatClient.create(chatModel).prompt()
                .messages(messages)
                .stream()
                .content()
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> {
                    // 流完成后保存AI回复
                    saveChatMessage(conversationId, userId, "assistant", fullResponse.toString());
                    CpaChatConversationDO conversation = conversationMapper.selectById(conversationId);
                    if (conversation != null) {
                        conversation.setMessageCount(conversation.getMessageCount() + 2);
                        conversationMapper.updateById(conversation);
                    }
                });
    }

    @Override
    public String generateFallbackNews(Integer subject) {
        CpaSubjectEnum subjectEnum = CpaSubjectEnum.getByCode(subject);
        String prompt = String.format(CpaPromptTemplates.NEWS_GENERATE_PROMPT,
                LocalDate.now().toString(),
                subjectEnum != null ? subjectEnum.getName() : "会计");

        return ChatClient.create(chatModel).prompt()
                .user(prompt)
                .call()
                .content();
    }

    /**
     * 构建对话消息列表（包含历史上下文）
     */
    private List<Message> buildChatMessages(Long conversationId, String userMessage) {
        List<Message> messages = new ArrayList<>();

        // 获取会话信息，添加系统提示词
        CpaChatConversationDO conversation = conversationMapper.selectById(conversationId);
        if (conversation != null && conversation.getSystemPrompt() != null) {
            messages.add(new SystemMessage(conversation.getSystemPrompt()));
        }

        // 加载历史消息
        List<CpaChatMessageDO> history = messageMapper.selectListByConversationId(conversationId);
        for (CpaChatMessageDO msg : history) {
            switch (msg.getRole()) {
                case "user" -> messages.add(new UserMessage(msg.getContent()));
                case "assistant" -> messages.add(new AssistantMessage(msg.getContent()));
                case "system" -> messages.add(new SystemMessage(msg.getContent()));
            }
        }

        // 添加当前用户消息
        messages.add(new UserMessage(userMessage));
        return messages;
    }

    /**
     * 保存对话消息
     */
    private void saveChatMessage(Long conversationId, Long userId, String role, String content) {
        CpaChatMessageDO message = new CpaChatMessageDO();
        message.setConversationId(conversationId);
        message.setUserId(userId);
        message.setRole(role);
        message.setContent(content);
        messageMapper.insert(message);
    }
}
