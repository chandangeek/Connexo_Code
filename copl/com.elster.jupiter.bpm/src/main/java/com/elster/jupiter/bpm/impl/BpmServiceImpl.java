package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.bpm.BpmProcess;
import com.elster.jupiter.bpm.BpmServer;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.http.whiteboard.App;
import com.elster.jupiter.license.License;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.json.JsonService;
import java.util.Optional;
import com.google.inject.AbstractModule;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component(
        name = "com.elster.jupiter.bpm",
        service = {BpmService.class, InstallService.class},
        immediate = true,
        property = "name=" + BpmService.COMPONENTNAME)
public class BpmServiceImpl implements BpmService, InstallService {
    public static final String APP_KEY = "BPM";

    private volatile DataModel dataModel;
    private volatile MessageService messageService;
    private volatile JsonService jsonService;
    private volatile AppService appService;
    private volatile Thesaurus thesaurus;
    private volatile UserService userService;
    private volatile License license;
    private BpmServerImpl bpmServer;
    private ServiceRegistration<App> appServiceRegistration;

    public BpmServiceImpl() {
    }

    @Inject
    public BpmServiceImpl(OrmService ormService, MessageService messageService, JsonService jsonService, AppService appService, UserService userService) {
        setOrmService(ormService);
        setMessageService(messageService);
        setAppService(appService);
        setJsonService(jsonService);
        setUserService(userService);
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
                bind(AppService.class).toInstance(appService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(UserService.class).toInstance(userService);
                bind(BpmService.class).toInstance(BpmServiceImpl.this);
                bind(License.class).toInstance(license);
            }
        });
        if (context != null) {
            bpmServer = new BpmServerImpl(context);
            App app = new App("BPM", "BPM console", "connexo", bpmServer.getUrl());
            appServiceRegistration = context.registerService(App.class, app, null);

        }
    }

    @Deactivate
    public void deactivate() {
        bpmServer = null;
        if (appServiceRegistration != null) {
            appServiceRegistration.unregister();
        }
    }

    @Override
    public void install() {
        new InstallerImpl().install(messageService, appService, userService);
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("USR", "MSG", "LIC");
    }

    @Reference(target="(com.elster.jupiter.license.application.key=" + APP_KEY  + ")")
    public void setLicense(License license) {
        this.license = license;
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
    public void setAppService(AppService appService) {
        this.appService = appService;
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
}
