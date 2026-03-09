package cn.iocoder.yudao.module.cpa.job;

import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaDailyPushDO;
import cn.iocoder.yudao.module.cpa.enums.CpaPushStatusEnum;
import cn.iocoder.yudao.module.cpa.service.push.CpaDailyPushService;
import cn.iocoder.yudao.module.cpa.service.wechat.CpaWechatPushService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日推送发送定时任务
 * 每天07:30执行
 */
@Slf4j
@Component
public class CpaDailyPushJob {

    @Resource
    private CpaDailyPushService pushService;

    @Resource
    private CpaWechatPushService wechatPushService;

    @Scheduled(cron = "0 30 7 * * ?")
    public void execute() {
        log.info("[CpaDailyPushJob] 开始推送今日学习内容...");
        LocalDate today = LocalDate.now();

        CpaDailyPushDO push = pushService.getByDate(today);
        if (push == null) {
            log.warn("[CpaDailyPushJob] 今日没有待推送内容");
            return;
        }

        if (!CpaPushStatusEnum.PENDING.getCode().equals(push.getPushStatus())) {
            log.info("[CpaDailyPushJob] 今日内容已推送，跳过");
            return;
        }

        // 推送到微信（带重试）
        int maxRetries = 3;
        for (int i = 1; i <= maxRetries; i++) {
            try {
                wechatPushService.pushToAll(push);
                push.setPushStatus(CpaPushStatusEnum.PUSHED.getCode());
                push.setPushTime(LocalDateTime.now());
                pushService.updatePushStatus(push.getId(), push.getPushStatus());
                log.info("[CpaDailyPushJob] 推送成功: {}", push.getTitle());
                return;
            } catch (Exception e) {
                log.warn("[CpaDailyPushJob] 推送失败，第{}次重试", i, e);
                if (i == maxRetries) {
                    pushService.updatePushStatus(push.getId(), CpaPushStatusEnum.FAILED.getCode());
                    log.error("[CpaDailyPushJob] 推送最终失败: {}", push.getTitle());
                }
            }
        }
    }
}
