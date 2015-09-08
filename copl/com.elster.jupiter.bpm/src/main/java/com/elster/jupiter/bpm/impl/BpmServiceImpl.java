package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmProcess;
import com.elster.jupiter.bpm.BpmServer;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.security.Privileges;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component(
        name = "com.elster.jupiter.bpm",
        service = {BpmService.class, InstallService.class, PrivilegesProvider.class, TranslationKeyProvider.class, MessageSeedProvider.class},
        immediate = true,
        property = "name=" + BpmService.COMPONENTNAME)
public class BpmServiceImpl implements BpmService, InstallService, PrivilegesProvider, TranslationKeyProvider, MessageSeedProvider {

    private volatile DataModel dataModel;
    private volatile MessageService messageService;
    private volatile JsonService jsonService;
    private volatile Thesaurus thesaurus;
    private volatile UserService userService;
    private volatile BpmServerImpl bpmServer;

    public BpmServiceImpl() {
    }

    @Inject
    public BpmServiceImpl(OrmService ormService, MessageService messageService, JsonService jsonService, NlsService nlsService, UserService userService) {
        this();
        setOrmService(ormService);
        setMessageService(messageService);
        setJsonService(jsonService);
        setUserService(userService);
        setNlsService(nlsService);
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
        new InstallerImpl().install(messageService);
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("USR", "MSG", "LIC");
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENTNAME, "BPM");
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
    public BpmServer getBpmServer() {
        return bpmServer;
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
    public String getModuleName() {
        return BpmService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(BpmService.COMPONENTNAME, "bpm.businessProcesses", "bpm.businessProcesses.description",
                Arrays.asList(
                        Privileges.VIEW_BPM, Privileges.DESIGN_BPM)));
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
        return Collections.singletonList(new SimpleTranslationKey(BpmService.BPM_QUEUE_SUBSC, BpmService.BPM_QUEUE_DISPLAYNAME));
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

}