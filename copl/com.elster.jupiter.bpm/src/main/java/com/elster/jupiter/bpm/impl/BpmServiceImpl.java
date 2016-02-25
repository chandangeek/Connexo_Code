package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmAppService;
import com.elster.jupiter.bpm.BpmProcess;
import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmProcessDefinitionBuilder;
import com.elster.jupiter.bpm.BpmProcessDeviceState;
import com.elster.jupiter.bpm.BpmProcessPrivilege;
import com.elster.jupiter.bpm.BpmServer;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.ProcessAssociationProvider;
import com.elster.jupiter.bpm.security.Privileges;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;

import com.google.inject.AbstractModule;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;


@Component(
        name = "com.elster.jupiter.bpm",
        service = {BpmService.class, InstallService.class, PrivilegesProvider.class, TranslationKeyProvider.class, MessageSeedProvider.class},
        property = {"name=" + BpmService.COMPONENTNAME}, immediate = true)
public class BpmServiceImpl implements BpmService, InstallService, PrivilegesProvider, TranslationKeyProvider, MessageSeedProvider {

    private volatile DataModel dataModel;
    private volatile MessageService messageService;
    private volatile JsonService jsonService;
    private volatile Thesaurus thesaurus;
    private volatile UserService userService;
    private volatile BpmServerImpl bpmServer;
    private volatile QueryService queryService;
    private List<ProcessAssociationProvider> processAssociationProviders = new CopyOnWriteArrayList<>();

    public BpmServiceImpl() {
    }

    @Inject
    public BpmServiceImpl(OrmService ormService, MessageService messageService, JsonService jsonService, NlsService nlsService, UserService userService, QueryService queryService) {
        this();
        setOrmService(ormService);
        setMessageService(messageService);
        setJsonService(jsonService);
        setUserService(userService);
        setNlsService(nlsService);
        setQueryService(queryService);
        activate(null);
        if (!dataModel.isInstalled()) {
            install();
        }
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
                bind(BpmService.class).toInstance(BpmServiceImpl.this);
            }
        });
        bpmServer = new BpmServerImpl(context);
    }

    @Deactivate
    public void deactivate() {
        bpmServer = null;
    }

    @Override
    public void install() {
        new InstallerImpl(dataModel).install(messageService);
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("USR", "MSG", "LIC");
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

    @Override
    public List<String> getProcesses() {
        //TODO: access directly rest services
        return null;
    }

    @Override
    public Map<String, Object> getProcessParameters(String processId) {
        //TODO: access directly rest services
        return null;
    }

    @Override
    public boolean startProcess(String deploymentId, String process, Map<String, Object> parameters) {
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
    public boolean startProcess(String deploymentId, String process, Map<String, Object> parameters, String auth) {
        boolean result = false;
        Optional<DestinationSpec> found = messageService.getDestinationSpec(BPM_QUEUE_DEST);
        if (found.isPresent()) {
            String json = jsonService.serialize(new BpmProcess(deploymentId, process, parameters, auth));
            found.get().message(json).send();
            result = true;
        }
        return result;
    }

    @Override
    public BpmServer getBpmServer() {
        return bpmServer;
    }

    @Override
    public BpmProcessDeviceState createBpmProcessDeviceState(BpmProcessDefinition process, long lifecycleId, long stateId, String lifecycleName, String stateName) {
        return null;
    }

    @Override
    public BpmProcessDefinition findOrCreateBpmProcessDefinition(String processName, String association, String version, String status) {
        Condition nameCondition = Operator.EQUALIGNORECASE.compare("processName", processName);
        Condition versionCondition = Operator.EQUALIGNORECASE.compare("version", version);
        List<BpmProcessDefinition> bpmProcessDefinitions = dataModel.query(BpmProcessDefinition.class)
                .select(nameCondition.and(versionCondition));
        if (bpmProcessDefinitions.isEmpty()) {
            return BpmProcessDefinitionImpl.from(dataModel, processName, association, version, status);
        }
        bpmProcessDefinitions.get(0).setStatus(status);
        return bpmProcessDefinitions.get(0);
    }

    @Override
    public List<BpmProcessDefinition> getBpmProcessDefinitions() {
        return dataModel.query(BpmProcessDefinition.class).select(Operator.NOTEQUAL.compare("status", "UNDEPLOYED"));
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
        return new BpmProcessDefinitionBuilderImpl(dataModel, this);
    }

    public List<ProcessAssociationProvider> getProcessAssociationProviders() {
        return processAssociationProviders;
    }

    @Override
    public Optional<ProcessAssociationProvider> getProcessAssociationProvider(String type) {
        return processAssociationProviders.stream()
                .filter(p -> p.getType().equalsIgnoreCase(type)).findFirst();
    }

    @Override
    public String getModuleName() {
        return BpmService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(BpmService.COMPONENTNAME, Privileges.RESOURCE_BPM_PROCESSES
                        .getKey(), Privileges.RESOURCE_BPM_PROCESSES_DESCRIPTION.getKey(),
                Arrays.asList(
                        Privileges.Constants.VIEW_BPM, Privileges.Constants.DESIGN_BPM, Privileges.Constants.ADMINISTRATE_BPM)));
        resources.add(userService.createModuleResourceWithPrivileges(BpmService.COMPONENTNAME, Privileges.RESOURCE_BPM_TASKS
                        .getKey(), Privileges.RESOURCE_BPM_TASKS_DESCRIPTION.getKey(),
                Arrays.asList(
                        Privileges.Constants.ASSIGN_TASK, Privileges.Constants.VIEW_TASK, Privileges.Constants.EXECUTE_TASK)));
        resources.add(userService.createModuleResourceWithPrivileges(BpmService.COMPONENTNAME, Privileges.PROCESS_EXECUTION_LEVELS
                        .getKey(), Privileges.PROCESS_EXECUTION_LEVELS_DESCRIPTION.getKey(),
                Arrays.asList(
                        Privileges.Constants.EXECUTE_PROCESSES_LVL_1, Privileges.Constants.EXECUTE_PROCESSES_LVL_2,
                        Privileges.Constants.EXECUTE_PROCESSES_LVL_3, Privileges.Constants.EXECUTE_PROCESSES_LVL_4)));

        return resources;
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
        translationKeys.add(new SimpleTranslationKey(BpmAppService.APPLICATION_KEY, BpmAppService.APPLICATION_NAME));
        translationKeys.add(new SimpleTranslationKey(BpmService.BPM_QUEUE_SUBSC, BpmService.BPM_QUEUE_DISPLAYNAME));
        translationKeys.addAll(Arrays.asList(Privileges.values()));
        return translationKeys;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }
}
