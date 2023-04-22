package com.shepherdmoney.interviewproject.repository;

import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Crud repository to store credit cards
 */
@Repository("CreditCardRepo")
public interface CreditCardRepository extends JpaRepository<CreditCard, Integer> {
    @Query(value = "select * from Credit_Card where MY_USER_ID = :userId", nativeQuery=true)
    List<CreditCard> findByMyUserId(Integer userId);

    @Query(value = "select * from Credit_Card where NUMBER = :number", nativeQuery=true)
    Optional<CreditCard> findByCreditCardNumber(String number);
}
