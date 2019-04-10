/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit.connectionMethods;

import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.audit.AuditLogChangeBuilder;
import com.elster.jupiter.audit.AuditTrailReference;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.impl.search.ConnectionStatusSearchableProperty;
import com.energyict.mdc.device.data.impl.search.ConnectionStrategyTranslationKeys;
import com.energyict.mdc.device.data.impl.search.PropertyTranslationKeys;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;

import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.energyict.mdc.device.data.impl.search.PropertyTranslationKeys.CONNECTION_STATUS;
import static com.energyict.mdc.device.data.impl.search.PropertyTranslationKeys.CONNECTION_TASK_CONNECTION_WINDOW;
import static com.energyict.mdc.device.data.impl.search.PropertyTranslationKeys.CONNECTION_TASK_CONNECTION_WINDOW_NO_RESTRICTION;
import static com.energyict.mdc.device.data.impl.search.PropertyTranslationKeys.CONNECTION_TASK_STRATEGY;

public class CommonPropertiesDecoder {

    private AuditTrailConnectionMethodDecoder decoder;

    public CommonPropertiesDecoder(AuditTrailConnectionMethodDecoder decoder){
        this.decoder = decoder;
    }

    public List<AuditLogChange> getAuditLogs(){
        List<AuditLogChange> auditLogChanges = new ArrayList<>();
        Optional<ConnectionTask<?, ?>> connectionTask = getConnectionTask();
        Optional<Device> device = decoder.getDevice();

        if (!device.isPresent() || !connectionTask.isPresent()) {
            return auditLogChanges;
        }

        if (decoder.getAuditTrailReference().getOperation() == UnexpectedNumberOfUpdatesException.Operation.UPDATE) {
            auditLogChanges.addAll(getAuditLogsForUpdate());
        }
        else if (decoder.getAuditTrailReference().getOperation() == UnexpectedNumberOfUpdatesException.Operation.INSERT) {
            auditLogChanges.addAll(getAuditLogsForInsert());
        }
        return auditLogChanges;
    }

    private List<AuditLogChange> getAuditLogsForUpdate(){
        List<AuditLogChange> auditLogChanges = new ArrayList<>();

        DataModel dataModel = getOrmService().getDataModel(DeviceDataServices.COMPONENT_NAME).get();
        DataMapper<ConnectionTask> dataMapper = dataModel.mapper(ConnectionTask.class);
        long connectionTaskId = getAuditTrailReference().getPkContext1();

        List<ConnectionTask> actualEntries = decoder.getActualEntries(dataMapper, getActualClauses(connectionTaskId));
        List<ConnectionTask> historyByModTimeEntries = decoder.getHistoryEntries(dataMapper, decoder.getHistoryByModTimeClauses(connectionTaskId));
        List<ConnectionTask> historyByJournalTimeEntries = decoder.getHistoryEntries(dataMapper, decoder.getHistoryByJournalClauses(connectionTaskId));

        List<ConnectionTask> allEntries = new ArrayList<>();
        allEntries.addAll(actualEntries);
        allEntries.addAll(historyByModTimeEntries);
        allEntries.addAll(historyByJournalTimeEntries);
        allEntries = allEntries.stream()
                .filter( distinctByKey(p -> p.getVersion()))
                .sorted(Comparator.comparing(ConnectionTask::getVersion))
                .collect(Collectors.toList());

        Stream<List<ConnectionTask>> sliding = sliding(allEntries, 2);
        sliding.forEach(connectionTasks -> {
            ConnectionTask from = connectionTasks.get(0);
            ConnectionTask to = connectionTasks.get(1);
            decoder.getAuditLogChangeForString(from.getComPortPool().getName(), to.getComPortPool().getName(), PropertyTranslationKeys.CONNECTION_PORTPOOL).ifPresent(auditLogChanges::add);
            decoder.getAuditLogChangeForString(from.getProtocolDialectConfigurationProperties().getDeviceProtocolDialect().getDeviceProtocolDialectDisplayName(), to.getProtocolDialectConfigurationProperties().getDeviceProtocolDialect().getDeviceProtocolDialectDisplayName(), PropertyTranslationKeys.PROTOCOL_DIALECT).ifPresent(auditLogChanges::add);
            decoder.getAuditLogChangeForBoolean(from.isDefault(), to.isDefault(), PropertyTranslationKeys.CONNECTION_TASK_IS_DEFAULT).ifPresent(auditLogChanges::add);
            auditStatus(from, to).ifPresent(auditLogChanges::add);
            auditConnectionStrategy(from, to).ifPresent(auditLogChanges::add);
            auditSimultaneousConnectionsNo(from, to).ifPresent(auditLogChanges::add);
            auditConnectionWindow(from, to).ifPresent(auditLogChanges::add);
        });

        return auditLogChanges;
    }

