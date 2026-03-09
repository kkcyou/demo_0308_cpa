package cn.iocoder.yudao.module.cpa.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 推送状态枚举
 */
@Getter
@AllArgsConstructor
public enum CpaPushStatusEnum {

    PENDING(0, "待推送"),
    PUSHED(1, "已推送"),
    FAILED(2, "推送失败");

    private final Integer code;
    private final String name;
}
