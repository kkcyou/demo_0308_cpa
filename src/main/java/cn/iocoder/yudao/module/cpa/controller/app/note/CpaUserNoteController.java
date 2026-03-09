package cn.iocoder.yudao.module.cpa.controller.app.note;

import cn.iocoder.yudao.module.cpa.controller.app.note.vo.CpaUserNoteSaveReqVO;
import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaUserNoteDO;
import cn.iocoder.yudao.module.cpa.dal.mysql.CpaUserNoteMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户端 - 个人笔记")
@RestController
@RequestMapping("/app-api/cpa/note")
public class CpaUserNoteController {

    @Resource
    private CpaUserNoteMapper noteMapper;

    private static final Long CURRENT_USER_ID = 1L;

    @PostMapping("/create")
    @Operation(summary = "创建笔记")
    public ResponseEntity<Long> create(@Valid @RequestBody CpaUserNoteSaveReqVO req) {
        CpaUserNoteDO note = new CpaUserNoteDO();
        note.setUserId(CURRENT_USER_ID);
        note.setSubject(req.getSubject());
        note.setKnowledgePointId(req.getKnowledgePointId());
        note.setPushId(req.getPushId());
        note.setTitle(req.getTitle());
        note.setContent(req.getContent());
        note.setTags(req.getTags());
        note.setStarred(false);
        noteMapper.insert(note);
        return ResponseEntity.ok(note.getId());
    }

    @PutMapping("/update")
    @Operation(summary = "修改笔记")
    public ResponseEntity<Void> update(@Valid @RequestBody CpaUserNoteSaveReqVO req) {
        CpaUserNoteDO note = noteMapper.selectById(req.getId());
        if (note == null || !note.getUserId().equals(CURRENT_USER_ID)) {
            return ResponseEntity.notFound().build();
        }
        note.setTitle(req.getTitle());
        note.setContent(req.getContent());
        note.setTags(req.getTags());
        note.setSubject(req.getSubject());
        noteMapper.updateById(note);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除笔记")
    public ResponseEntity<Void> delete(@RequestParam Long id) {
        CpaUserNoteDO note = noteMapper.selectById(id);
        if (note != null && note.getUserId().equals(CURRENT_USER_ID)) {
            noteMapper.deleteById(id);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/page")
    @Operation(summary = "我的笔记分页")
    public ResponseEntity<Page<CpaUserNoteDO>> page(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Integer subject,
            @RequestParam(required = false) Boolean starred) {
        Page<CpaUserNoteDO> page = noteMapper.selectPage(
                new Page<>(pageNo, pageSize), CURRENT_USER_ID, subject, starred);
        return ResponseEntity.ok(page);
    }

    @PutMapping("/star")
    @Operation(summary = "标星/取消标星")
    public ResponseEntity<Void> toggleStar(@RequestParam Long id) {
        CpaUserNoteDO note = noteMapper.selectById(id);
        if (note != null && note.getUserId().equals(CURRENT_USER_ID)) {
            note.setStarred(!note.getStarred());
            noteMapper.updateById(note);
        }
        return ResponseEntity.ok().build();
    }
}