    private Optional<AuditLogChange> auditStatus(ConnectionTask from, ConnectionTask to) {
        if (to.getStatus().compareTo(from.getStatus()) != 0) {
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(decoder.getDisplayName(CONNECTION_STATUS));
            auditLogChange.setType(SimplePropertyType.TEXT.name());
            auditLogChange.setValue(decoder.getDisplayName(ConnectionStatusSearchableProperty.ConnectionStatusContainer.valueOf(to.getStatus().toString()).translation()));
            auditLogChange.setPreviousValue(decoder.getDisplayName(ConnectionStatusSearchableProperty.ConnectionStatusContainer.valueOf(from.getStatus().toString()).translation()));
            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    private Optional<AuditLogChange> auditConnectionStrategy(ConnectionTask from, ConnectionTask to) {
        if (ScheduledConnectionTask.class.isAssignableFrom(from.getClass())
                && ((ScheduledConnectionTask)to).getConnectionStrategy().compareTo(((ScheduledConnectionTask)from).getConnectionStrategy()) != 0) {
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(decoder.getDisplayName(CONNECTION_TASK_STRATEGY));
            auditLogChange.setType(SimplePropertyType.TEXT.name());
            auditLogChange.setValue(decoder.getDisplayName(ConnectionStrategyTranslationKeys.valueOf(((ScheduledConnectionTask)to).getConnectionStrategy().name()).translation()));
            auditLogChange.setPreviousValue(decoder.getDisplayName(ConnectionStrategyTranslationKeys.valueOf(((ScheduledConnectionTask)from).getConnectionStrategy().name()).translation()));
            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    private Optional<AuditLogChange> auditConnectionWindow(ConnectionTask from, ConnectionTask to) {
        if (!ScheduledConnectionTask.class.isAssignableFrom(from.getClass())){
            return Optional.empty();
        }

        ComWindow fromComWindow = ((ScheduledConnectionTask)from).getCommunicationWindow();
        ComWindow toComWindow = ((ScheduledConnectionTask)to).getCommunicationWindow();

        if (((fromComWindow == null) && (toComWindow == null)) ||
                ((fromComWindow == null) && (toComWindow.getStart().getMillis() == 0 && toComWindow.getEnd().getMillis() == 0) ||
                ((toComWindow == null) && (fromComWindow.getStart().getMillis() == 0 && fromComWindow.getEnd().getMillis() == 0)))){
            return Optional.empty();
        }

        if (fromComWindow != null && !fromComWindow.equals(toComWindow) || fromComWindow == null){
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(decoder.getDisplayName(CONNECTION_TASK_CONNECTION_WINDOW));
            auditLogChange.setType(SimplePropertyType.TEXT.name());
            auditLogChange.setValue(toComWindow == null ? decoder.getDisplayName(CONNECTION_TASK_CONNECTION_WINDOW_NO_RESTRICTION): toComWindow.toString());
            auditLogChange.setPreviousValue(fromComWindow == null ? decoder.getDisplayName(CONNECTION_TASK_CONNECTION_WINDOW_NO_RESTRICTION): fromComWindow.toString());
            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    private Optional<AuditLogChange> auditSimultaneousConnectionsNo(ConnectionTask from, ConnectionTask to) {
        if (ScheduledConnectionTask.class.isAssignableFrom(from.getClass())){
            return decoder.getAuditLogChangeForInteger(((ScheduledConnectionTask)to).getNumberOfSimultaneousConnections(),
                    ((ScheduledConnectionTask)from).getNumberOfSimultaneousConnections(),
                    PropertyTranslationKeys.CONNECTION_TASK_SIMULTANEOUS_CONNECTIONS_NUMBER);
        }
        return Optional.empty();
    }

    private List<AuditLogChange> getAuditLogsForInsert(){
        return new ArrayList<>();
    }

    private Map<String, Object> getActualClauses(long connectionTaskId) {
        return ImmutableMap.of("id", connectionTaskId);
    }

    private AuditTrailReference getAuditTrailReference() {
        return decoder.getAuditTrailReference();
    }

    private Optional<ConnectionTask<?, ?>>  getConnectionTask(){
        return decoder.getConnectionTask();
    }

    private OrmService getOrmService(){
        return decoder.getOrmService();
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor)
    {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    private static <T> Stream<List<T>> sliding(List<T> list, int size) {
        if(size > list.size())
            return Stream.empty();
        return IntStream.range(0, list.size()-size+1)
                .mapToObj(start -> list.subList(start, start+size));
    }
}
