package cn.iocoder.yudao.module.cpa.controller.app.chat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "用户端 - 创建AI对话 Request VO")
public class CpaChatCreateReqVO {

    @Schema(description = "会话标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description = "科目code")
    private Integer subject;

    @Schema(description = "关联考点ID")
    private Long knowledgePointId;

    @Schema(description = "AI模型")
    private String model;
}
