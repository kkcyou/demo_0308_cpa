package cn.iocoder.yudao.module.cpa.service.coverage;

import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaKnowledgePointDO;
import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaPointCoverageDO;
import cn.iocoder.yudao.module.cpa.dal.mysql.CpaKnowledgePointMapper;
import cn.iocoder.yudao.module.cpa.dal.mysql.CpaPointCoverageMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CpaPointCoverageServiceImpl implements CpaPointCoverageService {

    @Resource
    private CpaPointCoverageMapper coverageMapper;

    @Resource
    private CpaKnowledgePointMapper knowledgePointMapper;

    @Override
    public CpaKnowledgePointDO selectNextUncoveredPoint() {
        int currentRound = getCurrentRound();
        return findUncoveredPoint(currentRound);
    }

    private CpaKnowledgePointDO findUncoveredPoint(int round) {
        // 获取当前轮次已覆盖的考点ID
        Set<Long> coveredIds = coverageMapper.selectCoveredPointIdsByRound(round);

        // 获取所有启用的考点
        List<CpaKnowledgePointDO> allPoints = knowledgePointMapper.selectListByStatus(0);

        // 过滤出未覆盖的
        List<CpaKnowledgePointDO> uncovered = allPoints.stream()
                .filter(p -> !coveredIds.contains(p.getId()))
                .toList();

        // 全部覆盖完毕，进入下一轮
        if (uncovered.isEmpty()) {
            log.info("第{}轮考点已全部覆盖，开始第{}轮", round, round + 1);
            return findUncoveredPoint(round + 1);
        }

        // 按优先级排序：重要程度×0.6 + 考试频率×0.4
        return uncovered.stream()
                .max(Comparator.comparingDouble(p ->
                        p.getImportance() * 0.6 + p.getFrequency() * 0.4))
                .orElse(uncovered.get(0));
    }

    @Override
    public void markCovered(Long knowledgePointId, Long pushId, LocalDate pushDate) {
        int currentRound = getCurrentRound();
        CpaPointCoverageDO coverage = new CpaPointCoverageDO();
        coverage.setKnowledgePointId(knowledgePointId);
        coverage.setPushId(pushId);
        coverage.setPushDate(pushDate);
        coverage.setCoverageRound(currentRound);
        coverageMapper.insert(coverage);
    }

    @Override
    public int getCurrentRound() {
        Integer maxRound = coverageMapper.selectMaxRound();
        if (maxRound == null || maxRound == 0) {
            return 1;
        }

        long totalPoints = knowledgePointMapper.selectListByStatus(0).size();
        long coveredInRound = coverageMapper.selectCountByRound(maxRound);

        return coveredInRound >= totalPoints ? maxRound + 1 : maxRound;
    }

    @Override
    public Map<Integer, Double> getCoverageStatistics() {
        int currentRound = getCurrentRound();
        Set<Long> coveredIds = coverageMapper.selectCoveredPointIdsByRound(currentRound);
        List<CpaKnowledgePointDO> allPoints = knowledgePointMapper.selectListByStatus(0);

        // 按科目分组统计
        Map<Integer, List<CpaKnowledgePointDO>> bySubject = allPoints.stream()
                .collect(Collectors.groupingBy(CpaKnowledgePointDO::getSubject));

        Map<Integer, Double> result = new HashMap<>();
        bySubject.forEach((subject, points) -> {
            long covered = points.stream().filter(p -> coveredIds.contains(p.getId())).count();
            double rate = points.isEmpty() ? 0 : (double) covered / points.size() * 100;
            result.put(subject, Math.round(rate * 10) / 10.0);
        });

        return result;
    }
}
