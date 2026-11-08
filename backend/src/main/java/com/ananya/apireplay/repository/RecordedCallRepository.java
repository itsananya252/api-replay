package com.ananya.apireplay.repository;

import com.ananya.apireplay.entity.RecordedCall;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordedCallRepository extends JpaRepository<RecordedCall, Long> {
}