package com.radionet.orderservice.controller;

import com.radionet.basedomains.dto.Order;
import com.radionet.basedomains.dto.OrderEvent;
import com.radionet.orderservice.kafka.OrderProducer;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("api/v1")
public class OrderController {

    private OrderProducer orderProducer;

    public OrderController(OrderProducer orderProducer) {
        this.orderProducer = orderProducer;
    }

    @PostMapping("/orders")
    public String PlaceOrder (@RequestBody Order order){

        order.setOrderId(UUID.randomUUID().toString());

        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setStatus("Pending");
        orderEvent.setMessage("Order status: pending");
        orderEvent.setOrder(order);

        orderProducer.sendMessage(orderEvent);

        return "Successfully operation.";
    }
}
