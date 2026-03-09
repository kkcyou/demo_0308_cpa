package cn.iocoder.yudao.module.cpa.controller.admin.knowledge;

import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaKnowledgePointDO;
import cn.iocoder.yudao.module.cpa.dal.mysql.CpaKnowledgePointMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理后台 - 考点管理")
@RestController
@RequestMapping("/admin-api/cpa/knowledge-point")
public class CpaKnowledgePointController {

    @Resource
    private CpaKnowledgePointMapper knowledgePointMapper;

    @PostMapping("/create")
    @Operation(summary = "创建考点")
    public ResponseEntity<Long> create(@RequestBody CpaKnowledgePointDO point) {
        knowledgePointMapper.insert(point);
        return ResponseEntity.ok(point.getId());
    }

    @PutMapping("/update")
    @Operation(summary = "修改考点")
    public ResponseEntity<Void> update(@RequestBody CpaKnowledgePointDO point) {
        knowledgePointMapper.updateById(point);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除考点")
    public ResponseEntity<Void> delete(@RequestParam Long id) {
        knowledgePointMapper.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get")
    @Operation(summary = "获取考点详情")
    public ResponseEntity<CpaKnowledgePointDO> get(@RequestParam Long id) {
        return ResponseEntity.ok(knowledgePointMapper.selectById(id));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询考点")
    public ResponseEntity<Page<CpaKnowledgePointDO>> page(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Integer subject) {
        Page<CpaKnowledgePointDO> page = knowledgePointMapper.selectPage(
                new Page<>(pageNo, pageSize),
                new LambdaQueryWrapper<CpaKnowledgePointDO>()
                        .eq(subject != null, CpaKnowledgePointDO::getSubject, subject)
                        .eq(CpaKnowledgePointDO::getStatus, 0)
                        .orderByAsc(CpaKnowledgePointDO::getSubject)
                        .orderByAsc(CpaKnowledgePointDO::getSort));
        return ResponseEntity.ok(page);
    }

    @GetMapping("/tree")
    @Operation(summary = "按科目获取考点树")
    public ResponseEntity<List<CpaKnowledgePointDO>> tree(@RequestParam Integer subject) {
        return ResponseEntity.ok(knowledgePointMapper.selectListBySubject(subject));
    }

    @GetMapping("/list-all")
    @Operation(summary = "获取所有启用考点")
    public ResponseEntity<List<CpaKnowledgePointDO>> listAll() {
        return ResponseEntity.ok(knowledgePointMapper.selectListByStatus(0));
    }
}
