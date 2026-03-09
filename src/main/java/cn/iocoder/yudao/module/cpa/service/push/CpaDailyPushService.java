package cn.iocoder.yudao.module.cpa.service.push;

import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaDailyPushDO;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日推送服务接口
 */
public interface CpaDailyPushService {

    /**
     * 生成今日推送内容（AI驱动）
     */
    CpaDailyPushDO generateDailyContent(LocalDate date);

    /**
     * 获取指定日期的推送
     */
    CpaDailyPushDO getByDate(LocalDate date);

    /**
     * 获取推送详情
     */
    CpaDailyPushDO getById(Long id);

    /**
     * 更新推送状态
     */
    void updatePushStatus(Long id, Integer status);
}
