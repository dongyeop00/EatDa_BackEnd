package com.eatda.domain.menu.repository;

import com.eatda.domain.menu.entity.Menu;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByRestaurantId(Long restaurantId);

    @Query("SELECT m FROM Menu m JOIN m.restaurant r WHERE r.president.id = :presidentId")
    List<Menu> findByPresidentId(@Param("presidentId") Long presidentId);

    // 비관적 락 조회 메서드 추가
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Menu m where m.id = :id")
    Optional<Menu> findByIdWithPessimisticLock(Long id);
}
