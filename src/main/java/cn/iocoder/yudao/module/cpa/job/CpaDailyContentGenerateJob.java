package cn.iocoder.yudao.module.cpa.job;

import cn.iocoder.yudao.module.cpa.service.push.CpaDailyPushService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 每日内容生成定时任务
 * 每天06:30执行
 */
@Slf4j
@Component
public class CpaDailyContentGenerateJob {

    @Resource
    private CpaDailyPushService pushService;

    @Scheduled(cron = "0 30 6 * * ?")
    public void execute() {
        log.info("[CpaDailyContentGenerateJob] 开始生成今日学习内容...");
        LocalDate today = LocalDate.now();

        try {
            pushService.generateDailyContent(today);
            log.info("[CpaDailyContentGenerateJob] 今日内容生成完成");
        } catch (Exception e) {
            log.error("[CpaDailyContentGenerateJob] 内容生成失败", e);
        }
    }
}
