package cn.iocoder.yudao.module.cpa.dal.mysql;

import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaKnowledgePointDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CpaKnowledgePointMapper extends BaseMapper<CpaKnowledgePointDO> {

    default List<CpaKnowledgePointDO> selectListBySubject(Integer subject) {
        return selectList(new LambdaQueryWrapper<CpaKnowledgePointDO>()
                .eq(CpaKnowledgePointDO::getSubject, subject)
                .eq(CpaKnowledgePointDO::getStatus, 0)
                .orderByAsc(CpaKnowledgePointDO::getSort));
    }

    default List<CpaKnowledgePointDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapper<CpaKnowledgePointDO>()
                .eq(CpaKnowledgePointDO::getStatus, status));
    }

    default List<CpaKnowledgePointDO> selectListByParentId(Long parentId) {
        return selectList(new LambdaQueryWrapper<CpaKnowledgePointDO>()
                .eq(CpaKnowledgePointDO::getParentId, parentId)
                .eq(CpaKnowledgePointDO::getStatus, 0)
                .orderByAsc(CpaKnowledgePointDO::getSort));
    }
}
