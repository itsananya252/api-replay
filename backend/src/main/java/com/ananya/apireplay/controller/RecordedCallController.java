package com.ananya.apireplay.controller;

import com.ananya.apireplay.entity.RecordedCall;
import com.ananya.apireplay.repository.RecordedCallRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recorded")
public class RecordedCallController {

    private final RecordedCallRepository recordedCallRepository;

    public RecordedCallController(RecordedCallRepository recordedCallRepository) {
        this.recordedCallRepository = recordedCallRepository;
    }

    @GetMapping
    public List<RecordedCall> getAll() {
        return recordedCallRepository.findAll();
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear() {
        recordedCallRepository.deleteAll();
    }
}