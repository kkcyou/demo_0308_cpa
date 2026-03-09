package cn.iocoder.yudao.module.cpa.dal.mysql;

import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaChatConversationDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CpaChatConversationMapper extends BaseMapper<CpaChatConversationDO> {

    default List<CpaChatConversationDO> selectListByUserId(Long userId) {
        return selectList(new LambdaQueryWrapper<CpaChatConversationDO>()
                .eq(CpaChatConversationDO::getUserId, userId)
                .orderByDesc(CpaChatConversationDO::getPinned)
                .orderByDesc(CpaChatConversationDO::getUpdateTime));
    }
}
