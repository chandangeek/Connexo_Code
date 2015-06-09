package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.*;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.domain.util.UniqueCaseInsensitive;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

@UniqueCaseInsensitive(fields = "name", groups = Save.Create.class, message = "{" + MessageSeeds.Keys.NAME_MUST_BE_UNIQUE + "}")
public class AppServerImpl implements AppServer {
    // Application server name should be less then 25 characters due to DB constrains (including "APPSERVER_" prefix)
    private static final int APP_SERVER_NAME_SIZE = 14;
    private static final String APP_SERVER = "AppServer";
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY +"}")
    @Size(max=APP_SERVER_NAME_SIZE, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_14 +"}")
    @Pattern(regexp="[a-zA-Z0-9\\-]+", groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.APPSERVER_NAME_INVALID_CHARS +"}")
    private String name;
    private String cronString;
    private transient CronExpression scheduleFrequency;
    private boolean recurrentTaskActive = true;
    private boolean active;
    private final DataModel dataModel;
    private final CronExpressionParser cronExpressionParser;
    private final FileImportService fileImportService;
    private final MessageService messageService;
    private final JsonService jsonService;
    private final Thesaurus thesaurus;
    private List<SubscriberExecutionSpecImpl> executionSpecs;
    private List<ImportScheduleOnAppServerImpl> importSchedulesOnAppServer;

    @Inject
	AppServerImpl(DataModel dataModel, CronExpressionParser cronExpressionParser, FileImportService fileImportService, MessageService messageService, JsonService jsonService, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.cronExpressionParser = cronExpressionParser;
        this.fileImportService = fileImportService;
        this.messageService = messageService;
        this.jsonService = jsonService;
        this.thesaurus = thesaurus;
    }
    
    AppServerImpl init(String name, CronExpression scheduleFrequency) {
        this.name = name;
        this.scheduleFrequency = scheduleFrequency;
        this.cronString = scheduleFrequency.toString();
        return this;
    }

    static AppServerImpl from(DataModel dataModel, String name, CronExpression scheduleFrequency) {
        return dataModel.getInstance(AppServerImpl.class).init(name, scheduleFrequency);
    }

    @Override
	public SubscriberExecutionSpecImpl createSubscriberExecutionSpec(SubscriberSpec subscriberSpec, int threadCount) {
        try(BatchUpdateImpl updater = forBatchUpdate()) {
            return updater.createSubscriberExecutionSpec(subscriberSpec, threadCount);
        }
    }

    @Override
    public ImportScheduleOnAppServerImpl addImportScheduleOnAppServer(ImportSchedule importSchedule) {
        try(BatchUpdateImpl updater = forBatchUpdate()) {
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
        return executionSpecs;
    }

    @Override
    public List<ImportScheduleOnAppServerImpl> getImportSchedulesOnAppServer() {
        if(importSchedulesOnAppServer == null) {
            importSchedulesOnAppServer = getImportScheduleOnAppServerFactory().find("appServer", this)
                    .stream()
                    .filter(importService -> importService.getAppServer().getName().equals(this.getName()))
                    .collect(Collectors.toList());
        }
        return importSchedulesOnAppServer;
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
    public void setImportDirectory(Path path) {
        ImportFolderForAppServer importFolderForAppServer = dataModel.mapper(ImportFolderForAppServer.class)
                .getOptional(getName()).orElse(ImportFolderForAppServerImpl.from(dataModel,path, this));
        importFolderForAppServer.setImportFolder(path);
        importFolderForAppServer.save();
    }

    @Override
    public void removeImportDirectory() {
        dataModel.mapper(ImportFolderForAppServer.class)
                .getOptional(getName()).ifPresent(ifas -> ifas.delete());
    }

    @Override
    public Optional<Path> getImportDirectory() {
        dataModel.mapper(ImportFolderForAppServer.class).getOptional(getName());
        return null;
    }

    @Override
    public void delete() {
        try (BatchUpdate updater = forBatchUpdate()) {
            new ArrayList<>(getSubscriberExecutionSpecs()).forEach(updater::removeSubscriberExecutionSpec);
            new ArrayList<>(getImportSchedulesOnAppServer()).forEach(updater::removeImportScheduleOnAppServer);
            updater.delete();
        }
    }

    @Override
    public BatchUpdateImpl forBatchUpdate() {
       return new BatchUpdateImpl();
    }

    private class BatchUpdateImpl implements BatchUpdate {

        boolean needsUpdate = false;

        @Override
        public SubscriberExecutionSpecImpl createSubscriberExecutionSpec(SubscriberSpec subscriberSpec, int threadCount) {
            SubscriberExecutionSpecImpl subscriberExecutionSpec = SubscriberExecutionSpecImpl.from(dataModel, AppServerImpl.this, subscriberSpec, threadCount);
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
        public ImportScheduleOnAppServerImpl addImportScheduleOnAppServer(ImportSchedule importSchedule){
            ImportScheduleOnAppServerImpl importScheduleOnAppServer = ImportScheduleOnAppServerImpl.from(dataModel,AppServerImpl.this.fileImportService, importSchedule, AppServerImpl.this);
            getImportScheduleOnAppServerFactory().persist(importScheduleOnAppServer);
            fileImportService.schedule(importSchedule);
            return importScheduleOnAppServer;
        }

        @Override
        public void removeImportScheduleOnAppServer(ImportScheduleOnAppServer importScheduleOnAppServer){
            ImportScheduleOnAppServerImpl found = getImportScheduleOnAppServer(importScheduleOnAppServer);
            importScheduleOnAppServer.getImportSchedule().ifPresent(importSchedule ->  fileImportService.unSchedule(importSchedule));
            getImportScheduleOnAppServerFactory().remove(found);
            importSchedulesOnAppServer.remove(found);
        }

        @Override
        public void setRecurrentTaskActive(boolean recurrentTaskActive) {
            AppServerImpl.this.recurrentTaskActive = recurrentTaskActive;
            needsUpdate = true;
        }

        @Override
        public void setThreadCount(SubscriberExecutionSpec subscriberExecutionSpec, int threads) {
            SubscriberExecutionSpecImpl executionSpec = getSubscriberExecutionSpec(subscriberExecutionSpec);
            executionSpec.setThreadCount(threads);
            executionSpec.update();
        }

        @Override
        public void activate() {
            if (!active) {
                active = true;
                needsUpdate = true;
            }
        }

        @Override
        public void deactivate() {
            if (active) {
                active = false;
                needsUpdate = true;
            }
        }

        @Override
        public void close() {
            if (needsUpdate) {
                dataModel.mapper(AppServer.class).update(AppServerImpl.this);
            }
            sendCommand(new AppServerCommand(Command.CONFIG_CHANGED));
        }

        @Override
        public void delete() {
            dataModel.mapper(AppServer.class).remove(AppServerImpl.this);
        }
    }

    @Override
    public void removeSubscriberExecutionSpec(SubscriberExecutionSpec subscriberExecutionSpec) {
        try(BatchUpdate updater = forBatchUpdate()) {
            updater.removeSubscriberExecutionSpec(subscriberExecutionSpec);
        }
    }

    @Override
    public void removeImportScheduleOnAppServer(ImportScheduleOnAppServer importScheduleOnAppServer) {
        try(BatchUpdate updater = forBatchUpdate()) {
            updater.removeImportScheduleOnAppServer(importScheduleOnAppServer);
        }
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

    // for future use in the appserver's shut down request
    private void removeMessageQueue() {
        Optional<DestinationSpec> allServersTopic = messageService.getDestinationSpec(AppService.ALL_SERVERS);
        if (allServersTopic.isPresent()) {
            allServersTopic.get().unSubscribe(AppServerImpl.this.messagingName());
        }

        Optional<DestinationSpec> destinationSpecRef = messageService.getDestinationSpec(AppServerImpl.this.messagingName());

        if (destinationSpecRef.isPresent()) {
            destinationSpecRef.get().delete();
        }
    }

}
