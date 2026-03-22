package com.example.order.repository;

import com.example.order.model.OrderRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRecommendationRepository extends JpaRepository<OrderRecommendation, Integer> {

    List<OrderRecommendation> findByOrderId(Integer orderId);
}