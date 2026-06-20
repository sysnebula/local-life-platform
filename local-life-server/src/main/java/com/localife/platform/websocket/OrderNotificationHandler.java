package com.localife.platform.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 订单通知 — 商家端实时接收新订单提醒
 */
@Slf4j
@Component
@ServerEndpoint("/ws/notification/{shopId}")
public class OrderNotificationHandler {

    /**
     * shopId → Session
     */
    private static final Map<Long, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("shopId") Long shopId) {
        sessions.put(shopId, session);
        log.info("WebSocket 连接: shopId={}", shopId);
    }

    @OnClose
    public void onClose(@PathParam("shopId") Long shopId) {
        sessions.remove(shopId);
    }

    @OnError
    public void onError(Throwable e) {
        log.error("WebSocket 错误", e);
    }

    /**
     * 向指定店铺推送新订单消息
     */
    public static void notifyNewOrder(Long shopId, String message) {
        Session session = sessions.get(shopId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.error("WebSocket 推送失败", e);
            }
        }
    }
}
