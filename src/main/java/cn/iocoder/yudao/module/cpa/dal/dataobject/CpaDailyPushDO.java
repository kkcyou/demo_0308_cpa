package cn.iocoder.yudao.module.cpa.dal.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日推送内容 DO（核心聚合表）
 */
@Data
@TableName("cpa_daily_push")
public class CpaDailyPushDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 推送日期 */
    private LocalDate pushDate;

    /** 关联新闻ID */
    private Long newsId;

    /** 关联考点ID */
    private Long knowledgePointId;

    /** 科目 */
    private Integer subject;

    /** 推送标题 */
    private String title;

    /** 新闻要点 */
    private String newsSummary;

    /** 考点讲解内容（AI生成） */
    private String pointExplain;

    /** 关联真题ID列表 */
    private String questionIds;

    /** 真题讲解内容（AI生成） */
    private String questionExplain;

    /** 记忆辅助（AI生成，Markdown格式） */
    private String mnemonic;

    /** 思维导图数据（JSON格式） */
    private String mindmapData;

    /** 推送状态 0待推送 1已推送 2推送失败 */
    private Integer pushStatus;

    /** 实际推送时间 */
    private LocalDateTime pushTime;

    /** 使用的AI模型 */
    private String aiModel;

    /** AI消耗的token数 */
    private Integer aiTokenUsed;

    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;

    @TableLogic
    private Boolean deleted;
}
