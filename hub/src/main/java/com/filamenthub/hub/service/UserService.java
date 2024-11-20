package com.filamenthub.hub.service;

import org.springframework.stereotype.Service;

import com.filamenthub.hub.model.User;
import com.filamenthub.hub.repositories.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUser(String username) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername(username);
            return userRepository.save(newUser);
        });
    }
}
