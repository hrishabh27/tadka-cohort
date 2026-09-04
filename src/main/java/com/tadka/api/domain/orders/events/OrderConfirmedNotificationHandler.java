package com.tadka.api.domain.orders.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderConfirmedNotificationHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderConfirmedNotificationHandler.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handle(OrderConfirmedEvent event) {
        log.info("Notification: order {} confirmed — SMS sent to customer {}", event.orderId(), event.customerId());
    }
}
