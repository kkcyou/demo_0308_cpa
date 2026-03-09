package cn.iocoder.yudao.module.cpa.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 真题题型枚举
 */
@Getter
@AllArgsConstructor
public enum CpaQuestionTypeEnum {

    SINGLE_CHOICE(1, "单选题"),
    MULTIPLE_CHOICE(2, "多选题"),
    CALCULATION(3, "计算分析题"),
    COMPREHENSIVE(4, "综合题"),
    SHORT_ANSWER(5, "简答题");

    private final Integer code;
    private final String name;
}
