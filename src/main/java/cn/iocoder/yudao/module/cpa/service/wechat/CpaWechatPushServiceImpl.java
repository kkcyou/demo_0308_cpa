package cn.iocoder.yudao.module.cpa.service.wechat;

import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaDailyPushDO;
import cn.iocoder.yudao.module.cpa.dal.dataobject.CpaWechatConfigDO;
import cn.iocoder.yudao.module.cpa.dal.mysql.CpaWechatConfigMapper;
import cn.iocoder.yudao.module.cpa.enums.CpaSubjectEnum;
import cn.iocoder.yudao.module.cpa.enums.CpaWechatChannelEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CpaWechatPushServiceImpl implements CpaWechatPushService {

    @Resource
    private CpaWechatConfigMapper wechatConfigMapper;

    private final WebClient webClient = WebClient.create();

    @Override
    public void pushToAll(CpaDailyPushDO push) {
        List<CpaWechatConfigDO> configs = wechatConfigMapper.selectListEnabled();
        for (CpaWechatConfigDO config : configs) {
            try {
                if (config.getChannel().equals(CpaWechatChannelEnum.WORK_WECHAT.getCode())) {
                    pushViaWorkWechat(push, config);
                } else if (config.getChannel().equals(CpaWechatChannelEnum.OFFICIAL_ACCOUNT.getCode())) {
                    pushViaOfficialAccount(push, config);
                }
                log.info("推送成功 - 渠道: {}, 用户: {}", config.getChannel(), config.getUserId());
            } catch (Exception e) {
                log.error("推送失败 - 渠道: {}, 用户: {}", config.getChannel(), config.getUserId(), e);
            }
        }
    }

    @Override
    public void testPush(Long configId) {
        CpaWechatConfigDO config = wechatConfigMapper.selectById(configId);
        if (config == null) {
            throw new RuntimeException("推送配置不存在");
        }

        CpaDailyPushDO testPush = new CpaDailyPushDO();
        testPush.setTitle("【测试】CPA Daily 推送测试");
        testPush.setSubject(1);
        testPush.setNewsSummary("这是一条测试推送消息，确认推送通道配置正确。");

        if (config.getChannel().equals(CpaWechatChannelEnum.WORK_WECHAT.getCode())) {
            pushViaWorkWechat(testPush, config);
        }
    }

    /**
     * 通过企业微信Webhook推送
     */
    private void pushViaWorkWechat(CpaDailyPushDO push, CpaWechatConfigDO config) {
        CpaSubjectEnum subject = CpaSubjectEnum.getByCode(push.getSubject());
        String subjectName = subject != null ? subject.getName() : "未知";

        String markdownContent = String.format("""
                # CPA每日学习 - %s
                > 科目：**%s** | 日期：%s

                ## 今日新闻
                %s

                ## 考点速览
                %s

                > 点击查看完整讲解内容
                """,
                push.getTitle(),
                subjectName,
                push.getPushDate() != null ? push.getPushDate().toString() : "今日",
                truncate(push.getNewsSummary(), 200),
                truncate(push.getPointExplain(), 300));

        Map<String, Object> body = Map.of(
                "msgtype", "markdown",
                "markdown", Map.of("content", markdownContent));

        webClient.post()
                .uri(config.getWebhookUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    /**
     * 通过公众号模板消息推送（简化实现，实际需要WxJava SDK）
     */
    private void pushViaOfficialAccount(CpaDailyPushDO push, CpaWechatConfigDO config) {
        log.info("公众号推送 - openid: {}, title: {}", config.getOpenid(), push.getTitle());
        // TODO: 接入WxJava SDK实现公众号模板消息推送
        // WxMpTemplateMessage msg = WxMpTemplateMessage.builder()
        //     .toUser(config.getOpenid())
        //     .templateId(config.getTemplateId())
        //     .build();
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen) + "..." : text;
    }
}
