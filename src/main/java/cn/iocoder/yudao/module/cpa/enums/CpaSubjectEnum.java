package cn.iocoder.yudao.module.cpa.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 注会考试科目枚举
 */
@Getter
@AllArgsConstructor
public enum CpaSubjectEnum {

    ACCOUNTING(1, "会计"),
    AUDITING(2, "审计"),
    FINANCIAL_MANAGEMENT(3, "财管"),
    ECONOMIC_LAW(4, "经济法"),
    TAX_LAW(5, "税法"),
    STRATEGY(6, "战略");

    private final Integer code;
    private final String name;

    public static CpaSubjectEnum getByCode(Integer code) {
        for (CpaSubjectEnum subject : values()) {
            if (subject.getCode().equals(code)) {
                return subject;
            }
        }
        return null;
    }
}
