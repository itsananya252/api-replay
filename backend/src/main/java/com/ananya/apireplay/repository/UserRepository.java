package com.ananya.apireplay.repository;

import com.ananya.apireplay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}