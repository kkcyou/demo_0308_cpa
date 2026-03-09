package cn.iocoder.yudao.module.cpa.service.archive;

import java.time.LocalDate;

/**
 * 内容存档服务接口
 */
public interface CpaArchiveService {

    /**
     * 将指定日期的推送内容存档为Markdown文件
     */
    void archiveByDate(LocalDate date);

    /**
     * 获取存档文件路径
     */
    String getArchiveFilePath(Long pushId);
}
