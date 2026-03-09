package cn.iocoder.yudao.module.cpa.service.archive;

import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaArchiveDO;
import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaDailyPushDO;
import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaKnowledgePointDO;
import cn.iocoder.yudao.module.cpa.dal.mysql.CpaArchiveMapper;
import cn.iocoder.yudao.module.cpa.dal.mysql.CpaDailyPushMapper;
import cn.iocoder.yudao.module.cpa.dal.mysql.CpaKnowledgePointMapper;
import cn.iocoder.yudao.module.cpa.enums.CpaSubjectEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class CpaArchiveServiceImpl implements CpaArchiveService {

    @Value("${cpa.archive.base-path:./data/cpa-archive}")
    private String basePath;

    @Resource
    private CpaDailyPushMapper pushMapper;

    @Resource
    private CpaKnowledgePointMapper knowledgePointMapper;

    @Resource
    private CpaArchiveMapper archiveMapper;

    @Override
    public void archiveByDate(LocalDate date) {
        CpaDailyPushDO push = pushMapper.selectByPushDate(date);
        if (push == null) {
            log.warn("日期 {} 没有推送内容，跳过存档", date);
            return;
        }

        // 检查是否已存档
        CpaArchiveDO existing = archiveMapper.selectByPushId(push.getId());
        if (existing != null) {
            log.info("推送 {} 已存档，跳过", push.getId());
            return;
        }

        CpaKnowledgePointDO point = knowledgePointMapper.selectById(push.getKnowledgePointId());
        CpaSubjectEnum subject = CpaSubjectEnum.getByCode(push.getSubject());

        // 构建文件路径: /archive/{科目名}/{年份}/{月日}_{考点标题}.md
        String subjectName = subject != null ? subject.getName() : "其他";
        String year = String.valueOf(date.getYear());
        String fileName = date.format(DateTimeFormatter.ofPattern("MM-dd"))
                + "_" + sanitizeFileName(point != null ? point.getTitle() : "未知考点") + ".md";

        Path filePath = Paths.get(basePath, subjectName, year, fileName);

        // 生成Markdown内容
        String markdownContent = buildMarkdownContent(push, point, date, subjectName);

        try {
            // 创建目录
            Files.createDirectories(filePath.getParent());
            // 写入文件
            Files.writeString(filePath, markdownContent, StandardCharsets.UTF_8);

            // 保存存档记录
            CpaArchiveDO archive = new CpaArchiveDO();
            archive.setPushId(push.getId());
            archive.setSubject(push.getSubject());
            archive.setPushDate(date);
            archive.setKnowledgePointId(push.getKnowledgePointId());
            archive.setFilePath(filePath.toString());
            archive.setFileFormat("md");
            archive.setFileSize(Files.size(filePath));
            archiveMapper.insert(archive);

            log.info("存档完成: {}", filePath);
        } catch (IOException e) {
            log.error("存档文件写入失败: {}", filePath, e);
            throw new RuntimeException("存档失败", e);
        }
    }

    @Override
    public String getArchiveFilePath(Long pushId) {
        CpaArchiveDO archive = archiveMapper.selectByPushId(pushId);
        return archive != null ? archive.getFilePath() : null;
    }

    private String buildMarkdownContent(CpaDailyPushDO push, CpaKnowledgePointDO point,
                                         LocalDate date, String subjectName) {
        StringBuilder sb = new StringBuilder();

        // YAML Front Matter
        sb.append("---\n");
        sb.append("date: ").append(date).append("\n");
        sb.append("subject: ").append(subjectName).append("\n");
        if (point != null) {
            sb.append("chapter: ").append(point.getChapter()).append("\n");
            sb.append("point: ").append(point.getTitle()).append("\n");
            sb.append("importance: ").append(point.getImportance()).append("\n");
        }
        sb.append("---\n\n");

        // 标题
        sb.append("# CPA每日学习 - ").append(date).append("\n\n");

        // 新闻
        if (push.getNewsSummary() != null) {
            sb.append("## 今日新闻\n\n");
            sb.append(push.getNewsSummary()).append("\n\n");
        }

        // 考点讲解
        if (push.getPointExplain() != null) {
            sb.append(push.getPointExplain()).append("\n\n");
        }

        // 真题讲解
        if (push.getQuestionExplain() != null && !push.getQuestionExplain().isEmpty()) {
            sb.append("## 关联真题\n\n");
            sb.append(push.getQuestionExplain()).append("\n\n");
        }

        // 记忆辅助
        if (push.getMnemonic() != null) {
            sb.append(push.getMnemonic()).append("\n\n");
        }

        sb.append("---\n*由 CPA Daily 学习助手自动生成*\n");
        return sb.toString();
    }

    /**
     * 清理文件名中的非法字符
     */
    private String sanitizeFileName(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
