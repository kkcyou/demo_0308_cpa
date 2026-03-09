package cn.iocoder.yudao.module.cpa.dal.mysql;

import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaArchiveDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CpaArchiveMapper extends BaseMapper<CpaArchiveDO> {

    default CpaArchiveDO selectByPushId(Long pushId) {
        return selectOne(new LambdaQueryWrapper<CpaArchiveDO>()
                .eq(CpaArchiveDO::getPushId, pushId));
    }
}
