package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.exception.InvalidUserIdException;
import com.shepherdmoney.interviewproject.exception.UserEmptyEmailException;
import com.shepherdmoney.interviewproject.exception.UserEmptyNameException;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.CreateUserPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @PutMapping("/user")
    public ResponseEntity<Integer> createUser(@RequestBody CreateUserPayload payload) {

        try {
            //validate the information given in the payload
            if (payload.getName().isEmpty()) {
                throw new UserEmptyNameException("user name is empty");
            }
            if (payload.getEmail().isEmpty()) {
                throw new UserEmptyEmailException("user email is empty");
            }
            //create an user entity with information given in the payload, store it in the database
            User user = new User();
            user.setName(payload.getName());
            user.setEmail(payload.getEmail());
            userRepository.save(user);
            //return 200 OK response
            return ResponseEntity.ok(200);
        } catch (UserEmptyNameException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(400);
        } catch (UserEmptyEmailException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(400);
        }
    }

    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(@RequestParam int userId) {

        try {
            //validate the information given in the requestparam
            if (!userRepository.existsById(userId)) {
                throw new InvalidUserIdException("user id does not exist");
            }
            //Return 200 OK if a user with the given ID exists, and the deletion is successful
            userRepository.deleteById(userId);
            return ResponseEntity.ok("Deleted user with userId:" + userId);
        } catch (InvalidUserIdException e) {
            //Return 400 Bad Request if a user with the ID does not exist
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
