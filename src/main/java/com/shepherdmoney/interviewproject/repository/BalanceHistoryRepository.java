package com.shepherdmoney.interviewproject.repository;

import com.shepherdmoney.interviewproject.model.BalanceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BalanceHistoryRepository extends JpaRepository<BalanceHistory, Integer> {
    @Query(value = "select * from Balance_History where CREDIT_CARD_ID = :cardId order by DATE DESC", nativeQuery=true)
    List<BalanceHistory> findByCreditCardId(Integer cardId);
}
