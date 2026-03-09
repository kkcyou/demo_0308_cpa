package cn.iocoder.yudao.module.cpa.controller.admin.statistics;

import cn.iocoder.yudao.module.cpa.service.coverage.CpaPointCoverageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "管理后台 - 学习统计")
@RestController
@RequestMapping("/admin-api/cpa/statistics")
public class CpaStatisticsController {

    @Resource
    private CpaPointCoverageService coverageService;

    @GetMapping("/coverage")
    @Operation(summary = "各科覆盖率统计")
    public ResponseEntity<Map<String, Object>> getCoverage() {
        Map<String, Object> result = new HashMap<>();
        result.put("currentRound", coverageService.getCurrentRound());
        result.put("subjectCoverage", coverageService.getCoverageStatistics());
        return ResponseEntity.ok(result);
    }
}
