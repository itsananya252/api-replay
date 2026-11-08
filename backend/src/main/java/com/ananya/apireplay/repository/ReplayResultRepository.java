package com.ananya.apireplay.repository;

import com.ananya.apireplay.entity.ReplayResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReplayResultRepository extends JpaRepository<ReplayResult, Long> {

    List<ReplayResult> findByRunId(String runId);
}