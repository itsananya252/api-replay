package com.ananya.apireplay.controller;

import com.ananya.apireplay.dto.ReplayReport;
import com.ananya.apireplay.dto.ReplayRunResponse;
import com.ananya.apireplay.dto.RunSummary;
import com.ananya.apireplay.entity.ReplayResult;
import com.ananya.apireplay.replay.ReplayReportService;
import com.ananya.apireplay.replay.ReplayService;
import com.ananya.apireplay.repository.ReplayResultRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/replay")
public class ReplayController {

    private final ReplayService replayService;
    private final ReplayReportService replayReportService;
    private final ReplayResultRepository replayResultRepository;

    public ReplayController(ReplayService replayService,
                            ReplayReportService replayReportService,
                            ReplayResultRepository replayResultRepository) {
        this.replayService = replayService;
        this.replayReportService = replayReportService;
        this.replayResultRepository = replayResultRepository;
    }

    @GetMapping
    public List<RunSummary> runs() {
        return replayResultRepository.findAll().stream()
                .collect(Collectors.groupingBy(ReplayResult::getRunId))
                .entrySet().stream()
                .map(e -> new RunSummary(
                        e.getKey(),
                        e.getValue().stream()
                                .map(ReplayResult::getReplayedAt)
                                .min(Comparator.naturalOrder())
                                .orElse(Instant.EPOCH),
                        e.getValue().size()))
                .sorted(Comparator.comparing(RunSummary::replayedAt).reversed())
                .toList();
    }

    @PostMapping
    public ReplayRunResponse start(@RequestParam(defaultValue = "http://localhost:8080") String target) {
        return replayService.run(target);
    }

    @GetMapping("/{runId}")
    public List<ReplayResult> results(@PathVariable String runId) {
        return replayResultRepository.findByRunId(runId);
    }

    @GetMapping("/{runId}/report")
    public ReplayReport report(@PathVariable String runId) {
        return replayReportService.build(runId);
    }
}