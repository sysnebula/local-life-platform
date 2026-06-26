package com.localife.platform.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置（秒杀异步下单）
 */
@Configuration
public class RabbitMQConfig {

    public static final String SECKILL_EXCHANGE = "seckill.order.exchange";
    public static final String SECKILL_QUEUE = "seckill.order.queue";
    public static final String SECKILL_ROUTING_KEY = "seckill.order.create";

    @Bean
    public TopicExchange seckillExchange() {
        return new TopicExchange(SECKILL_EXCHANGE, true, false);
    }

    @Bean
    public Queue seckillQueue() {
        return new Queue(SECKILL_QUEUE, true);
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public Binding seckillBinding() {
        return BindingBuilder
                .bind(seckillQueue())
                .to(seckillExchange())
                .with(SECKILL_ROUTING_KEY);
    }
}
