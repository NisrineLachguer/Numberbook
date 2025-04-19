package com.lachguer.mobile.controller;

import ch.qos.logback.classic.Logger;
import com.lachguer.mobile.model.User;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.lachguer.mobile.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    private static final Logger logger = (Logger) LoggerFactory.getLogger(UserController.class);

    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody User user) {
        logger.info("Creating/updating user with IMEI: {}", user.getImei());

        User existingUser = userRepository.findByImei(user.getImei());

        if (existingUser != null) {
            existingUser.setNumber(user.getNumber());
            userRepository.save(existingUser);

            logger.info("User updated with IMEI: {}", user.getImei());
            Map<String, Object> response = new HashMap<>();
            response.put("id", existingUser.getId());
            response.put("imei", existingUser.getImei());
            return ResponseEntity.ok(response);
        }

        User savedUser = userRepository.save(user);
        logger.info("User created with ID: {}", savedUser.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("id", savedUser.getId());
        response.put("imei", savedUser.getImei());
        return ResponseEntity.ok(response);
    }
}