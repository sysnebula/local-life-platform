package com.localife.platform.module.dianping.voucher.listener;

import com.localife.platform.config.RabbitMQConfig;
import com.localife.platform.module.dianping.voucher.entity.VoucherOrder;
import com.localife.platform.module.dianping.voucher.mapper.VoucherOrderMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 秒杀订单异步消费者 — 削峰填谷
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillOrderListener {

    private final VoucherOrderMapper voucherOrderMapper;

    @RabbitListener(queues = RabbitMQConfig.SECKILL_QUEUE)
    public void onMessage(VoucherOrder order, Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            log.info("秒杀异步下单: userId={}, voucherId={}", order.getUserId(), order.getVoucherId());
            // 幂等处理：订单可能已在同步流程中插入
            if (voucherOrderMapper.selectById(order.getId()) == null) {
                voucherOrderMapper.insert(order);
            }
            channel.basicAck(tag, false);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            log.warn("订单已存在，跳过插入: {}", order.getId());
            try { channel.basicAck(tag, false); } catch (IOException ex) {}
        } catch (Exception e) {
            log.error("秒杀下单失败", e);
            try {
                channel.basicNack(tag, false, true);
            } catch (IOException ex) {
                log.error("消息确认失败", ex);
            }
        }
    }
}
