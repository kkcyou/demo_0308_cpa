package cn.iocoder.yudao.module.cpa.dal.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日财经新闻 DO
 */
@Data
@TableName("cpa_daily_news")
public class CpaDailyNewsDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 新闻标题 */
    private String title;

    /** 新闻摘要 */
    private String summary;

    /** 新闻正文 */
    private String content;

    /** 来源URL */
    private String sourceUrl;

    /** 来源名称 */
    private String sourceName;

    /** 新闻发布日期 */
    private LocalDate publishDate;

    /** 抓取日期 */
    private LocalDate fetchDate;

    /** 是否已使用 */
    private Boolean used;

    /** 使用日期 */
    private LocalDate usedDate;

    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;

    @TableLogic
    private Boolean deleted;
}
