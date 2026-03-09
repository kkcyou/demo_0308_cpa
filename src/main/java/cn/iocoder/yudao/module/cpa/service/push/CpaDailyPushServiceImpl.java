package cn.iocoder.yudao.module.cpa.service.push;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.cpa.dal.dataobject.*;
import cn.iocoder.yudao.module.cpa.dal.mysql.*;
import cn.iocoder.yudao.module.cpa.enums.CpaPushStatusEnum;
import cn.iocoder.yudao.module.cpa.enums.CpaSubjectEnum;
import cn.iocoder.yudao.module.cpa.service.ai.CpaAiService;
import cn.iocoder.yudao.module.cpa.service.coverage.CpaPointCoverageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class CpaDailyPushServiceImpl implements CpaDailyPushService {

    @Resource
    private CpaDailyPushMapper pushMapper;
    @Resource
    private CpaDailyNewsMapper newsMapper;
    @Resource
    private CpaKnowledgePointMapper knowledgePointMapper;
    @Resource
    private CpaExamQuestionMapper questionMapper;
    @Resource
    private CpaAiService aiService;
    @Resource
    private CpaPointCoverageService coverageService;

    @Override
    @Transactional
    public CpaDailyPushDO generateDailyContent(LocalDate date) {
        // 检查是否已生成
        CpaDailyPushDO existing = pushMapper.selectByPushDate(date);
        if (existing != null) {
            log.info("日期 {} 已存在推送内容，跳过生成", date);
            return existing;
        }

        // 1. 获取一条未使用的新闻
        CpaDailyNewsDO news = newsMapper.selectOneUnused();
        if (news == null) {
            log.warn("没有可用新闻，使用AI生成降级新闻");
            news = generateFallbackNews(date);
        }

        // 2. 获取下一个未覆盖的考点
        CpaKnowledgePointDO point = coverageService.selectNextUncoveredPoint();
        if (point == null) {
            log.error("没有可用考点，请先录入考点数据");
            throw new RuntimeException("没有可用考点");
        }

        // 3. AI生成考点讲解
        log.info("开始AI生成内容 - 考点: {}（{}）", point.getTitle(),
                CpaSubjectEnum.getByCode(point.getSubject()).getName());
        String pointExplain = aiService.generatePointExplanation(point, news);

        // 4. 关联真题并AI生成讲解
        List<CpaExamQuestionDO> questions = questionMapper.selectListByKnowledgePointId(point.getId());
        String questionExplain = "";
        String questionIds = "";
        if (!questions.isEmpty()) {
            CpaExamQuestionDO question = questions.get(0); // 取第一道关联真题
            questionExplain = aiService.generateQuestionAnalysis(question, point);
            questionIds = String.valueOf(question.getId());
        }

        // 5. AI生成记忆辅助
        String mnemonic = aiService.generateMnemonic(point);

        // 6. 组装推送内容
        CpaDailyPushDO push = new CpaDailyPushDO();
        push.setPushDate(date);
        push.setNewsId(news.getId());
        push.setKnowledgePointId(point.getId());
        push.setSubject(point.getSubject());
        push.setTitle(String.format("【%s】%s",
                CpaSubjectEnum.getByCode(point.getSubject()).getName(),
                point.getTitle()));
        push.setNewsSummary(news.getTitle() + "：" + news.getSummary());
        push.setPointExplain(pointExplain);
        push.setQuestionIds(questionIds);
        push.setQuestionExplain(questionExplain);
        push.setMnemonic(mnemonic);
        push.setPushStatus(CpaPushStatusEnum.PENDING.getCode());
        pushMapper.insert(push);

        // 7. 标记新闻已使用
        news.setUsed(true);
        news.setUsedDate(date);
        newsMapper.updateById(news);

        // 8. 标记考点已覆盖
        coverageService.markCovered(point.getId(), push.getId(), date);

        log.info("日期 {} 推送内容生成完成: {}", date, push.getTitle());
        return push;
    }

    @Override
    public CpaDailyPushDO getByDate(LocalDate date) {
        return pushMapper.selectByPushDate(date);
    }

    @Override
    public CpaDailyPushDO getById(Long id) {
        return pushMapper.selectById(id);
    }

    @Override
    public void updatePushStatus(Long id, Integer status) {
        CpaDailyPushDO push = new CpaDailyPushDO();
        push.setId(id);
        push.setPushStatus(status);
        pushMapper.updateById(push);
    }

    /**
     * 生成降级新闻
     */
    private CpaDailyNewsDO generateFallbackNews(LocalDate date) {
        // 随机选一个科目生成新闻
        int subjectCode = (date.getDayOfYear() % 6) + 1;
        String newsJson = aiService.generateFallbackNews(subjectCode);

        CpaDailyNewsDO news = new CpaDailyNewsDO();
        try {
            JSONObject json = JSONUtil.parseObj(newsJson);
            news.setTitle(json.getStr("title"));
            news.setSummary(json.getStr("summary"));
            news.setContent(json.getStr("content"));
        } catch (Exception e) {
            news.setTitle("财经要闻：市场动态分析");
            news.setSummary(newsJson);
            news.setContent(newsJson);
        }
        news.setSourceName("AI生成");
        news.setPublishDate(date);
        news.setFetchDate(date);
        news.setUsed(false);
        newsMapper.insert(news);

        return news;
    }
}
