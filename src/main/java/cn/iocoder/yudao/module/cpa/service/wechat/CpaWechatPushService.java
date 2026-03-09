package cn.iocoder.yudao.module.cpa.service.wechat;

import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaDailyPushDO;

/**
 * 微信推送服务接口
 */
public interface CpaWechatPushService {

    /**
     * 推送每日学习内容到所有启用的渠道
     */
    void pushToAll(CpaDailyPushDO push);

    /**
     * 测试推送
     */
    void testPush(Long configId);
}
