package com.example.FFsmartBackend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.FFsmartBackend.models.Delivery;
import com.example.FFsmartBackend.models.Order;
import com.example.FFsmartBackend.services.DeliveryService;
import com.example.FFsmartBackend.services.OrderService;

@SpringBootTest
class FFsmartBackendApplicationTests {

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private OrderService orderService;

    @Test
    void contextLoads() {
    }

    //1
    @Test
    void loadDeliveriesFromFile_WhenFileExists() throws IOException {
        String deliveriesFilePath = "src/main/resources/data/deliveries.json";
        assertFalse(Files.notExists(Paths.get(deliveriesFilePath)));
        deliveryService.loadDeliveriesFromFile();
        List<Delivery> deliveries = deliveryService.getAllDeliveries();
        assertFalse(deliveries.isEmpty(), "Deliveries list is empty");
        assertEquals("Tomatoes", deliveries.get(0).getItemName());
    }

    //4
    @Test
    void getAllDeliveries() {
        Delivery delivery = new Delivery();
        delivery.setItemName("Test Item");
        deliveryService.createDelivery(delivery);
        List<Delivery> deliveries = deliveryService.getAllDeliveries();
        assertFalse(deliveries.isEmpty());
    }

    //5
    @Test
    void findDeliveryById_WhenExists() {
        Delivery delivery = new Delivery();
        delivery.setId("1");
        deliveryService.createDelivery(delivery);
        Optional<Delivery> found = deliveryService.findById("1");
        assertTrue(found.isPresent());
    }

    //6
    @Test
    void findDeliveryById_WhenDoesNotExist() {
        Optional<Delivery> found = deliveryService.findById("99876");
        assertTrue(found.isEmpty());
    }

    //7
    @Test
    void createDeliveryWithId() {
        Delivery delivery = new Delivery();
        delivery.setId("100");
        Delivery created = deliveryService.createDelivery(delivery);
        assertEquals("100", created.getId());
    }

    //8
    @Test
    void createDeliveryWithoutId() {
        Delivery delivery = new Delivery();
        Delivery created = deliveryService.createDelivery(delivery);
        assertNotNull(created.getId());
    }

    //9
    @Test
    void approveDeliveryWhenExists() {
        Delivery delivery = new Delivery();
        delivery.setId("1");
        deliveryService.createDelivery(delivery);
        boolean result = deliveryService.approveDelivery("1");
        assertTrue(result);
        assertEquals("Approved", deliveryService.findById("1").get().getApprovalStatus());
    }

    //10
    @Test
    void declineDeliveryWhenExists() {
        Delivery delivery = new Delivery();
        delivery.setId("2");
        deliveryService.createDelivery(delivery);
        boolean result = deliveryService.declineDelivery("2");
        assertTrue(result);
        assertEquals("Declined", deliveryService.findById("2").get().getApprovalStatus());
    }

    //11
    @Test
    void loadOrdersFromFile_WhenFileExists() throws IOException {
        String ordersFilePath = "src/main/resources/data/orders.json";
        assertFalse(Files.notExists(Paths.get(ordersFilePath)));
        orderService.loadOrdersFromFile();
        List<Order> orders = orderService.getAllOrders();
        assertFalse(orders.isEmpty());
    }

    //13
    @Test
    void getAllOrders() {
        Order order = new Order();
        orderService.saveOrder(order);
        List<Order> orders = orderService.getAllOrders();
        assertFalse(orders.isEmpty());
    }

    //14
    @Test
    void findOrderById_WhenExists() {
        Order order = new Order();
        order.setId("1");
        orderService.saveOrder(order);
        Optional<Order> found = orderService.findById("1");
        assertTrue(found.isPresent());
    }

    //15
    @Test
    void createOrderWithoutId() {
        Order order = new Order();
        Order saved = orderService.saveOrder(order);
        assertNotNull(saved.getId());
    }
}
