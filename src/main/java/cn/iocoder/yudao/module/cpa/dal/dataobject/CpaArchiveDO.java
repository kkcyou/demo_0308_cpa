package cn.iocoder.yudao.module.cpa.dal.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 内容存档 DO
 */
@Data
@TableName("cpa_archive")
public class CpaArchiveDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联推送ID */
    private Long pushId;

    /** 科目 */
    private Integer subject;

    /** 推送日期 */
    private LocalDate pushDate;

    /** 考点ID */
    private Long knowledgePointId;

    /** 存档文件路径 */
    private String filePath;

    /** 文件格式 */
    private String fileFormat;

    /** 文件大小（字节） */
    private Long fileSize;

    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;

    @TableLogic
    private Boolean deleted;
}
