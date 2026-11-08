package com.ananya.apireplay.replay;

import com.ananya.apireplay.dto.ReplayRunResponse;
import com.ananya.apireplay.entity.RecordedCall;
import com.ananya.apireplay.entity.ReplayResult;
import com.ananya.apireplay.repository.RecordedCallRepository;
import com.ananya.apireplay.repository.ReplayResultRepository;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ReplayService {

    private final RecordedCallRepository recordedCallRepository;
    private final ReplayResultRepository replayResultRepository;

    public ReplayService(RecordedCallRepository recordedCallRepository,
                         ReplayResultRepository replayResultRepository) {
        this.recordedCallRepository = recordedCallRepository;
        this.replayResultRepository = replayResultRepository;
    }

    public ReplayRunResponse run(String target) {
        String runId = UUID.randomUUID().toString();
        RestClient client = RestClient.builder().baseUrl(target).build();

        List<RecordedCall> calls = recordedCallRepository.findAll().stream()
                .filter(c -> c.getMethod().equals("GET"))
                .toList();

        int same = 0;
        int changed = 0;

        for (RecordedCall call : calls) {
            ReplayResult result = replayOne(client, call, runId);
            replayResultRepository.save(result);

            boolean isSame = result.getActualStatus() == result.getExpectedStatus()
                    && Objects.equals(result.getActualBody(), result.getExpectedBody());
            if (isSame) {
                same++;
            } else {
                changed++;
            }
        }
        return new ReplayRunResponse(runId, target, calls.size(), same, changed);
    }

    private ReplayResult replayOne(RestClient client, RecordedCall call, String runId) {
        ReplayResult result = new ReplayResult();
        result.setRunId(runId);
        result.setRecordedCallId(call.getId());
        result.setMethod(call.getMethod());
        result.setPath(call.getPath());
        result.setExpectedStatus(call.getStatus());
        result.setExpectedBody(call.getResponseBody());
        result.setReplayedAt(Instant.now());

        String uri = call.getPath();
        if (call.getQueryString() != null) {
            uri = uri + "?" + call.getQueryString();
        }

        long start = System.currentTimeMillis();
        try {
            ResponseEntity<String> response = client.method(HttpMethod.GET)
                    .uri(uri)
                    .header("X-Replay-Call", "true")
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> { })
                    .toEntity(String.class);

            result.setActualStatus(response.getStatusCode().value());
            result.setActualBody(response.getBody());
        } catch (Exception e) {
            result.setActualStatus(-1);
            result.setActualBody("REPLAY ERROR: " + e.getMessage());
        }
        result.setLatencyMs(System.currentTimeMillis() - start);
        return result;
    }
}