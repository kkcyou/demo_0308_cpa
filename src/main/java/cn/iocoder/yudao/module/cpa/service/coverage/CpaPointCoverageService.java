package cn.iocoder.yudao.module.cpa.service.coverage;

import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaKnowledgePointDO;

import java.time.LocalDate;
import java.util.Map;

/**
 * 考点覆盖（去重）服务接口
 */
public interface CpaPointCoverageService {

    /**
     * 获取下一个未覆盖的考点
     */
    CpaKnowledgePointDO selectNextUncoveredPoint();

    /**
     * 标记考点已覆盖
     */
    void markCovered(Long knowledgePointId, Long pushId, LocalDate pushDate);

    /**
     * 获取当前轮次
     */
    int getCurrentRound();

    /**
     * 获取各科目覆盖率统计
     *
     * @return key=科目code, value=覆盖率百分比
     */
    Map<Integer, Double> getCoverageStatistics();
}
