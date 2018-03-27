/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import static com.elster.jupiter.messaging.DestinationSpec.whereCorrelationId;
import static com.elster.jupiter.util.Checks.is;

class Installer implements FullInstaller {
    private static final Logger LOGGER = Logger.getLogger(Installer.class.getName());
    private static final String PUBLIC_KEY_FILE_NAME = "publicKey.txt";

    private final DataModel dataModel;
    private final DataVaultService dataVaultService;
    private final BasicAuthentication basicAuthentication;
    private final EventService eventService;
    private volatile UserService userService;
    private final MessageService messageService;

    @Inject
    public Installer(DataModel dataModel, DataVaultService dataVaultService, BasicAuthentication basicAuthentication,
                     UserService userService,
                     EventService eventService,
                     MessageService messageService) {
        this.dataModel = dataModel;
        this.dataVaultService = dataVaultService;
        this.basicAuthentication = basicAuthentication;
        this.userService = userService;
        this.eventService = eventService;
        this.messageService = messageService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());

        doTry(
                "Create Key Pairs",
                this::createKeys,
                logger
        );
        doTry(
                "Write public key to file " + PUBLIC_KEY_FILE_NAME,
                this::dumpPublicKey,
                logger
        );
        doTry(
                "Create Events",
                this::createEventTypes,
                logger
        );
    }

    private void createKeys() {
        if (!basicAuthentication.getKeyPair().isPresent()) {
            try {
                dataModel.getInstance(KeyStoreImpl.class).init(dataVaultService);
                basicAuthentication.initSecurityTokenImpl();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void dumpPublicKey() {
        if (!is(basicAuthentication.getInstallDir()).emptyOrOnlyWhiteSpace()) {
            Path conf = FileSystems.getDefault()
                    .getPath(basicAuthentication.getInstallDir())
                    .resolve(PUBLIC_KEY_FILE_NAME);
            dumpToFile(conf);
        }
    }

    private void dumpToFile(Path conf) {
        basicAuthentication.saveKeyToFile(conf);
    }

    protected void createEventTypes() {
        for (WhiteboardEvent eventType : WhiteboardEvent.values()) {
            if (!this.eventService.getEventType(eventType.topic()).isPresent()) {
                this.eventService.buildEventTypeWithTopic(eventType.topic())
                        .name(eventType.name())
                        .component(BasicAuthentication.COMPONENT_NAME)
                        .category("Login")
                        .scope("System")
                        .shouldPublish()
                        .create();
            }
        }
        TranslationKey SUBSCRIBER_DISPLAYNAME = new SimpleTranslationKey("subscriber",
        "Handle events to propagate login/logout into MSG_RAWTOPICTABLE");

        messageService.getDestinationSpec(EventService.JUPITER_EVENTS).get()
                    .subscribe(SUBSCRIBER_DISPLAYNAME,
                            BasicAuthentication.COMPONENT_NAME, Layer.DOMAIN,
                            whereCorrelationId().isEqualTo(WhiteboardEvent.LOGOUT.topic())
                            .or(whereCorrelationId().isEqualTo(WhiteboardEvent.LOGIN.topic()))
                            .or(whereCorrelationId().isEqualTo(WhiteboardEvent.LOGOUT.topic()))
                    );


    }
}
