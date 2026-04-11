package com.radiostation.subscription.messaging;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.kafka.receiver.KafkaReceiver;

@Component
public class SubscriptionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionEventConsumer.class);

    private final KafkaReceiver<String, String> receiver;

    public SubscriptionEventConsumer(KafkaReceiver<String, String> receiver) {
        this.receiver = receiver;
    }

    @PostConstruct
    public void consume() {
        receiver.receive()
                .doOnNext(record -> {
                    log.info("Received event: key={} value={}", record.key(), record.value());
                    record.receiverOffset().acknowledge();
                })
                .doOnError(e -> log.error("Error consuming event", e))
                .subscribe();
    }
}
