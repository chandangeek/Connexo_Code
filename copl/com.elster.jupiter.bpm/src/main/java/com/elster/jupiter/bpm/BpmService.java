package com.elster.jupiter.bpm;

import java.util.List;
import java.util.Map;

public interface BpmService {
    String COMPONENTNAME = "BPM";
    String BPM_QUEUE_DEST = "BpmQueueDest";
    String BPM_QUEUE_SUBSC = "BpmQueueSubsc";

    List<String> getProcesses();

    Map<String, Object> getProcessParameters(String processId);

    boolean startProcess(String deploymentId, String processId, Map<String, Object> parameters);
}
