package cn.iocoder.yudao.module.cpa.controller.app.daily;

import cn.iocoder.yudao.module.cpa.controller.app.daily.vo.CpaDailyContentRespVO;
import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaDailyPushDO;
import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaKnowledgePointDO;
import cn.iocoder.yudao.module.cpa.dal.mysql.CpaKnowledgePointMapper;
import cn.iocoder.yudao.module.cpa.enums.CpaSubjectEnum;
import cn.iocoder.yudao.module.cpa.service.coverage.CpaPointCoverageService;
import cn.iocoder.yudao.module.cpa.service.push.CpaDailyPushService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Tag(name = "用户端 - 每日学习内容")
@RestController
@RequestMapping("/app-api/cpa/daily")
public class CpaDailyContentController {

    @Resource
    private CpaDailyPushService pushService;
    @Resource
    private CpaKnowledgePointMapper knowledgePointMapper;
    @Resource
    private CpaPointCoverageService coverageService;

    @GetMapping("/today")
    @Operation(summary = "获取今日学习内容")
    public ResponseEntity<CpaDailyContentRespVO> getToday() {
        CpaDailyPushDO push = pushService.getByDate(LocalDate.now());
        if (push == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(convertToVO(push));
    }

    @GetMapping("/get")
    @Operation(summary = "获取指定日期学习内容")
    public ResponseEntity<CpaDailyContentRespVO> getByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        CpaDailyPushDO push = pushService.getByDate(date);
        if (push == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(convertToVO(push));
    }

    @GetMapping("/detail")
    @Operation(summary = "获取推送详情")
    public ResponseEntity<CpaDailyContentRespVO> getDetail(@RequestParam Long id) {
        CpaDailyPushDO push = pushService.getById(id);
        if (push == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(convertToVO(push));
    }

    @GetMapping("/coverage")
    @Operation(summary = "获取各科覆盖率统计")
    public ResponseEntity<Map<Integer, Double>> getCoverage() {
        return ResponseEntity.ok(coverageService.getCoverageStatistics());
    }

    @PostMapping("/generate")
    @Operation(summary = "手动触发生成今日内容")
    public ResponseEntity<CpaDailyContentRespVO> generate() {
        CpaDailyPushDO push = pushService.generateDailyContent(LocalDate.now());
        return ResponseEntity.ok(convertToVO(push));
    }

    private CpaDailyContentRespVO convertToVO(CpaDailyPushDO push) {
        CpaDailyContentRespVO vo = new CpaDailyContentRespVO();
        vo.setId(push.getId());
        vo.setPushDate(push.getPushDate());
        vo.setSubject(push.getSubject());
        CpaSubjectEnum subject = CpaSubjectEnum.getByCode(push.getSubject());
        vo.setSubjectName(subject != null ? subject.getName() : "未知");
        vo.setTitle(push.getTitle());
        vo.setNewsSummary(push.getNewsSummary());
        vo.setPointExplain(push.getPointExplain());
        vo.setQuestionExplain(push.getQuestionExplain());
        vo.setMnemonic(push.getMnemonic());
        vo.setMindmapData(push.getMindmapData());

        // 补充考点信息
        if (push.getKnowledgePointId() != null) {
            CpaKnowledgePointDO point = knowledgePointMapper.selectById(push.getKnowledgePointId());
            if (point != null) {
                vo.setKnowledgePointTitle(point.getTitle());
                vo.setChapter(point.getChapter());
            }
        }
        return vo;
    }
}
