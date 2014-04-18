package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.bpm.BpmProcess;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.json.JsonService;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(
        name = "com.elster.jupiter.bpm",
        service = {BpmService.class, InstallService.class},
        immediate = true,
        property = "name=" + BpmService.COMPONENTNAME)
public class BpmServiceImpl implements BpmService, InstallService {

    private volatile DataModel dataModel;
    private volatile MessageService messageService;
    private volatile JsonService jsonService;
    private volatile AppService appService;
    private volatile Thesaurus thesaurus;

    public BpmServiceImpl(){
    }

    @Inject
    public BpmServiceImpl(OrmService ormService, MessageService messageService, JsonService jsonService, AppService appService) {
        setOrmService(ormService);
        setMessageService(messageService);
        setAppService(appService);
        setJsonService(jsonService);
        activate();
        if (!dataModel.isInstalled()) {
            install();
        }
    }

    @Activate
    public void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MessageService.class).toInstance(messageService);
                bind(JsonService.class).toInstance(jsonService);
                bind(AppService.class).toInstance(appService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(BpmService.class).toInstance(BpmServiceImpl.this);
            }
        });
    }

    @Deactivate
    public void deactivate() {
    }

    @Override
    public void install() {
        new InstallerImpl().install(messageService, appService);

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
            if (found.isPresent()){
                String json = jsonService.serialize(new BpmProcess(deploymentId, process, parameters));
                found.get().message(json).send();
                result = true;
            }
        return result;
    }
}
