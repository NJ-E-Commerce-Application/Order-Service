package FashionFlow.order_service.service;

import FashionFlow.order_service.client.InventoryClient;
import FashionFlow.order_service.dto.OrderRequest;
import FashionFlow.order_service.event.OrderPlacedEvent;
import FashionFlow.order_service.model.Order;
import FashionFlow.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final KafkaTemplate<String,OrderPlacedEvent> kafkaTemplate;
    public void placeOrder(OrderRequest orderRequest) {

        var isProductInStock = inventoryClient.isInStock(orderRequest.skuCode(), orderRequest.quantity());
        if (isProductInStock) {
            Order order = new Order();
            order.setOrderNumber(UUID.randomUUID().toString());
            order.setPrice(orderRequest.price().multiply(BigDecimal.valueOf(orderRequest.quantity())));
            order.setSkuCode(orderRequest.skuCode());
            order.setQuantity(orderRequest.quantity());
            orderRepository.save(order);

            /*
send the msg to kafka topic
            OrderPlacedEvent orderPlacedEvent=new OrderPlacedEvent(order.getOrderNumber(),orderRequest.userDetails().email());
            //we are sending string and the value
            log.info("start of sending the orderplacedevent {} to the kafka topic" ,orderPlacedEvent);
            try{
                kafkaTemplate.send("order_placed",orderPlacedEvent);
            }catch (Exception e){
                System.out.println("Error occured in "+ e);
            }
            log.info("end of sending the orderplacedevent {} to the kafka topic" ,orderPlacedEvent);

*/


        } else {
            throw new RuntimeException("Product with SkuCode " + orderRequest.skuCode() + " is not in stock");
        }
    }
}