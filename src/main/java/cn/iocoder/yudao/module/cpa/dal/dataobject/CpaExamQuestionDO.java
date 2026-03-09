package cn.iocoder.yudao.module.cpa.dal.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 注会历年真题 DO
 */
@Data
@TableName("cpa_exam_question")
public class CpaExamQuestionDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 科目 */
    private Integer subject;

    /** 考试年份 */
    private Integer year;

    /** 题型：1单选 2多选 3计算 4综合 5简答 */
    private Integer questionType;

    /** 题目内容 */
    private String content;

    /** 选项JSON */
    private String options;

    /** 标准答案 */
    private String answer;

    /** 解析 */
    private String analysis;

    /** 关联考点ID列表，逗号分隔 */
    private String knowledgePointIds;

    /** 难度 1-5 */
    private Integer difficulty;

    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;

    @TableLogic
    private Boolean deleted;
}
