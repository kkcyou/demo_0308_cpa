package cn.iocoder.yudao.module.cpa.dal.mysql;

import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaDailyNewsDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CpaDailyNewsMapper extends BaseMapper<CpaDailyNewsDO> {

    default CpaDailyNewsDO selectOneUnused() {
        return selectOne(new LambdaQueryWrapper<CpaDailyNewsDO>()
                .eq(CpaDailyNewsDO::getUsed, false)
                .orderByDesc(CpaDailyNewsDO::getFetchDate)
                .last("LIMIT 1"));
    }

    default List<CpaDailyNewsDO> selectListByFetchDate(LocalDate fetchDate) {
        return selectList(new LambdaQueryWrapper<CpaDailyNewsDO>()
                .eq(CpaDailyNewsDO::getFetchDate, fetchDate));
    }

    default boolean existsByTitle(String title) {
        return selectCount(new LambdaQueryWrapper<CpaDailyNewsDO>()
                .eq(CpaDailyNewsDO::getTitle, title)) > 0;
    }
}
