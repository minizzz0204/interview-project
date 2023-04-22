package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.exception.InvalidCreditCardNumber;
import com.shepherdmoney.interviewproject.exception.InvalidUserIdException;
import com.shepherdmoney.interviewproject.model.BalanceHistory;
import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.BalanceHistoryRepository;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RequiredArgsConstructor
@RestController
public class CreditCardController {

    public final CreditCardRepository creditCardRepository;
    public final UserRepository userRepository;
    public final BalanceHistoryRepository balanceHistoryRepository;

    @PostMapping("/credit-card")
    public ResponseEntity<Integer> addCreditCardToUser(@RequestBody AddCreditCardToUserPayload payload) {

        try {
            if (!userRepository.existsById(payload.getUserId())) {
                throw new InvalidUserIdException("user id does not exist");
            }
            //Create a credit card entity, and then associate that credit card with user with given userId
            Optional<User> optionalStudent = userRepository.findById(payload.getUserId());
            User user = optionalStudent.get();
            CreditCard creditCard = new CreditCard();
            creditCard.setIssuanceBank(payload.getCardIssuanceBank());
            creditCard.setNumber(payload.getCardNumber());
            creditCard.setUser(user);
            creditCardRepository.save(creditCard);
            return ResponseEntity.ok(200);
        } catch (InvalidUserIdException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(400);
        }
    }

