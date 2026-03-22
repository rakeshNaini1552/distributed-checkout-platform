package com.example.inventory_service.service;

import com.example.events.OrderConfirmedEvent;
import com.example.events.OrderCreatedEvent;
import com.example.events.OrderRejectedEvent;
import com.example.inventory_service.kafka.InventoryEventPublisher;
import com.example.inventory_service.model.Inventory;
import com.example.inventory_service.repository.InventoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryEventPublisher eventPublisher;

    public InventoryService(InventoryRepository inventoryRepository,
                            InventoryEventPublisher eventPublisher) {
        this.inventoryRepository = inventoryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void processOrder(OrderCreatedEvent event) {
        Optional<Inventory> inventoryOpt =inventoryRepository.findByItemNameIgnoreCase(event.getItem());

        if (inventoryOpt.isEmpty()) {
            eventPublisher.publishRejected(new OrderRejectedEvent(
                    event.getOrderId(),
                    event.getUserId(),
                    "Item not found in inventory: " + event.getItem(),
                    LocalDateTime.now()
            ));
            return;
        }

        Inventory inventory = inventoryOpt.get();

        if (inventory.getQuantity() > 0) {
            inventory.setQuantity(inventory.getQuantity() - 1);
            inventory.setUpdatedAt(LocalDateTime.now());
            inventoryRepository.save(inventory);

            eventPublisher.publishConfirmed(new OrderConfirmedEvent(
                    event.getOrderId(),
                    event.getUserId(),
                    LocalDateTime.now()
            ));
        } else {
            eventPublisher.publishRejected(new OrderRejectedEvent(
                    event.getOrderId(),
                    event.getUserId(),
                    "Out of stock: " + event.getItem(),
                    LocalDateTime.now()
            ));
        }
    }

    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    public Optional<Inventory> getById(Integer id) {
        return inventoryRepository.findById(id);
    }

    public Optional<Inventory> getByItemName(String itemName) {
        return inventoryRepository.findByItemNameIgnoreCase(itemName);
    }

    public Inventory addInventory(Inventory inventory) {
        inventory.setUpdatedAt(LocalDateTime.now());
        return inventoryRepository.save(inventory);
    }

    public Inventory updateQuantity(Integer id, Integer quantity) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory item not found: " + id));
        inventory.setQuantity(quantity);
        inventory.setUpdatedAt(LocalDateTime.now());
        return inventoryRepository.save(inventory);
    }
}