package cn.iocoder.yudao.module.cpa.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 微信推送渠道枚举
 */
@Getter
@AllArgsConstructor
public enum CpaWechatChannelEnum {

    OFFICIAL_ACCOUNT(1, "公众号"),
    WORK_WECHAT(2, "企业微信");

    private final Integer code;
    private final String name;
}
