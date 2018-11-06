/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.messaging.DestinationSpec;
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
import java.util.Optional;
import java.util.logging.Logger;

import static com.elster.jupiter.messaging.DestinationSpec.whereCorrelationId;
import static com.elster.jupiter.util.Checks.is;

class Installer implements FullInstaller {

    private static final String PUBLIC_KEY_FILE_NAME = "publicKey.txt";
    private static final TranslationKey SUBSCRIBER_DISPLAYNAME = new SimpleTranslationKey(TranslationKeys.SUBSCRIBER_DISPLAYNAME.getKey(),
            TranslationKeys.SUBSCRIBER_DISPLAYNAME.getDefaultFormat());

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
        doTry(
                "Create Additional Events",
                this::createAdditionalEventTypes,
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
        addEventType(WhiteboardEvent.LOGIN);
        addEventType(WhiteboardEvent.LOGOUT);
        addSubscriber4CreatedEventTypes();
    }

    private void addEventType(WhiteboardEvent event) {
        if (this.eventService.getEventType(event.topic()).isPresent()) {
            return;
        }

        this.eventService.buildEventTypeWithTopic(event.topic())
                .name(event.name())
                .component(BasicAuthentication.COMPONENT_NAME)
                .category("Login")
                .scope("System")
                .shouldPublish()
                .withProperty("userName", ValueType.STRING, "name")
                .withProperty("description", ValueType.STRING, "description")
                .withProperty("version", ValueType.LONG, "version")
                .withProperty("lastSuccessfulLogin", ValueType.STRING, "lastSuccessfulLogin")
                .create();

    }

    private void addSubscriber4CreatedEventTypes() {
        Optional<DestinationSpec> destinationSpec = this.messageService.getDestinationSpec(EventService.JUPITER_EVENTS);
        if (!destinationSpec.isPresent()) {
            return;
        }

        DestinationSpec jupiterEvents = destinationSpec.get();
        if (isSubscriberDefined4(jupiterEvents)) {
            return;
        }

        jupiterEvents.subscribe(SUBSCRIBER_DISPLAYNAME, BasicAuthentication.COMPONENT_NAME, Layer.REST,
                whereCorrelationId().isEqualTo(WhiteboardEvent.LOGOUT.topic())
                        .or(whereCorrelationId().isEqualTo(WhiteboardEvent.LOGIN.topic()))
        );
    }

    private boolean isSubscriberDefined4(DestinationSpec jupiterEvents) {
        return jupiterEvents.getSubscribers().stream()
                .anyMatch(s -> s.getName().equals(SUBSCRIBER_DISPLAYNAME.getKey()));
    }

    protected void createAdditionalEventTypes() {
        addEventType(WhiteboardEvent.LOGIN_FAILED);
        addEventType(WhiteboardEvent.TOKEN_RENEWAL);
        addEventType(WhiteboardEvent.TOKEN_EXPIRED);
        addSubscriber4AdditionalEventTypes();
    }

    private void addSubscriber4AdditionalEventTypes() {
        Optional<DestinationSpec> destinationSpec = this.messageService.getDestinationSpec(EventService.JUPITER_EVENTS);
        if (!destinationSpec.isPresent()) {
            return;
        }

        DestinationSpec jupiterEvents = destinationSpec.get();
        if (isSubscriberDefined4(jupiterEvents)) {
            jupiterEvents.unSubscribe(SUBSCRIBER_DISPLAYNAME.getKey());
        }

        jupiterEvents
                .subscribe(SUBSCRIBER_DISPLAYNAME, BasicAuthentication.COMPONENT_NAME, Layer.REST,
                        whereCorrelationId().isEqualTo(WhiteboardEvent.LOGOUT.topic())
                                .or(whereCorrelationId().isEqualTo(WhiteboardEvent.LOGIN.topic()))
                                .or(whereCorrelationId().isEqualTo(WhiteboardEvent.LOGIN_FAILED.topic()))
                                .or(whereCorrelationId().isEqualTo(WhiteboardEvent.TOKEN_RENEWAL.topic()))
                                .or(whereCorrelationId().isEqualTo(WhiteboardEvent.TOKEN_EXPIRED.topic()))
                );
    }

}
