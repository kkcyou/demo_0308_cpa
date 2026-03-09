package cn.iocoder.yudao.module.cpa.service.chat;

import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaChatConversationDO;
import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaChatMessageDO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI对话服务接口
 */
public interface CpaChatService {

    /**
     * 创建对话会话
     */
    CpaChatConversationDO createConversation(Long userId, String title, Integer subject,
                                              Long knowledgePointId, String model);

    /**
     * 获取用户的对话列表
     */
    List<CpaChatConversationDO> listConversations(Long userId);

    /**
     * 删除对话
     */
    void deleteConversation(Long id, Long userId);

    /**
     * 发送消息（同步）
     */
    String sendMessage(Long conversationId, String message, Long userId);

    /**
     * 发送消息（流式SSE）
     */
    Flux<String> sendMessageStream(Long conversationId, String message, Long userId);

    /**
     * 获取对话消息列表
     */
    List<CpaChatMessageDO> listMessages(Long conversationId);
}
