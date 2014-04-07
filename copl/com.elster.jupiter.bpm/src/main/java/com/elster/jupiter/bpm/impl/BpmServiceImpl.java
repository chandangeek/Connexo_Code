package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.*;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.transaction.TransactionService;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component(
        name = "com.elster.jupiter.bpm",
        service = {BpmService.class, InstallService.class},
        immediate = true,
        property = "name=" + BpmService.COMPONENTNAME)
public class BpmServiceImpl implements BpmService, InstallService {

    private volatile DataModel dataModel;
    private volatile TransactionService transactionService;
    private volatile MessageService messageService;
    private volatile JsonService jsonService;
    private volatile Thesaurus thesaurus;

    public BpmServiceImpl(){
    }

    @Inject
    public BpmServiceImpl(OrmService ormService, TransactionService transactionService, MessageService messageService, JsonService jsonService) {
        setTransactionService(transactionService);
        setOrmService(ormService);
        setMessageService(messageService);
        setJsonService(jsonService);
        if (!dataModel.isInstalled()) {
            install();
        }
        activate();
    }

    @Activate
    public void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(TransactionService.class).toInstance(transactionService);
                bind(MessageService.class).toInstance(messageService);
                bind(JsonService.class).toInstance(jsonService);
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
        new InstallerImpl(dataModel).install(messageService);
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENTNAME, "BPM");
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
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
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public List<String> getProcesses() {
        return null;
    }

    @Override
    public Map<String, Object> getProcessParameters(String processId) {
        return null;
    }

    @Override
    public boolean startProcess(String process, Map<String, Object> parameters) {
        return false;
    }
}
