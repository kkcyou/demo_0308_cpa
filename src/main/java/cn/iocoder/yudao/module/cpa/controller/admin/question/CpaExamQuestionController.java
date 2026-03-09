package cn.iocoder.yudao.module.cpa.controller.admin.question;

import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaExamQuestionDO;
import cn.iocoder.yudao.module.cpa.dal.mysql.CpaExamQuestionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理后台 - 真题管理")
@RestController
@RequestMapping("/admin-api/cpa/exam-question")
public class CpaExamQuestionController {

    @Resource
    private CpaExamQuestionMapper questionMapper;

    @PostMapping("/create")
    @Operation(summary = "创建真题")
    public ResponseEntity<Long> create(@RequestBody CpaExamQuestionDO question) {
        questionMapper.insert(question);
        return ResponseEntity.ok(question.getId());
    }

    @PutMapping("/update")
    @Operation(summary = "修改真题")
    public ResponseEntity<Void> update(@RequestBody CpaExamQuestionDO question) {
        questionMapper.updateById(question);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除真题")
    public ResponseEntity<Void> delete(@RequestParam Long id) {
        questionMapper.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询真题")
    public ResponseEntity<Page<CpaExamQuestionDO>> page(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Integer subject,
            @RequestParam(required = false) Integer year) {
        Page<CpaExamQuestionDO> page = questionMapper.selectPage(
                new Page<>(pageNo, pageSize),
                new LambdaQueryWrapper<CpaExamQuestionDO>()
                        .eq(subject != null, CpaExamQuestionDO::getSubject, subject)
                        .eq(year != null, CpaExamQuestionDO::getYear, year)
                        .orderByDesc(CpaExamQuestionDO::getYear));
        return ResponseEntity.ok(page);
    }
}
