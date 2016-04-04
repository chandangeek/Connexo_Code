package com.elster.jupiter.bpm;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ProviderType
public interface BpmService {
    String COMPONENTNAME = "BPM";
    String BPM_QUEUE_DEST = "BpmQueueDest";
    String BPM_QUEUE_SUBSC = "BpmQueueSubsc";
    String BPM_QUEUE_DISPLAYNAME = "Handle Connexo Flow";

    @Deprecated
    List<String> getProcesses();

    @Deprecated
    Map<String, Object> getProcessParameters(String processId);

    boolean startProcess(String deploymentId, String processId, Map<String, Object> parameters);

    boolean startProcess(String deploymentId, String processId, Map<String, Object> parameters, String auth);

    BpmServer getBpmServer();

    @Deprecated
    BpmProcessDeviceState createBpmProcessDeviceState(BpmProcessDefinition bpmProcessDefinition, long deviceStateId, long deviceLifeCycleId, String name, String deviceName);

    @Deprecated
    BpmProcessDefinition findOrCreateBpmProcessDefinition(String processName, String association, String version, String status);

    List<BpmProcessDefinition> getBpmProcessDefinitions();

    List<BpmProcessDefinition> getAllBpmProcessDefinitions();

    List<BpmProcessDefinition> getActiveBpmProcessDefinitions();

    BpmProcessPrivilege createBpmProcessPrivilege(String privilegeName, String application);

    @Deprecated
    BpmProcessPrivilege createBpmProcessPrivilege(BpmProcessDefinition bpmProcessDefinition, String privilegeName, String application);

    Optional<BpmProcessDefinition> getBpmProcessDefinition(String processName, String version);

    List<BpmProcessPrivilege> getBpmProcessPrivileges(long processId);

    QueryService getQueryService();

    Query<BpmProcessDefinition> getQueryBpmProcessDefinition();

    BpmProcessDefinitionBuilder newProcessBuilder();

    List<ProcessAssociationProvider> getProcessAssociationProviders();

    Optional<ProcessAssociationProvider> getProcessAssociationProvider(String type);

    ProcessInstanceInfos getRunningProcesses(String authorization, String filter);
}
