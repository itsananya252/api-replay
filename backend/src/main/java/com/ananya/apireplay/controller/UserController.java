package com.ananya.apireplay.controller;

import com.ananya.apireplay.dto.UserRequest;
import com.ananya.apireplay.dto.UserResponse;
import com.ananya.apireplay.entity.User;
import com.ananya.apireplay.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toResponse(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserRequest request) {
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        return toResponse(userRepository.save(user));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}