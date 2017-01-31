/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.conditions.Condition;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.nls.console", service = ConsoleCommands.class, property = {"osgi.command.scope=nls",
        "osgi.command.function=load",
        "osgi.command.function=listKeys",
        "osgi.command.function=translate",
        "osgi.command.function=purgeTranslations",
        "osgi.command.function=translations"}, immediate = true)
public class ConsoleCommands {

    private static final String MISSING = "Translation is missing from current thesaurus";

    private volatile NlsService nlsService;

    private Thesaurus thesaurus;
    private String module;
    private Layer layer;
    private DataModel dataModel;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;

    public void load(String moduleName, String layerName) {
        layer = Layer.valueOf(layerName);
        thesaurus = nlsService.getThesaurus(moduleName, layer);
    }

    public void purgeTranslations() {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            dataModel.query(NlsKeyImpl.class, NlsEntry.class).select(Condition.TRUE).forEach(dataModel::remove);
            context.commit();
        }
    }

    public void listKeys() {
        if (thesaurus != null) {
            thesaurus.getTranslationsForCurrentLocale().keySet().stream()
                    .forEach(System.out::println);
        }
    }

    public void translate(String key) {
        if (thesaurus != null) {
            System.out.println(thesaurus.getString(key, MISSING));
        }
    }

    public void translations() {
        if (thesaurus != null) {
            thesaurus.getTranslationsForCurrentLocale().entrySet().stream()
                    .map(entry -> entry.getKey() + " -> " + entry.getValue())
                    .forEach(System.out::println);
        }
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.getDataModel(NlsService.COMPONENTNAME).get();
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

}
