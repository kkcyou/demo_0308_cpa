package cn.iocoder.yudao.module.cpa.controller.admin.wechat;

import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaWechatConfigDO;
import cn.iocoder.yudao.module.cpa.dal.mysql.CpaWechatConfigMapper;
import cn.iocoder.yudao.module.cpa.service.wechat.CpaWechatPushService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理后台 - 微信推送配置")
@RestController
@RequestMapping("/admin-api/cpa/wechat-config")
public class CpaWechatConfigController {

    @Resource
    private CpaWechatConfigMapper wechatConfigMapper;

    @Resource
    private CpaWechatPushService wechatPushService;

    @GetMapping("/list")
    @Operation(summary = "获取推送配置列表")
    public ResponseEntity<List<CpaWechatConfigDO>> list() {
        return ResponseEntity.ok(wechatConfigMapper.selectList(null));
    }

    @PostMapping("/save")
    @Operation(summary = "保存推送配置")
    public ResponseEntity<Long> save(@RequestBody CpaWechatConfigDO config) {
        if (config.getId() != null) {
            wechatConfigMapper.updateById(config);
        } else {
            wechatConfigMapper.insert(config);
        }
        return ResponseEntity.ok(config.getId());
    }

    @PostMapping("/test-push")
    @Operation(summary = "测试推送")
    public ResponseEntity<String> testPush(@RequestParam Long configId) {
        try {
            wechatPushService.testPush(configId);
            return ResponseEntity.ok("测试推送成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("测试推送失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除推送配置")
    public ResponseEntity<Void> delete(@RequestParam Long id) {
        wechatConfigMapper.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
