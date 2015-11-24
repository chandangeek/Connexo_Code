package com.elster.jupiter.bpm;

import com.elster.jupiter.bpm.impl.BpmProcessDefinitionImpl;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BpmService {
    String COMPONENTNAME = "BPM";
    String BPM_QUEUE_DEST = "BpmQueueDest";
    String BPM_QUEUE_SUBSC = "BpmQueueSubsc";
    String BPM_QUEUE_DISPLAYNAME = "Connexo Flow handler";

    List<String> getProcesses();

    Map<String, Object> getProcessParameters(String processId);

    boolean startProcess(String deploymentId, String processId, Map<String, Object> parameters);

    BpmServer getBpmServer();

    BpmProcessDefinition findOrCreateBpmProcessDefinition(String processName, String association, String version, String status);

    Query<BpmProcessDefinition> getQueryBpmProcessDefinition();

    List<BpmProcessDefinition> getBpmProcessDefinitions();

    BpmProcessPrivilege createBpmProcessPrivilege(BpmProcessDefinition bpmProcessDefinition, String privilegeName, String application);

    BpmProcessDeviceState createBpmProcessDeviceState(BpmProcessDefinition bpmProcessDefinition, long deviceStateId, long deviceLifeCycleId, String name, String deviceName);

    public Optional<BpmProcessDefinition> getBpmProcessDefinition(String processName, String version);

    List<BpmProcessPrivilege> getBpmProcessPrivileges(long processId);

    QueryService getQueryService();
}
