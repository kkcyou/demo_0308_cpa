package cn.iocoder.yudao.module.cpa.service.ai;

import cn.iocoder.yudao.module.cpa.dal.dataobject.*;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * CPA AI服务接口
 */
public interface CpaAiService {

    /**
     * 分析新闻并匹配最相关的考点
     *
     * @param news       新闻
     * @param candidates 候选考点列表
     * @return 匹配的考点ID
     */
    Long matchNewsToKnowledgePoint(CpaDailyNewsDO news, List<CpaKnowledgePointDO> candidates);

    /**
     * 生成考点讲解内容
     *
     * @param point 考点
     * @param news  新闻
     * @return 讲解内容（Markdown）
     */
    String generatePointExplanation(CpaKnowledgePointDO point, CpaDailyNewsDO news);

    /**
     * 生成真题讲解
     *
     * @param question 真题
     * @param point    关联考点
     * @return 讲解内容（Markdown）
     */
    String generateQuestionAnalysis(CpaExamQuestionDO question, CpaKnowledgePointDO point);

    /**
     * 生成记忆辅助材料（口诀+表格+导图）
     *
     * @param point 考点
     * @return 记忆辅助内容（Markdown）
     */
    String generateMnemonic(CpaKnowledgePointDO point);

    /**
     * AI对话（同步）
     *
     * @param conversationId 会话ID
     * @param userMessage    用户消息
     * @param userId         用户ID
     * @return AI回复
     */
    String chat(Long conversationId, String userMessage, Long userId);

    /**
     * AI对话（流式SSE）
     *
     * @param conversationId 会话ID
     * @param userMessage    用户消息
     * @param userId         用户ID
     * @return 流式回复
     */
    Flux<String> chatStream(Long conversationId, String userMessage, Long userId);

    /**
     * 生成模拟财经新闻（降级方案）
     *
     * @param subject 科目
     * @return 新闻JSON字符串
     */
    String generateFallbackNews(Integer subject);
}
