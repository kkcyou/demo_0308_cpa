package cn.iocoder.yudao.module.cpa.dal.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI对话会话 DO
 */
@Data
@TableName("cpa_chat_conversation")
public class CpaChatConversationDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 会话标题 */
    private String title;

    /** 关联科目 */
    private Integer subject;

    /** 关联考点ID */
    private Long knowledgePointId;

    /** AI模型 */
    private String model;

    /** 系统提示词 */
    private String systemPrompt;

    /** 消息数量 */
    private Integer messageCount;

    /** 是否置顶 */
    private Boolean pinned;

    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;

    @TableLogic
    private Boolean deleted;
}
