/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppServerCommand;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.Command;
import com.elster.jupiter.appserver.ImportFolderForAppServer;
import com.elster.jupiter.appserver.ImportScheduleOnAppServer;
import com.elster.jupiter.appserver.MessageSeeds;
import com.elster.jupiter.appserver.ServerMessageQueueMissing;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.domain.util.UniqueCaseInsensitive;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.EventType;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.nio.file.Path;
import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.elster.jupiter.util.conditions.Where.where;
import static java.util.stream.Collectors.toList;

@UniqueCaseInsensitive(fields = "name", groups = Save.Create.class, message = "{" + MessageSeeds.Keys.NAME_MUST_BE_UNIQUE + "}")
class AppServerImpl implements AppServer {
    // Application server name should be less then 25 characters due to DB constrains (including "APPSERVER_" prefix)
    private static final int APP_SERVER_NAME_SIZE = 14;
    private static final String APP_SERVER = "AppServer";
    private final DataModel dataModel;
    private final CronExpressionParser cronExpressionParser;
    private final FileImportService fileImportService;
    private final MessageService messageService;
    private final JsonService jsonService;
    private final Thesaurus thesaurus;
    private final TransactionService transactionService;
    private final ThreadPrincipalService threadPrincipalService;
    private final WebServicesService webServicesService;
    private final Provider<EndPointForAppServerImpl> webServiceForAppServerProvider;
    private final EventService eventService;
    private final EndPointConfigurationService endPointConfigurationService;

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(max = APP_SERVER_NAME_SIZE, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_14 + "}")
    @Pattern(regexp = "[a-zA-Z0-9\\-]+", groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.APPSERVER_NAME_INVALID_CHARS + "}")
    private String name;
    private String cronString;
    private transient CronExpression scheduleFrequency;
    private boolean recurrentTaskActive = true;
    private boolean active;
    private List<SubscriberExecutionSpecImpl> executionSpecs;

    private List<ImportScheduleOnAppServerImpl> importSchedulesOnAppServer;

    //audit columns: all managed by ORM
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    @Inject
    AppServerImpl(DataModel dataModel, CronExpressionParser cronExpressionParser, FileImportService fileImportService,
                  MessageService messageService, JsonService jsonService, Thesaurus thesaurus,
                  TransactionService transactionService, ThreadPrincipalService threadPrincipalService,
                  Provider<EndPointForAppServerImpl> webServiceForAppServerProvider,
                  WebServicesService webServicesService, EventService eventService, EndPointConfigurationService endPointConfigurationService) {
        this.dataModel = dataModel;
        this.cronExpressionParser = cronExpressionParser;
        this.fileImportService = fileImportService;
        this.messageService = messageService;
        this.jsonService = jsonService;
        this.thesaurus = thesaurus;
        this.transactionService = transactionService;
        this.threadPrincipalService = threadPrincipalService;
        this.webServiceForAppServerProvider = webServiceForAppServerProvider;
        this.webServicesService = webServicesService;
        this.eventService = eventService;
        this.endPointConfigurationService = endPointConfigurationService;
    }

    static AppServerImpl from(DataModel dataModel, String name, CronExpression scheduleFrequency) {
        return dataModel.getInstance(AppServerImpl.class).init(name, scheduleFrequency);
    }

    AppServerImpl init(String name, CronExpression scheduleFrequency) {
        this.name = name;
        this.scheduleFrequency = scheduleFrequency;
        this.cronString = scheduleFrequency.toString();
        return this;
    }

    @Override
    public SubscriberExecutionSpecImpl createSubscriberExecutionSpec(SubscriberSpec subscriberSpec, int threadCount) {
        try (BatchUpdateImpl updater = forBatchUpdate()) {
            return updater.createActiveSubscriberExecutionSpec(subscriberSpec, threadCount);
        }
    }

    @Override
    public ImportScheduleOnAppServerImpl addImportScheduleOnAppServer(ImportSchedule importSchedule) {
        try (BatchUpdateImpl updater = forBatchUpdate()) {
            return updater.addImportScheduleOnAppServer(importSchedule);
        }
    }

