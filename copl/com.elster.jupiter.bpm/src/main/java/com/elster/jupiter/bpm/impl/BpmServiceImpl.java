/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmProcess;
import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmProcessDefinitionBuilder;
import com.elster.jupiter.bpm.BpmProcessDeviceState;
import com.elster.jupiter.bpm.BpmProcessPrivilege;
import com.elster.jupiter.bpm.BpmServer;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.ProcessAssociationProvider;
import com.elster.jupiter.bpm.ProcessInstanceInfo;
import com.elster.jupiter.bpm.ProcessInstanceInfos;
import com.elster.jupiter.bpm.security.Privileges;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.upgrade.InstallIdentifier.identifier;

@Component(
        name = "com.elster.jupiter.bpm",
        service = {BpmService.class, TranslationKeyProvider.class, MessageSeedProvider.class},
        property = {"name=" + BpmService.COMPONENTNAME}, immediate = true)
public final class BpmServiceImpl implements BpmService, TranslationKeyProvider, MessageSeedProvider {

    static String BPM_QUEUE_DEST = "BpmQueueDest";
    static String BPM_QUEUE_SUBSC = "BpmQueueSubsc";
    static String BPM_QUEUE_DISPLAYNAME = "Handle Connexo Flow";

    private volatile DataModel dataModel;
    private volatile MessageService messageService;
    private volatile JsonService jsonService;
    private volatile Thesaurus thesaurus;
    private volatile UserService userService;
    private volatile BpmServerImpl bpmServer;
    private volatile QueryService queryService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile UpgradeService upgradeService;
    private List<ProcessAssociationProvider> processAssociationProviders = new CopyOnWriteArrayList<>();
    private Map<String, String> singletonInstanceProcesses = new ConcurrentHashMap();

    private final static String TOKEN_AUTH = "com.elster.jupiter.token";

    public BpmServiceImpl() {
    }

    @Inject
    BpmServiceImpl(OrmService ormService, MessageService messageService, JsonService jsonService, NlsService nlsService, UserService userService, QueryService queryService, ThreadPrincipalService threadPrincipalService, UpgradeService upgradeService) {
        this();
        setOrmService(ormService);
        setMessageService(messageService);
        setJsonService(jsonService);
        setUserService(userService);
        setNlsService(nlsService);
        setQueryService(queryService);
        setThreadPrincipalService(threadPrincipalService);
        setUpgradeService(upgradeService);
        activate(null);
    }

