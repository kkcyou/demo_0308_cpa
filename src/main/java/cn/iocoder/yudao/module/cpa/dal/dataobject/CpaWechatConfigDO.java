package cn.iocoder.yudao.module.cpa.dal.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 微信推送配置 DO
 */
@Data
@TableName("cpa_wechat_config")
public class CpaWechatConfigDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 渠道：1公众号 2企业微信 */
    private Integer channel;

    /** 微信openid */
    private String openid;

    /** 企业微信webhook地址 */
    private String webhookUrl;

    /** 消息模板ID */
    private String templateId;

    /** 推送时间 HH:mm */
    private String pushTime;

    /** 是否启用 */
    private Boolean enabled;

    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;

    @TableLogic
    private Boolean deleted;
}
