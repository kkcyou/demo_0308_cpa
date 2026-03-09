package cn.iocoder.yudao.module.cpa.controller.app.chat;

import cn.iocoder.yudao.module.cpa.controller.app.chat.vo.CpaChatCreateReqVO;
import cn.iocoder.yudao.module.cpa.controller.app.chat.vo.CpaChatSendReqVO;
import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaChatConversationDO;
import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaChatMessageDO;
import cn.iocoder.yudao.module.cpa.service.chat.CpaChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@Tag(name = "用户端 - AI对话")
@RestController
@RequestMapping("/app-api/cpa/chat")
public class CpaChatController {

    @Resource
    private CpaChatService chatService;

    // 临时用固定userId，实际应从Security上下文获取
    private static final Long CURRENT_USER_ID = 1L;

    @PostMapping("/conversation/create")
    @Operation(summary = "创建对话会话")
    public ResponseEntity<CpaChatConversationDO> createConversation(@Valid @RequestBody CpaChatCreateReqVO req) {
        CpaChatConversationDO conversation = chatService.createConversation(
                CURRENT_USER_ID, req.getTitle(), req.getSubject(),
                req.getKnowledgePointId(), req.getModel());
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/conversation/list")
    @Operation(summary = "我的对话列表")
    public ResponseEntity<List<CpaChatConversationDO>> listConversations() {
        return ResponseEntity.ok(chatService.listConversations(CURRENT_USER_ID));
    }

    @DeleteMapping("/conversation/delete")
    @Operation(summary = "删除对话")
    public ResponseEntity<Void> deleteConversation(@RequestParam Long id) {
        chatService.deleteConversation(id, CURRENT_USER_ID);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/message/send")
    @Operation(summary = "发送消息（同步）")
    public ResponseEntity<String> sendMessage(@Valid @RequestBody CpaChatSendReqVO req) {
        String reply = chatService.sendMessage(req.getConversationId(), req.getMessage(), CURRENT_USER_ID);
        return ResponseEntity.ok(reply);
    }

    @PostMapping(value = "/message/send-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "发送消息（SSE流式）")
    public Flux<String> sendMessageStream(@Valid @RequestBody CpaChatSendReqVO req) {
        return chatService.sendMessageStream(req.getConversationId(), req.getMessage(), CURRENT_USER_ID);
    }

    @GetMapping("/message/list")
    @Operation(summary = "获取对话消息列表")
    public ResponseEntity<List<CpaChatMessageDO>> listMessages(@RequestParam Long conversationId) {
        return ResponseEntity.ok(chatService.listMessages(conversationId));
    }
}