    private DataMapper<SubscriberExecutionSpecImpl> getSubscriberExecutionSpecFactory() {
        return dataModel.mapper(SubscriberExecutionSpecImpl.class);
    }

    private DataMapper<ImportScheduleOnAppServerImpl> getImportScheduleOnAppServerFactory() {
        return dataModel.mapper(ImportScheduleOnAppServerImpl.class);
    }

    @Override
    public List<SubscriberExecutionSpecImpl> getSubscriberExecutionSpecs() {
        if (executionSpecs == null) {
            executionSpecs = getSubscriberExecutionSpecFactory().find("appServer", this);
        }
        return Collections.unmodifiableList(executionSpecs);
    }

    @Override
    public List<ImportScheduleOnAppServerImpl> getImportSchedulesOnAppServer() {
        if (importSchedulesOnAppServer == null) {
            importSchedulesOnAppServer = getImportScheduleOnAppServerFactory().find("appServer", this)
                    .stream()
                    .filter(importService -> importService.getAppServer().getName().equals(this.getName()))
                    .collect(toList());
        }
        return Collections.unmodifiableList(importSchedulesOnAppServer);
    }

    @Override
    public CronExpression getScheduleFrequency() {
        if (scheduleFrequency == null) {
            scheduleFrequency = cronExpressionParser.parse(cronString).orElseThrow(IllegalArgumentException::new);
        }
        return scheduleFrequency;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void sendCommand(AppServerCommand command) {
        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(messagingName());
        if (!destinationSpec.isPresent()) {
            throw new ServerMessageQueueMissing(messagingName(), thesaurus);
        }
        String json = jsonService.serialize(command);
        destinationSpec.get().message(json).send();
    }

    String messagingName() {
        return APP_SERVER + '_' + getName().replaceAll("-", "_").toUpperCase();
    }

    public boolean isRecurrentTaskActive() {
        return recurrentTaskActive;
    }

    @Override
    public void setRecurrentTaskActive(boolean recurrentTaskActive) {
        this.recurrentTaskActive = recurrentTaskActive;
    }


    public boolean isActive() {
        return active;
    }

    @Override
    public void activate() {
        if (!active) {
            try (BatchUpdate updater = forBatchUpdate()) {
                updater.activate();
            }
        }
    }

    @Override
    public void deactivate() {
        if (active) {
            try (BatchUpdate updater = forBatchUpdate()) {
                updater.deactivate();
            }
        }
    }

    @Override
    public void setThreadCount(SubscriberExecutionSpec subscriberExecutionSpec, int threads) {
        try (BatchUpdate updater = forBatchUpdate()) {
            updater.setThreadCount(subscriberExecutionSpec, threads);
        }
    }

    @Override
    public void removeImportDirectory() {
        dataModel.mapper(ImportFolderForAppServer.class)
                .getOptional(getName()).ifPresent(ImportFolderForAppServer::delete);
    }

    @Override
    public Optional<Path> getImportDirectory() {
        return dataModel.mapper(ImportFolderForAppServer.class).getOptional(getName())
                .flatMap(ImportFolderForAppServer::getImportFolder);
    }

    @Override
    public void setImportDirectory(Path path) {
        ImportFolderForAppServer importFolderForAppServer = dataModel.mapper(ImportFolderForAppServer.class)
                .getOptional(getName()).orElse(ImportFolderForAppServerImpl.from(dataModel, path, this));
        importFolderForAppServer.setImportFolder(path);
        importFolderForAppServer.save();
    }

    @Override
    public void delete() {
        try (BatchUpdate updater = forBatchUpdate()) {
            new ArrayList<>(getSubscriberExecutionSpecs()).forEach(updater::removeSubscriberExecutionSpec);
            new ArrayList<>(getImportSchedulesOnAppServer()).forEach(updater::removeImportScheduleOnAppServer);
            updater.delete();
        }
        webServicesService.removeAllEndPoints();
    }

    @Override
    public BatchUpdateImpl forBatchUpdate() {
        return new BatchUpdateImpl();
    }

    @Override
    public void removeSubscriberExecutionSpec(SubscriberExecutionSpec subscriberExecutionSpec) {
        try (BatchUpdate updater = forBatchUpdate()) {
            updater.removeSubscriberExecutionSpec(subscriberExecutionSpec);
        }
    }

    @Override
    public void removeImportScheduleOnAppServer(ImportScheduleOnAppServer importScheduleOnAppServer) {
        try (BatchUpdate updater = forBatchUpdate()) {
            updater.removeImportScheduleOnAppServer(importScheduleOnAppServer);
        }
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    @Override
    public void supportEndPoint(EndPointConfiguration endPointConfiguration) {
        Objects.nonNull(endPointConfiguration);
        if (!supportedEndPoints().contains(endPointConfiguration)) {
            EndPointForAppServerImpl link = webServiceForAppServerProvider.get().init(this, endPointConfiguration);
            link.save();
            eventService.postEvent(EventType.ENDPOINT_CONFIGURATION_CHANGED.topic(), endPointConfiguration);
        }
    }

    @Override
    public void dropEndPointSupport(EndPointConfiguration endPointConfiguration) {
        Objects.nonNull(endPointConfiguration);
        List<WebServiceForAppServer> links = dataModel.query(WebServiceForAppServer.class)
                .select(
                        where(EndPointForAppServerImpl.Fields.EndPointConfiguration.fieldName())
                                .isEqualTo(endPointConfiguration)
                                .and(where(EndPointForAppServerImpl.Fields.AppServer.fieldName())
                                        .isEqualTo(this)));
        if (!links.isEmpty()) {
            dataModel.mapper(WebServiceForAppServer.class).remove(links.get(0)); // there can only be one anyway
        }
        eventService.postEvent(EventType.ENDPOINT_CONFIGURATION_CHANGED.topic(), endPointConfiguration);
    }

    @Override
    public List<EndPointConfiguration> supportedEndPoints() {
        List<EndPointConfiguration> endPointConfigurations =
                dataModel.stream(WebServiceForAppServer.class)
                        .filter(where(EndPointForAppServerImpl.Fields.AppServer.fieldName()).isEqualTo(this))
                        .map(WebServiceForAppServer::getEndPointConfiguration)
                        .collect(toList());
        endPointConfigurationService.findEndPointConfigurations()
                .stream()
                .filter(epc -> !epc.isInbound())
                .forEach(endPointConfigurations::add);
        return endPointConfigurations;
    }

    private SubscriberExecutionSpecImpl getSubscriberExecutionSpec(SubscriberExecutionSpec subscriberExecutionSpec) {
        return getSubscriberExecutionSpecs().stream()
                .filter(sp -> sp.getSubscriberSpec().getDestination().getName().equals(subscriberExecutionSpec.getSubscriberSpec().getDestination().getName()))
                .filter(sp -> sp.getSubscriberSpec().getName().equals(subscriberExecutionSpec.getSubscriberSpec().getName()))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    private ImportScheduleOnAppServerImpl getImportScheduleOnAppServer(ImportScheduleOnAppServer importScheduleOnAppServer) {
        return getImportSchedulesOnAppServer()
                .stream()
                .filter(importSchedule -> importSchedule.getImportSchedule().equals(importScheduleOnAppServer.getImportSchedule()))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    private void removeMessageQueue() {
        messageService.getDestinationSpec(AppService.ALL_SERVERS)
                .ifPresent(allServersTopic -> allServersTopic.unSubscribe(AppServerImpl.this.messagingName()));

        messageService.getDestinationSpec(AppServerImpl.this.messagingName())
                .ifPresent(DestinationSpec::delete);
    }

    public void launchWebServices() {
        if (this.isActive()) {
            this.supportedEndPoints()
                    .stream()
                    .filter(EndPointConfiguration::isActive)
                    .forEach(webServicesService::publishEndPoint);
        }
    }

    public void stopWebServices() {
        webServicesService.removeAllEndPoints();
    }

    private class BatchUpdateImpl implements BatchUpdate {

        private boolean deleted;
        private boolean wasActivated = false;
        private boolean wasDeactivated = false;

        @Override
        public SubscriberExecutionSpecImpl createActiveSubscriberExecutionSpec(SubscriberSpec subscriberSpec, int threadCount) {
            SubscriberExecutionSpecImpl subscriberExecutionSpec = SubscriberExecutionSpecImpl.newActive(dataModel, AppServerImpl.this, subscriberSpec, threadCount);
            getSubscriberExecutionSpecFactory().persist(subscriberExecutionSpec);
            return subscriberExecutionSpec;
        }

        @Override
        public SubscriberExecutionSpecImpl createInactiveSubscriberExecutionSpec(SubscriberSpec subscriberSpec, int threadCount) {
            SubscriberExecutionSpecImpl subscriberExecutionSpec = SubscriberExecutionSpecImpl.newInactive(dataModel, AppServerImpl.this, subscriberSpec, threadCount);
            getSubscriberExecutionSpecFactory().persist(subscriberExecutionSpec);
            return subscriberExecutionSpec;
        }

        @Override
        public void removeSubscriberExecutionSpec(SubscriberExecutionSpec subscriberExecutionSpec) {
            SubscriberExecutionSpecImpl found = getSubscriberExecutionSpec(subscriberExecutionSpec);
            getSubscriberExecutionSpecFactory().remove(found);
            executionSpecs.remove(found);
        }

        @Override
        public ImportScheduleOnAppServerImpl addImportScheduleOnAppServer(ImportSchedule importSchedule) {
            ImportScheduleOnAppServerImpl importScheduleOnAppServer = ImportScheduleOnAppServerImpl.from(dataModel, AppServerImpl.this.fileImportService, importSchedule, AppServerImpl.this);
            getImportScheduleOnAppServerFactory().persist(importScheduleOnAppServer);
            return importScheduleOnAppServer;
        }

        @Override
        public void removeImportScheduleOnAppServer(ImportScheduleOnAppServer importScheduleOnAppServer) {
            ImportScheduleOnAppServerImpl found = getImportScheduleOnAppServer(importScheduleOnAppServer);
            getImportScheduleOnAppServerFactory().remove(found);
            importSchedulesOnAppServer.remove(found);
        }

        @Override
        public void setRecurrentTaskActive(boolean recurrentTaskActive) {
            AppServerImpl.this.recurrentTaskActive = recurrentTaskActive;
        }

        @Override
        public void setThreadCount(SubscriberExecutionSpec subscriberExecutionSpec, int threads) {
            SubscriberExecutionSpecImpl executionSpec = getSubscriberExecutionSpec(subscriberExecutionSpec);
            executionSpec.setThreadCount(threads);
            executionSpec.update();
        }

        @Override
        public void activate(SubscriberExecutionSpec subscriberExecutionSpec) {
            SubscriberExecutionSpecImpl executionSpec = getSubscriberExecutionSpec(subscriberExecutionSpec);
            executionSpec.setActive(true);
            executionSpec.update();

        }

        @Override
        public void deactivate(SubscriberExecutionSpec subscriberExecutionSpec) {
            SubscriberExecutionSpecImpl executionSpec = getSubscriberExecutionSpec(subscriberExecutionSpec);
            executionSpec.setActive(false);
            executionSpec.update();
        }

        @Override
        public void activate() {
            if (!active) {
                active = true;
                this.wasActivated = true;
            }
        }

        @Override
        public void deactivate() {
            if (active) {
                active = false;
                this.wasDeactivated = true;
            }
        }

        @Override
        public void close() {
            if (deleted) {
                dataModel.mapper(AppServer.class).remove(AppServerImpl.this);
                Principal principal = threadPrincipalService.getPrincipal();
                CompletableFuture.runAsync(() -> {
                    try {
                        TimeUnit.MINUTES.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).thenRun(() -> {
                    boolean inUseAgain = dataModel.stream(AppServer.class)
                            .filter(where("name").isEqualTo(getName()))
                            .findAny()
                            .isPresent();
                    if (!inUseAgain) {
                        transactionService.builder()
                                .principal(principal)
                                .run(AppServerImpl.this::removeMessageQueue);
                    }
                });
            } else {
                dataModel.mapper(AppServer.class).update(AppServerImpl.this);
            }
            sendCommand(new AppServerCommand(Command.CONFIG_CHANGED));
            if (wasActivated) {
                launchWebServices();
            } else if (wasDeactivated) {
                stopWebServices();
            }
        }

        @Override
        public void delete() {
            if (!deleted) {
                deleted = true;
            }
        }
    }
}