    @Activate
    public void activate(BundleContext context) {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MessageService.class).toInstance(messageService);
                bind(JsonService.class).toInstance(jsonService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(DataModel.class).toInstance(dataModel);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(UserService.class).toInstance(userService);
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
                bind(BpmService.class).toInstance(BpmServiceImpl.this);
            }
        });
        bpmServer = new BpmServerImpl(context, threadPrincipalService, context != null ? "Bearer " + context.getProperty(TOKEN_AUTH) : null);
        upgradeService.register(
                identifier("Pulse", COMPONENTNAME),
                dataModel,
                InstallerImpl.class,
                ImmutableMap.of(
                        version(10, 2), UpgraderV10_2.class
                ));
    }

    @Deactivate
    public void deactivate() {
        bpmServer = null;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENTNAME, "BPM");
        for (TableSpecs each : TableSpecs.values()) {
            each.addTo(dataModel);
        }
    }

    @Reference(name = "ZProcesses", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addProcessAssociation(ProcessAssociationProvider provider) {
        processAssociationProviders.add(provider);
    }

    @SuppressWarnings("unused")
    public void removeProcessAssociation(ProcessAssociationProvider provider) {
        processAssociationProviders.remove(provider);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(BpmService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Override
    public List<String> getProcesses() {
        return this.getBpmProcessDefinitions()
                .stream()
                .map(BpmProcessDefinition::getProcessName)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getProcessParameters(String processId) {
        Optional<BpmProcessDefinition> foundProcess = this.getBpmProcessDefinitions()
                .stream()
                .filter(p -> processId.equals(p.getProcessName()))
                .findFirst();
        if (foundProcess.isPresent()) {
            return foundProcess.get().getProperties();
        }
        return null;
    }

    @Override
    public boolean startProcess(String deploymentId, String process, Map<String, Object> parameters) {
        verifyProcessIsAlreadyRunning(deploymentId, process, parameters, null);
        return runProcess(deploymentId, process, parameters);
    }

    private boolean runProcess(String deploymentId, String process, Map<String, Object> parameters) {
        boolean result = false;
        Optional<DestinationSpec> found = messageService.getDestinationSpec(BPM_QUEUE_DEST);
        if (found.isPresent()) {
            String json = jsonService.serialize(new BpmProcess(deploymentId, process, parameters));
            found.get().message(json).send();
            result = true;
        }
        return result;
    }

    @Override
    public boolean startProcess(BpmProcessDefinition bpmProcessDefinition, Map<String, Object> parameters) {
        String jsonContent;
        JSONArray arr = null;
        String errorInvalidMessage = thesaurus.getString("error.flow.invalid.response", "Invalid response received, please check your Flow version.");
        String errorNotFoundMessage = thesaurus.getString("error.flow.unavailable", "Connexo Flow is not available.");
        try {
            jsonContent = this.getBpmServer().doGet("/services/rest/server/queries/processes/definitions?page=0&pageSize=10");
            if (!"".equals(jsonContent)) {
                JSONObject jsnobject = new JSONObject(jsonContent);
                arr = jsnobject.getJSONArray("processes");
            }
        } catch (JSONException | RuntimeException ignored) {
        }
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                try {
                    JSONObject task = arr.getJSONObject(i);
                    String processName = task.getString("name");
                    String version = task.getString("version");
                    if (processName.equals(bpmProcessDefinition.getProcessName()) && version.equals(bpmProcessDefinition.getVersion())) {
                        checkProcessIsAlreadyRunning(processName, version, parameters, null);
                        return runProcess(task.getString("deploymentId"), task.getString("id"), parameters);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return false;
    }

    @Override
    public boolean startProcess(String deploymentId, String process, Map<String, Object> parameters, String auth) {
        verifyProcessIsAlreadyRunning(deploymentId, process, parameters, auth);
        boolean result = false;
        Optional<DestinationSpec> found = messageService.getDestinationSpec(BPM_QUEUE_DEST);
        if (found.isPresent()) {
            String json = jsonService.serialize(new BpmProcess(deploymentId, process, parameters, auth));
            found.get().message(json).send();
            result = true;
        }
        return result;
    }

    private void verifyProcessIsAlreadyRunning(String deploymentId, String id, Map<String, Object> parameters, String auth) {
        String jsonContent;
        JSONArray arr = null;
        try {
            jsonContent = this.getBpmServer().doGet("/services/rest/server/queries/processes/definitions?page=0&pageSize=10", auth);
            if (!"".equals(jsonContent)) {
                JSONObject jsonbject = new JSONObject(jsonContent);
                arr = jsonbject.getJSONArray("processes");
            }
        } catch (JSONException | RuntimeException ignored) {
        }
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                try {
                    JSONObject task = arr.getJSONObject(i);
                    if (task.getString("deploymentId").equals(deploymentId) && task.getString("id").equals(id)) {
                        checkProcessIsAlreadyRunning(task.getString("name"), task.getString("version"), parameters, auth);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void checkProcessIsAlreadyRunning(String processName, String version, Map<String, Object> parameters, String auth) {
        String businessObjectId = getSingletonInstanceProcesses().get(processName);
        if (businessObjectId != null) {
            Optional<Object> businessObjectValue = Optional.ofNullable(parameters.get(businessObjectId));
            businessObjectValue.ifPresent(value -> {
                String filter = "?variableid=" + businessObjectId + "&variablevalue=" + value;
                boolean processIsRunning = getRunningProcesses(auth, filter)
                        .processes
                        .stream()
                        .anyMatch(p -> p.name.equals(processName) && p.status.equals(ACTIVE_STATUS));
                if (processIsRunning) {
                    throw new ProcessIsAlreadyRunning(thesaurus, processName, version);
                }
            });
        }
    }

    @Override
    public BpmServer getBpmServer() {
        return bpmServer;
    }

    @Override
    public BpmProcessDeviceState createBpmProcessDeviceState(BpmProcessDefinition bpmProcessDefinition, long deviceStateId, long deviceLifeCycleId, String name, String deviceName) {
        return BpmProcessDeviceStateImpl.from(dataModel, bpmProcessDefinition, deviceStateId, deviceLifeCycleId, name, deviceName);
    }

    @Override
    public BpmProcessDefinition findOrCreateBpmProcessDefinition(String processName, String association, String version, String status) {
        Condition nameCondition = Operator.EQUALIGNORECASE.compare("processName", processName);
        Condition versionCondition = Operator.EQUALIGNORECASE.compare("version", version);
        List<BpmProcessDefinition> bpmProcessDefinitions = dataModel.query(BpmProcessDefinition.class)
                .select(nameCondition.and(versionCondition));
        if (bpmProcessDefinitions.isEmpty()) {
            return BpmProcessDefinitionImpl.from(dataModel, processName, association, version, status, "MDC", Collections.emptyList());
        }
        bpmProcessDefinitions.get(0).setStatus(status);
        return bpmProcessDefinitions.get(0);
    }

    @Override
    public List<BpmProcessDefinition> getBpmProcessDefinitions() {
        return dataModel.query(BpmProcessDefinition.class).select(Operator.NOTEQUAL.compare("status", "UNDEPLOYED"));
    }

    @Override
    public Optional<BpmProcessDefinition> findBpmProcessDefinition(long id) {
        return dataModel.mapper(BpmProcessDefinition.class).getOptional(id);
    }

    @Override
    public List<BpmProcessDefinition> getAllBpmProcessDefinitions() {
        return dataModel.mapper(BpmProcessDefinition.class).find();
    }

    @Override
    public List<BpmProcessDefinition> getActiveBpmProcessDefinitions() {
        return dataModel.query(BpmProcessDefinition.class).select(Operator.EQUALIGNORECASE.compare("status", "ACTIVE"));
    }

    @Override
    public List<BpmProcessDefinition> getActiveBpmProcessDefinitions(String appKey) {
        Condition statusCondition = Operator.EQUALIGNORECASE.compare("status", "ACTIVE");
        Condition appKeyCondition = Operator.EQUALIGNORECASE.compare("appKey", appKey);
        return dataModel.query(BpmProcessDefinition.class).select(statusCondition.and(appKeyCondition));
    }

    @Override
    public BpmProcessPrivilege createBpmProcessPrivilege(String privilegeName, String application) {
        return BpmProcessPrivilegeImpl.from(dataModel, privilegeName, application);
    }

    @Override
    public BpmProcessPrivilege createBpmProcessPrivilege(BpmProcessDefinition bpmProcessDefinition, String privilegeName, String application) {
        return BpmProcessPrivilegeImpl.from(dataModel, bpmProcessDefinition, privilegeName, application);
    }

    @Override
    public Optional<BpmProcessDefinition> getBpmProcessDefinition(String processName, String version) {
        Condition nameCondition = Operator.EQUALIGNORECASE.compare("processName", processName);
        Condition versionCondition = Operator.EQUALIGNORECASE.compare("version", version);
        List<BpmProcessDefinition> bpmProcessesDefinitions = dataModel.query(BpmProcessDefinition.class)
                .select(nameCondition.and(versionCondition));
        return bpmProcessesDefinitions.isEmpty() ? Optional.empty() : Optional.of(bpmProcessesDefinitions.get(0));
    }

    @Override
    public List<BpmProcessPrivilege> getBpmProcessPrivileges(long processId) {
        List<BpmProcessPrivilege> bpmProcessPrivileges = dataModel.query(BpmProcessPrivilege.class)
                .select(Operator.EQUALIGNORECASE.compare("processId", processId));
        if (!bpmProcessPrivileges.isEmpty()) {
            return bpmProcessPrivileges;
        }
        return Collections.emptyList();
    }

    @Override
    public QueryService getQueryService() {
        return queryService;
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public Query<BpmProcessDefinition> getQueryBpmProcessDefinition() {
        return getQueryService().wrap(dataModel.query(BpmProcessDefinition.class));
    }

    @Override
    public BpmProcessDefinitionBuilder newProcessBuilder() {
        return new BpmProcessDefinitionBuilderImpl(dataModel);
    }

    public List<ProcessAssociationProvider> getProcessAssociationProviders() {
        return Collections.unmodifiableList(processAssociationProviders);
    }

    @Override
    public Optional<ProcessAssociationProvider> getProcessAssociationProvider(String type) {
        return processAssociationProviders.stream()
                .filter(p -> p.getType().equalsIgnoreCase(type)).findFirst();
    }

    @Override
    public ProcessInstanceInfos getRunningProcesses(String authorization, String filter) {
        return getRunningProcesses(authorization, filter, null);
    }

    @Override
    public ProcessInstanceInfos getRunningProcesses(String authorization, String filter, String appKey) {
        ProcessInstanceInfos runningProcesses = this.getBpmServer().getRunningProcesses(authorization, filter);

        List<BpmProcessDefinition> activeProcesses = appKey != null ? this.getActiveBpmProcessDefinitions(appKey) : this.getActiveBpmProcessDefinitions();
        List<ProcessInstanceInfo> filteredRunningProcesses = runningProcesses.processes.stream()
                .filter(process -> activeProcesses.stream()
                        .anyMatch(activeProcess -> process.name.equals(activeProcess.getProcessName()) && process.version
                                .equals(activeProcess.getVersion())))
                .collect(Collectors.toList());

        return new ProcessInstanceInfos(filteredRunningProcesses);
    }

    @Override
    public String getComponentName() {
        return BpmService.COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> translationKeys = new ArrayList<>();
        translationKeys.add(TranslationKeys.APPLICATION);
        translationKeys.add(TranslationKeys.QUEUE_SUBSCRIBER);
        translationKeys.addAll(Arrays.asList(Privileges.values()));
        return translationKeys;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    public Map<String, String> getSingletonInstanceProcesses() {
        return singletonInstanceProcesses;
    }

    public void addSingletonInstanceProcess(String processName, String businessObjectId) {
        this.singletonInstanceProcesses.put(processName, businessObjectId);
    }
}