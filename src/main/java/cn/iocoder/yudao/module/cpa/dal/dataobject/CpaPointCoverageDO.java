package cn.iocoder.yudao.module.cpa.dal.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考点覆盖记录 DO（去重追踪）
 */
@Data
@TableName("cpa_point_coverage")
public class CpaPointCoverageDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 考点ID */
    private Long knowledgePointId;

    /** 推送ID */
    private Long pushId;

    /** 推送日期 */
    private LocalDate pushDate;

    /** 轮次（第几轮复习到该考点） */
    private Integer coverageRound;

    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;

    @TableLogic
    private Boolean deleted;
}
