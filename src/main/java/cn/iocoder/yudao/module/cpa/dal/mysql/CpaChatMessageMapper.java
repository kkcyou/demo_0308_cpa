package cn.iocoder.yudao.module.cpa.dal.mysql;

import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaChatMessageDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CpaChatMessageMapper extends BaseMapper<CpaChatMessageDO> {

    default List<CpaChatMessageDO> selectListByConversationId(Long conversationId) {
        return selectList(new LambdaQueryWrapper<CpaChatMessageDO>()
                .eq(CpaChatMessageDO::getConversationId, conversationId)
                .orderByAsc(CpaChatMessageDO::getCreateTime));
    }
}
