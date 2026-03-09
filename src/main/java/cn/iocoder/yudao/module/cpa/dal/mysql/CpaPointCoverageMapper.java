package cn.iocoder.yudao.module.cpa.dal.mysql;

import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaPointCoverageDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper
public interface CpaPointCoverageMapper extends BaseMapper<CpaPointCoverageDO> {

    default Set<Long> selectCoveredPointIdsByRound(Integer round) {
        List<CpaPointCoverageDO> list = selectList(new LambdaQueryWrapper<CpaPointCoverageDO>()
                .eq(CpaPointCoverageDO::getCoverageRound, round));
        return list.stream().map(CpaPointCoverageDO::getKnowledgePointId).collect(Collectors.toSet());
    }

    default long selectCountByRound(Integer round) {
        return selectCount(new LambdaQueryWrapper<CpaPointCoverageDO>()
                .eq(CpaPointCoverageDO::getCoverageRound, round));
    }

    @Select("SELECT IFNULL(MAX(coverage_round), 0) FROM cpa_point_coverage WHERE deleted = 0")
    Integer selectMaxRound();
}
