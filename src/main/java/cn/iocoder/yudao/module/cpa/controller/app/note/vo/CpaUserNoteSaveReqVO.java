package cn.iocoder.yudao.module.cpa.controller.app.note.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "用户端 - 保存笔记 Request VO")
public class CpaUserNoteSaveReqVO {

    @Schema(description = "笔记ID（更新时传入）")
    private Long id;

    @Schema(description = "科目code")
    private Integer subject;

    @Schema(description = "关联考点ID")
    private Long knowledgePointId;

    @Schema(description = "关联推送ID")
    private Long pushId;

    @Schema(description = "笔记标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description = "笔记内容（Markdown）")
    private String content;

    @Schema(description = "标签，逗号分隔")
    private String tags;
}
