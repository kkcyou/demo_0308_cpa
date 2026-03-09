package cn.iocoder.yudao.module.cpa.job;

import cn.iocoder.yudao.module.cpa.service.archive.CpaArchiveService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 内容存档定时任务
 * 每天23:00执行
 */
@Slf4j
@Component
public class CpaArchiveJob {

    @Resource
    private CpaArchiveService archiveService;

    @Scheduled(cron = "0 0 23 * * ?")
    public void execute() {
        log.info("[CpaArchiveJob] 开始存档今日内容...");
        LocalDate today = LocalDate.now();

        try {
            archiveService.archiveByDate(today);
            log.info("[CpaArchiveJob] 存档完成");
        } catch (Exception e) {
            log.error("[CpaArchiveJob] 存档失败", e);
        }
    }
}
