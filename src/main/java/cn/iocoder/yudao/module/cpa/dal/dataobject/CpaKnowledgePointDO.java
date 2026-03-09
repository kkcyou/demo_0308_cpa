package cn.iocoder.yudao.module.cpa.dal.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 注会考点知识库 DO
 */
@Data
@TableName("cpa_knowledge_point")
public class CpaKnowledgePointDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 科目：1会计 2审计 3财管 4经济法 5税法 6战略 */
    private Integer subject;

    /** 章节名称 */
    private String chapter;

    /** 考点标题 */
    private String title;

    /** 考点核心内容摘要 */
    private String content;

    /** 重要程度 1-5 */
    private Integer importance;

    /** 考试频率 1-5 */
    private Integer frequency;

    /** 关键词，逗号分隔 */
    private String keywords;

    /** 父考点ID，0为顶级 */
    private Long parentId;

    /** 排序 */
    private Integer sort;

    /** 状态 0正常 1停用 */
    private Integer status;

    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;

    @TableLogic
    private Boolean deleted;
}
