package com.ananya.apireplay.replay;

import com.ananya.apireplay.diff.DiffEntry;
import com.ananya.apireplay.diff.DiffType;
import com.ananya.apireplay.diff.JsonDiffEngine;
import com.ananya.apireplay.dto.CallDiff;
import com.ananya.apireplay.dto.ReplayReport;
import com.ananya.apireplay.entity.ReplayResult;
import com.ananya.apireplay.repository.ReplayResultRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReplayReportService {

    private final ReplayResultRepository replayResultRepository;
    private final JsonDiffEngine diffEngine;

    public ReplayReportService(ReplayResultRepository replayResultRepository, JsonDiffEngine diffEngine) {
        this.replayResultRepository = replayResultRepository;
        this.diffEngine = diffEngine;
    }

    public ReplayReport build(String runId) {
        List<ReplayResult> results = replayResultRepository.findByRunId(runId);
        List<CallDiff> changedCalls = new ArrayList<>();
        int breakingCount = 0;

        for (ReplayResult result : results) {
            List<DiffEntry> diffs = new ArrayList<>();

            if (result.getExpectedStatus() != result.getActualStatus()) {
                diffs.add(new DiffEntry("status", DiffType.STATUS_CHANGED,
                        String.valueOf(result.getExpectedStatus()),
                        String.valueOf(result.getActualStatus()), true));
            }
            diffs.addAll(diffEngine.compare(result.getExpectedBody(), result.getActualBody()));

            if (diffs.isEmpty()) {
                continue;
            }

            boolean breaking = diffs.stream().anyMatch(DiffEntry::breaking);
            if (breaking) {
                breakingCount++;
            }
            changedCalls.add(new CallDiff(result.getRecordedCallId(), result.getMethod(),
                    result.getPath(), breaking, diffs));
        }

        int total = results.size();
        int changed = changedCalls.size();
        return new ReplayReport(runId, total, total - changed, changed, breakingCount, changedCalls);
    }
}
