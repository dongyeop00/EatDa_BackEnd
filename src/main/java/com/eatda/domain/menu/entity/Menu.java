package com.eatda.domain.menu.entity;

import com.eatda.domain.restaurant.entity.Restaurant;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Menu {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String menuName;
    private String menuBody;
    private Boolean menuStatus;
    private int price;
    private int stock;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    // ===== 도메인 비즈니스 로직 =====

    /**
     * 메뉴 정보를 수정합니다.
     */
    public void updateInfo(String name, String body, Boolean status, Integer price) {
        if (name != null && !name.isBlank()) {
            this.menuName = name;
        }
        if (body != null) {
            this.menuBody = body;
        }
        if (status != null) {
            this.menuStatus = status;
        }
        if (price != null && price >= 0) {
            this.price = price;
        }
    }

    /**
     * 메뉴 가격을 변경합니다.
     */
    public void changePrice(int newPrice) {
        if (newPrice < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        }
        this.price = newPrice;
    }

    /**
     * 메뉴 판매를 시작합니다.
     */
    public void activate() {
        this.menuStatus = true;
    }

    /**
     * 메뉴 판매를 중지합니다.
     */
    public void deactivate() {
        this.menuStatus = false;
    }

    /**
     * 메뉴 판매 상태를 토글합니다.
     */
    public void toggleStatus() {
        this.menuStatus = !Boolean.TRUE.equals(this.menuStatus);
    }

    /**
     * 메뉴 재고를 차감합니다.
     */
    public void decreaseStock(int quantity) {
        if (this.stock < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }
        this.stock -= quantity;
    }

    /**
     * 메뉴가 판매 중인지 확인합니다.
     */
    public boolean isAvailable() {
        return Boolean.TRUE.equals(this.menuStatus);
    }
}

