package cn.iocoder.yudao.module.cpa.dal.mysql;

import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaDailyPushDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;

@Mapper
public interface CpaDailyPushMapper extends BaseMapper<CpaDailyPushDO> {

    default CpaDailyPushDO selectByPushDate(LocalDate pushDate) {
        return selectOne(new LambdaQueryWrapper<CpaDailyPushDO>()
                .eq(CpaDailyPushDO::getPushDate, pushDate));
    }

    default CpaDailyPushDO selectPendingByDate(LocalDate pushDate) {
        return selectOne(new LambdaQueryWrapper<CpaDailyPushDO>()
                .eq(CpaDailyPushDO::getPushDate, pushDate)
                .eq(CpaDailyPushDO::getPushStatus, 0));
    }
}
