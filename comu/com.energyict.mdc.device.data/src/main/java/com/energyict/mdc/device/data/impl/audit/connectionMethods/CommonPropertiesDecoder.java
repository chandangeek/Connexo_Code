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
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Operator;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.impl.search.ConnectionStatusSearchableProperty;
import com.energyict.mdc.device.data.impl.search.ConnectionStrategyTranslationKeys;
import com.energyict.mdc.device.data.impl.search.PropertyTranslationKeys;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;

import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.energyict.mdc.device.data.impl.search.PropertyTranslationKeys.CONNECTION_STATUS;
import static com.energyict.mdc.device.data.impl.search.PropertyTranslationKeys.CONNECTION_TASK_STRATEGY;

public class CommonPropertiesDecoder {

    private AuditTrailConnectionMethodDecoder decoder;

    public CommonPropertiesDecoder(AuditTrailConnectionMethodDecoder decoder){
        this.decoder = decoder;
    }

    public List<AuditLogChange> getAuditLogs(){
        List<AuditLogChange> auditLogChanges = new ArrayList<>();
        Optional<ConnectionTask<?, ?>> connectionTask = getConnectionTask();
        PartialConnectionTask partialConnectionTask = connectionTask.get().getPartialConnectionTask();
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
        List<ConnectionTask> historyByModTimeEntries = decoder.getHistoryEntries(dataMapper, getHistoryByModTimeClauses(connectionTaskId));
        List<ConnectionTask> historyByJournalTimeEntries = decoder.getHistoryEntries(dataMapper, getHistoryByJournalClauses(connectionTaskId));

        Optional<ConnectionTask> to = actualEntries.stream()
                .findFirst()
                .map(Optional::of)
                .orElseGet(() -> historyByModTimeEntries.stream().findFirst());

        Optional<ConnectionTask> from = historyByJournalTimeEntries.stream().findFirst();

        if (to.isPresent() && from.isPresent()){
            decoder.getAuditLogChangeForString(from.get().getComPortPool().getName(), to.get().getComPortPool().getName(), PropertyTranslationKeys.CONNECTION_PORTPOOL).ifPresent(auditLogChanges::add);
            decoder.getAuditLogChangeForString(from.get().getProtocolDialectConfigurationProperties().getDeviceProtocolDialect().getDeviceProtocolDialectDisplayName(), to.get().getProtocolDialectConfigurationProperties().getDeviceProtocolDialect().getDeviceProtocolDialectDisplayName(), PropertyTranslationKeys.PROTOCOL_DIALECT).ifPresent(auditLogChanges::add);
            decoder.getAuditLogChangeForBoolean(from.get().isDefault(), to.get().isDefault(), PropertyTranslationKeys.CONNECTION_TASK_IS_DEFAULT).ifPresent(auditLogChanges::add);
            auditStatus(from.get(), to.get()).ifPresent(auditLogChanges::add);
            auditConnectionStrategy(from.get(), to.get()).ifPresent(auditLogChanges::add);
            auditSimultaneousConnectionsNo(from.get(), to.get()).ifPresent(auditLogChanges::add);
        }

        return auditLogChanges;
    }

    public Optional<AuditLogChange> auditStatus(ConnectionTask from, ConnectionTask to) {
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

    public Optional<AuditLogChange> auditConnectionStrategy(ConnectionTask from, ConnectionTask to) {
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

    public Optional<AuditLogChange> auditSimultaneousConnectionsNo(ConnectionTask from, ConnectionTask to) {
        if (ScheduledConnectionTask.class.isAssignableFrom(from.getClass())){
            return decoder.getAuditLogChangeForInteger(((ScheduledConnectionTask)to).getNumberOfSimultaneousConnections(),
                    ((ScheduledConnectionTask)from).getNumberOfSimultaneousConnections(),
                    PropertyTranslationKeys.CONNECTION_TASK_SIMULTANEOUS_CONNECTIONS_NUMBER);
        }
        return Optional.empty();
    }

    private List<AuditLogChange> getAuditLogsForInsert(){
        List<AuditLogChange> auditLogChanges = new ArrayList<>();
        return auditLogChanges;
    }

    private Map<String, Object> getActualClauses(long connectionTaskId) {
        return ImmutableMap.of("ID", connectionTaskId);
    }

    private Map<Operator, Pair<String, Object>> getHistoryByModTimeClauses(long connectionTaskId) {
        return ImmutableMap.of(Operator.EQUAL, Pair.of("ID", connectionTaskId),
                Operator.GREATERTHANOREQUAL, Pair.of("modTime", getAuditTrailReference().getModTimeStart()),
                Operator.LESSTHANOREQUAL, Pair.of("modTime", getAuditTrailReference().getModTimeEnd()));
    }

    private Map<Operator, Pair<String, Object>> getHistoryByJournalClauses(long connectionTaskId) {
        return ImmutableMap.of(Operator.EQUAL, Pair.of("ID", connectionTaskId),
                Operator.GREATERTHANOREQUAL, Pair.of("journalTime", getAuditTrailReference().getModTimeStart()),
                Operator.LESSTHANOREQUAL, Pair.of("journalTime", getAuditTrailReference().getModTimeEnd()));
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
}
