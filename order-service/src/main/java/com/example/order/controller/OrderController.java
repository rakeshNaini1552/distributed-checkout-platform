package com.example.order.controller;


import com.example.order.dto.OrderRequest;
import com.example.order.dto.OrderResponse;
import com.example.order.model.Order;
import com.example.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest orderRequest){
        Order orderResponse = orderService.createOrder(orderRequest);
        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getAllOrdersForUser(@PathVariable Integer userId){
        return ResponseEntity.ok(orderService.getAllOrdersbyUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderDetails(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.getOrderDetailsByOrderId(id));
    }

}
