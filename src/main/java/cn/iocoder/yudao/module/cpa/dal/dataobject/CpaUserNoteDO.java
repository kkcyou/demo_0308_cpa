package cn.iocoder.yudao.module.cpa.dal.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户个人笔记 DO
 */
@Data
@TableName("cpa_user_note")
public class CpaUserNoteDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 科目 */
    private Integer subject;

    /** 关联考点ID */
    private Long knowledgePointId;

    /** 关联推送ID */
    private Long pushId;

    /** 笔记标题 */
    private String title;

    /** 笔记内容（Markdown） */
    private String content;

    /** 标签，逗号分隔 */
    private String tags;

    /** 是否标星 */
    private Boolean starred;

    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;

    @TableLogic
    private Boolean deleted;
}
