package com.eatda.domain.order;

import com.eatda.domain.menu.entity.Menu;
import com.eatda.domain.menu.repository.MenuRepository;
import com.eatda.domain.order.dto.OrderRequestDTO;
import com.eatda.domain.order.service.OrderService;
import com.eatda.domain.restaurant.entity.Restaurant;
import com.eatda.domain.restaurant.repository.RestaurantRepository;
import com.eatda.domain.user.child.entity.Child;
import com.eatda.domain.user.child.repository.ChildRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class OrderConcurrencyTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuRepository menuRepository;

    private Long childId;
    private Long restaurantId;
    private Long menuId;

    @BeforeEach
    void setUp() {
        Child child = Child.builder()
                .childName("재현테스트아동")
                .childAmount(100000) // 10만원 상정
                .build();
        childId = childRepository.save(child).getId();

        Restaurant restaurant = Restaurant.builder()
                .restaurantName("재현테스트식당")
                .build();
        restaurantId = restaurantRepository.save(restaurant).getId();

        Menu menu = Menu.builder()
                .menuName("테스트메뉴")
                .price(1000) // 1000원짜리 메뉴
                .restaurant(restaurant)
                .build();
        menuId = menuRepository.save(menu).getId();
    }

    @AfterEach
    void tearDown() {
        // 데이터 정리 로직 (필요시)
        // 실제 DB를 사용한다면 주의 필요
    }

    @Test
    @DisplayName("동시 주문 시 인지하지 못한 레이스 컨디션으로 인한 잔액 정합성 실패 재현")
    void concurrentOrderRaceConditionReproduction() throws InterruptedException {
        int threadCount = 30; // 30명이 동시에 주문
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        OrderRequestDTO request = OrderRequestDTO.builder()
                .childId(childId)
                .restaurantId(restaurantId)
                .menuOrders(List.of(new OrderRequestDTO.MenuOrder(menuId, 1)))
                .totalsum(1000)
                .build();

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    orderService.createOrder(request);
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Child child = childRepository.findById(childId).orElseThrow();

        // 기대하는 결과: 100,000 - (1,000 * 30) = 70,000
        // 실제 결과: Race Condition으로 인해 70,000보다 클 확률이 높음 (Lost Update 발생)
        System.out.println("최종 잔액: " + child.getChildAmount());
        
        // 정합성이 깨졌음을 증명 (현재 코드로는 이 테스트가 실패할 가능성이 높음)
        assertEquals(70000, child.getChildAmount(), "잔액이 정확히 70,000원이어야 합니다.");
    }
}
