package com.eatda.domain.order.service;

import com.eatda.domain.order.dto.OrderRequestDTO;
import com.eatda.domain.order.dto.OrderResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OptimisticLockOrderFacade {

    private final OrderService orderService;

    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO) throws InterruptedException {
        while (true) {
            try {
                return orderService.createOrderWithOptimisticLock(orderRequestDTO);
            } catch (ConcurrencyFailureException e) {
                log.info("동시성 충돌 발생 (낙관적 락 혹은 데드락), 재시도합니다... 예외타입: {}, 메뉴ID: {}", 
                        e.getClass().getSimpleName(),
                        orderRequestDTO.getMenuOrders().get(0).getMenuId());
                Thread.sleep(50); // 재시도 전 약간의 대기 (Backoff)
            }
        }
    }
}
