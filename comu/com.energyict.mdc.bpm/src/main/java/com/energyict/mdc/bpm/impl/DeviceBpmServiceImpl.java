package com.energyict.mdc.bpm.impl;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.energyict.mdc.bpm.DeviceBpmService;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.google.inject.AbstractModule;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.energyict.mdc.bpm.BpmProcessDeviceState;


@Component(
        name = "com.mdc.energyict.bpm",
        service = {DeviceBpmService.class, InstallService.class},
        immediate = true,
        property = "name=" + DeviceBpmService.COMPONENTNAME)
public class DeviceBpmServiceImpl implements DeviceBpmService, InstallService {

    private volatile DataModel dataModel;
    private volatile JsonService jsonService;
    private volatile Thesaurus thesaurus;
    private volatile UserService userService;
    private volatile QueryService queryService;

    public DeviceBpmServiceImpl() {
    }

    @Inject
    public DeviceBpmServiceImpl(OrmService ormService, JsonService jsonService, NlsService nlsService, UserService userService, QueryService queryService) {
        this();
        setOrmService(ormService);
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
                bind(JsonService.class).toInstance(jsonService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(UserService.class).toInstance(userService);
                bind(QueryService.class).toInstance(queryService);
                bind(DeviceBpmService.class).toInstance(DeviceBpmServiceImpl.this);
            }
        });
    }

    @Deactivate
    public void deactivate() {

    }

    @Override
    public void install() {
        new InstallerImpl(dataModel).install();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("USR", "MSG", "LIC", "BPM");
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENTNAME, "DBP");
        for (TableSpecs each : TableSpecs.values()) {
            each.addTo(dataModel);
        }
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(DeviceBpmService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getModuleName() {
        return DeviceBpmService.COMPONENTNAME;
    }


    @Override
    public String getComponentName() {
        return DeviceBpmService.COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }


    @Override
    public BpmProcessDeviceState createBpmProcessDeviceState(BpmProcessDefinition bpmProcessDefinition, long deviceStateId, long deviceLifeCycleId, String name, String deviceName) {
        return BpmProcessDeviceStateImpl.from(dataModel, bpmProcessDefinition, deviceStateId, deviceLifeCycleId, name, deviceName);
    }

    public DeviceBpmService getDeviceBpmService() {
        return this;
    }

    @Override
    public Optional<List<BpmProcessDeviceState>> findBpmProcessDeviceStates(long processId) {
        Condition condition = Operator.EQUALIGNORECASE.compare("processId", processId);
        List<BpmProcessDeviceState> processStates = dataModel.query(BpmProcessDeviceState.class).select(condition);
        if (!processStates.isEmpty()) {
            return Optional.of(processStates);
        }
        return Optional.empty();
    }

    @Override
    public void revokeProcessDeviceStates(List<BpmProcessDeviceState> processDeviceStates) {
        processDeviceStates.stream().forEach(BpmProcessDeviceState::delete);
    }

    @Override
    public void grantProcessDeviceStates(List<BpmProcessDeviceState> processDeviceStates) {
        processDeviceStates.stream().forEach(BpmProcessDeviceState::persist);
    }


}
