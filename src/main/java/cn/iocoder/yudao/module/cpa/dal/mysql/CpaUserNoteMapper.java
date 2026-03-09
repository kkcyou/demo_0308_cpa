package cn.iocoder.yudao.module.cpa.dal.mysql;

import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaUserNoteDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CpaUserNoteMapper extends BaseMapper<CpaUserNoteDO> {

    default Page<CpaUserNoteDO> selectPage(Page<CpaUserNoteDO> page, Long userId, Integer subject, Boolean starred) {
        return selectPage(page, new LambdaQueryWrapper<CpaUserNoteDO>()
                .eq(CpaUserNoteDO::getUserId, userId)
                .eq(subject != null, CpaUserNoteDO::getSubject, subject)
                .eq(starred != null, CpaUserNoteDO::getStarred, starred)
                .orderByDesc(CpaUserNoteDO::getUpdateTime));
    }
}
