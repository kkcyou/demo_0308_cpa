package cn.iocoder.yudao.module.cpa.service.chat;

import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaChatConversationDO;
import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaChatMessageDO;
import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaKnowledgePointDO;
import cn.iocoder.yudao.module.cpa.dal.mysql.CpaChatConversationMapper;
import cn.iocoder.yudao.module.cpa.dal.mysql.CpaChatMessageMapper;
import cn.iocoder.yudao.module.cpa.dal.mysql.CpaKnowledgePointMapper;
import cn.iocoder.yudao.module.cpa.enums.CpaSubjectEnum;
import cn.iocoder.yudao.module.cpa.service.ai.CpaAiService;
import cn.iocoder.yudao.module.cpa.service.ai.prompt.CpaPromptTemplates;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Service
public class CpaChatServiceImpl implements CpaChatService {

    @Resource
    private CpaChatConversationMapper conversationMapper;
    @Resource
    private CpaChatMessageMapper messageMapper;
    @Resource
    private CpaKnowledgePointMapper knowledgePointMapper;
    @Resource
    private CpaAiService aiService;

    @Override
    public CpaChatConversationDO createConversation(Long userId, String title, Integer subject,
                                                     Long knowledgePointId, String model) {
        CpaChatConversationDO conversation = new CpaChatConversationDO();
        conversation.setUserId(userId);
        conversation.setTitle(title);
        conversation.setSubject(subject);
        conversation.setKnowledgePointId(knowledgePointId);
        conversation.setModel(model);
        conversation.setMessageCount(0);
        conversation.setPinned(false);

        // 构建系统提示词
        String subjectName = "综合";
        if (subject != null) {
            CpaSubjectEnum subjectEnum = CpaSubjectEnum.getByCode(subject);
            if (subjectEnum != null) {
                subjectName = subjectEnum.getName();
            }
        }
        String systemPrompt = String.format(CpaPromptTemplates.CHAT_SYSTEM_PROMPT, subjectName);

        // 如果关联了考点，追加考点上下文
        if (knowledgePointId != null) {
            CpaKnowledgePointDO point = knowledgePointMapper.selectById(knowledgePointId);
            if (point != null) {
                systemPrompt += String.format("\n\n当前讨论的考点是：%s（%s - %s）\n考点内容：%s",
                        point.getTitle(), subjectName, point.getChapter(),
                        point.getContent() != null ? point.getContent() : "");
            }
        }
        conversation.setSystemPrompt(systemPrompt);

        conversationMapper.insert(conversation);
        return conversation;
    }

    @Override
    public List<CpaChatConversationDO> listConversations(Long userId) {
        return conversationMapper.selectListByUserId(userId);
    }

    @Override
    public void deleteConversation(Long id, Long userId) {
        CpaChatConversationDO conversation = conversationMapper.selectById(id);
        if (conversation != null && conversation.getUserId().equals(userId)) {
            conversationMapper.deleteById(id);
        }
    }

    @Override
    public String sendMessage(Long conversationId, String message, Long userId) {
        return aiService.chat(conversationId, message, userId);
    }

    @Override
    public Flux<String> sendMessageStream(Long conversationId, String message, Long userId) {
        return aiService.chatStream(conversationId, message, userId);
    }

    @Override
    public List<CpaChatMessageDO> listMessages(Long conversationId) {
        return messageMapper.selectListByConversationId(conversationId);
    }
}
