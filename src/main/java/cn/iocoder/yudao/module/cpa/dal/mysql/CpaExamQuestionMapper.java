package cn.iocoder.yudao.module.cpa.dal.mysql;

import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaExamQuestionDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CpaExamQuestionMapper extends BaseMapper<CpaExamQuestionDO> {

    default List<CpaExamQuestionDO> selectListBySubjectAndYear(Integer subject, Integer year) {
        return selectList(new LambdaQueryWrapper<CpaExamQuestionDO>()
                .eq(CpaExamQuestionDO::getSubject, subject)
                .eq(year != null, CpaExamQuestionDO::getYear, year));
    }

    default List<CpaExamQuestionDO> selectListByKnowledgePointId(Long knowledgePointId) {
        return selectList(new LambdaQueryWrapper<CpaExamQuestionDO>()
                .apply("FIND_IN_SET({0}, knowledge_point_ids)", knowledgePointId));
    }
}
