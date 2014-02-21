package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmEngine;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.NoBpmDirectoryFoundException;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(
        name = "com.elster.jupiter.bpm",
        service = {BpmService.class, InstallService.class},
        immediate = true,
        property = "name=" + BpmService.COMPONENTNAME)
public class BpmServiceImpl implements BpmService, InstallService {

    private volatile DataModel dataModel;
    private volatile TransactionService transactionService;
    private volatile Thesaurus thesaurus;

    @Inject
    public BpmServiceImpl(OrmService ormService, TransactionService transactionService) {
        setTransactionService(transactionService);
        setOrmService(ormService);
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
                bind(TransactionService.class).toInstance(transactionService);
                bind(BpmService.class).toInstance(BpmServiceImpl.this);
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

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENTNAME, "BPM");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(BpmService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Override
    public BpmEngine createBpmDirectory(String name) {
        return BpmEngineImpl.from(dataModel, name);
    }

    @Override
    public BpmEngine findBpmDirectory(String name) {
        Optional<BpmEngine> found = dataModel.mapper(BpmEngine.class).getOptional(name);
        if (!found.isPresent()) {
            throw new NoBpmDirectoryFoundException(thesaurus, name);
        }

        // TODO: check if a valid engine can be found at the specified address

        return found.get();
    }
}