    @GetMapping("/credit-card:all")
    public ResponseEntity<List<CreditCardView>> getAllCardOfUser(@RequestParam int userId) {

        try {
            if (!userRepository.existsById(userId)) {
                throw new InvalidUserIdException("user id does not exist");
            }
            List<CreditCard> creditCardList = creditCardRepository.findByMyUserId(userId);
            List<CreditCardView> creditCardViewList = new ArrayList<>();
            for (CreditCard card : creditCardList) {
                creditCardViewList.add(new CreditCardView(card.getIssuanceBank(), card.getNumber()));
            }
            return ResponseEntity.ok(creditCardViewList);
        } catch (InvalidUserIdException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ArrayList<>());
        }
    }

    @GetMapping("/credit-card:user-id")
    public ResponseEntity<Integer> getUserIdForCreditCard(@RequestParam String creditCardNumber) {

        try {
            Optional<CreditCard> optionalCreditCard = creditCardRepository.findByCreditCardNumber(creditCardNumber);
            if (optionalCreditCard.isEmpty()) {
                throw new InvalidCreditCardNumber("credit card number does not exist");
            }
            CreditCard creditCard = optionalCreditCard.get();
            return ResponseEntity.ok(creditCard.getUser().getId());
        } catch(InvalidCreditCardNumber e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(400);
        }
    }

    @PostMapping("/credit-card:update-balance")
    public ResponseEntity<Integer> updateCreditCardBalanceHistory(@RequestBody UpdateBalancePayload[] payload) {
        //TODO: Given a list of transactions, update credit cards' balance history.
        //      For example: if today is 4/12, a credit card's balanceHistory is [{date: 4/12, balance: 110}, {date: 4/10, balance: 100}],
        //      Given a transaction of {date: 4/10, amount: 10}, the new balanceHistory is
        //      [{date: 4/12, balance: 120}, {date: 4/11, balance: 110}, {date: 4/10, balance: 110}]
        //      Return 200 OK if update is done and successful, 400 Bad Request if the given card number
        //        is not associated with a card.
        try {
            //obtain all credit card number and map it to the credit card entity
            Set<String> cardNumbers = new HashSet<>();
            for (UpdateBalancePayload pay : payload) {
                cardNumbers.add(pay.getCreditCardNumber());
            }
            Map<String, CreditCard> creditCards = new HashMap<>();
            for (String number : cardNumbers) {
                creditCards.put(number, creditCardRepository.findByCreditCardNumber(number).get());
            }
            if (creditCards == null) {
                throw new InvalidCreditCardNumber("there is invalid credit card number");
            }

            // iterate every UpdateBalancePayload in payload array. update balance history for each card
            for (int p = 0; p < payload.length; p++) {
                UpdateBalancePayload pay = payload[p];
                CreditCard creditCard = creditCards.get(pay.getCreditCardNumber());
                if (creditCard == null) {
                    throw new InvalidCreditCardNumber("there is invalid credit card number");
                }

                Instant transactionTime = pay.getTransactionTime();
                List<BalanceHistory> creditBalanceHistories = balanceHistoryRepository.findByCreditCardId(creditCard.getId());
                //Find if there is a BalanceHistory with the same date as the transactionTime.
                Optional<BalanceHistory> balanceHistoryOptional = creditBalanceHistories.stream()
                        .filter(entry -> entry.getDate().equals(transactionTime))
                        .findFirst();

                if (balanceHistoryOptional.isPresent()) {
                    //if there is, add the amount of the transactionAmount to the balance of that BalanceHistory.
                    BalanceHistory balanceHistory = balanceHistoryOptional.get();
                    balanceHistory.setBalance(balanceHistory.getBalance() + pay.getTransactionAmount());
                    int index = creditBalanceHistories.indexOf(balanceHistory) - 1;
                    //update all of other's balance history which date is after the transactionTime
                    for (int i = index; i >= 0; i--) {
                        BalanceHistory balanceHistory1 = creditBalanceHistories.get(i);
                        balanceHistory1.setBalance(balanceHistory1.getBalance() + pay.getTransactionAmount());
                    }
                } else {
                    //If there isn't one, create a new balanceHistory and add it to the balance history.
                    if (creditBalanceHistories.isEmpty()) {
                        BalanceHistory balanceHistory = new BalanceHistory();
                        balanceHistory.setDate(transactionTime);
                        balanceHistory.setBalance(pay.getTransactionAmount());
                        balanceHistory.setCreditCard(creditCard);
                        balanceHistoryRepository.save(balanceHistory);
                        creditBalanceHistories.add(balanceHistory);
                    } else {
                        boolean found = false;
                        for (int j = 0; j < creditBalanceHistories.size(); j++) {
                            BalanceHistory cur = creditBalanceHistories.get(j);
                            if (cur.getDate().isBefore(transactionTime)) {
                                double balance = cur.getBalance();
                                BalanceHistory balanceHistory = new BalanceHistory();
                                balanceHistory.setDate(transactionTime);
                                balanceHistory.setBalance(balance + pay.getTransactionAmount());
                                balanceHistory.setCreditCard(creditCard);
                                balanceHistoryRepository.save(balanceHistory);
                                creditBalanceHistories.add(creditBalanceHistories.size(), balanceHistory);
                                for (int i = 0; i < creditBalanceHistories.size(); i++) {
                                    if (creditBalanceHistories.get(i).getDate().isAfter(transactionTime)) {
                                        BalanceHistory balanceHistory1 = creditBalanceHistories.get(i);
                                        balanceHistory1.setBalance(balanceHistory1.getBalance() + pay.getTransactionAmount());
                                        balanceHistoryRepository.save(balanceHistory1);
                                    }
                                }
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            BalanceHistory balanceHistory = new BalanceHistory();
                            balanceHistory.setDate(transactionTime);
                            balanceHistory.setBalance(pay.getTransactionAmount());
                            balanceHistory.setCreditCard(creditCard);
                            balanceHistoryRepository.save(balanceHistory);
                            creditBalanceHistories.add(creditBalanceHistories.size(), balanceHistory);
                            for (int i = 0; i < creditBalanceHistories.size(); i++) {
                                if (creditBalanceHistories.get(i).getDate().isAfter(transactionTime)) {
                                    BalanceHistory balanceHistory1 = creditBalanceHistories.get(i);
                                    balanceHistory1.setBalance(balanceHistory1.getBalance() + pay.getTransactionAmount());
                                    balanceHistoryRepository.save(balanceHistory1);
                                }
                            }
                        }

                    }
                }
                //deal with today
                if (p == payload.length - 1) {
                    Instant today = Instant.now();
                    BalanceHistory latestHistory = creditBalanceHistories.isEmpty() ? null : creditBalanceHistories.get(0);
                    if (latestHistory != null && latestHistory.getDate().isBefore(today)) {
                        BalanceHistory balanceHistory = new BalanceHistory();
                        balanceHistory.setCreditCard(creditCard);
                        balanceHistory.setBalance(latestHistory.getBalance());
                        balanceHistory.setDate(today);
                        balanceHistoryRepository.save(balanceHistory);
                        creditBalanceHistories.add(0, balanceHistory);
                    }
                }
                creditCard.setBalanceHistories(creditBalanceHistories);
                creditCardRepository.save(creditCard);
            }
            return ResponseEntity.ok().build();
        } catch(InvalidCreditCardNumber e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(400);
        }

    }
    
}
