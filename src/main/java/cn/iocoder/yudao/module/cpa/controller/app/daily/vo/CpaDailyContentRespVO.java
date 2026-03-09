package cn.iocoder.yudao.module.cpa.controller.app.daily.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户端 - 每日学习内容 Response VO")
public class CpaDailyContentRespVO {

    @Schema(description = "推送ID")
    private Long id;

    @Schema(description = "推送日期")
    private LocalDate pushDate;

    @Schema(description = "科目code")
    private Integer subject;

    @Schema(description = "科目名称")
    private String subjectName;

    @Schema(description = "推送标题")
    private String title;

    @Schema(description = "新闻要点")
    private String newsSummary;

    @Schema(description = "考点讲解（Markdown）")
    private String pointExplain;

    @Schema(description = "真题讲解（Markdown）")
    private String questionExplain;

    @Schema(description = "记忆辅助（Markdown）")
    private String mnemonic;

    @Schema(description = "思维导图数据（JSON）")
    private String mindmapData;

    @Schema(description = "考点标题")
    private String knowledgePointTitle;

    @Schema(description = "章节")
    private String chapter;
}
