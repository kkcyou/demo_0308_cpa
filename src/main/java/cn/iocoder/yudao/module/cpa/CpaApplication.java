package cn.iocoder.yudao.module.cpa;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
        org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration.class
})
@MapperScan("cn.iocoder.yudao.module.cpa.dal.mysql")
@EnableScheduling
public class CpaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CpaApplication.class, args);
    }
}
