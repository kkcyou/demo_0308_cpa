package cn.iocoder.yudao.module.cpa.dal.mysql;

import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaWechatConfigDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CpaWechatConfigMapper extends BaseMapper<CpaWechatConfigDO> {

    default List<CpaWechatConfigDO> selectListEnabled() {
        return selectList(new LambdaQueryWrapper<CpaWechatConfigDO>()
                .eq(CpaWechatConfigDO::getEnabled, true));
    }

    default CpaWechatConfigDO selectByUserId(Long userId) {
        return selectOne(new LambdaQueryWrapper<CpaWechatConfigDO>()
                .eq(CpaWechatConfigDO::getUserId, userId));
    }
}
