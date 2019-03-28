/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit.connectionMethods;

import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.data.impl.audit.AbstractCPSAuditDecoder;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AuditTrailConnectionMethodDecoder extends AbstractCPSAuditDecoder {

    private Optional<ConnectionTask<?, ?>> connectionTask = Optional.empty();

    AuditTrailConnectionMethodDecoder(OrmService ormService, Thesaurus thesaurus, MeteringService meteringService, ServerDeviceService serverDeviceService, CustomPropertySetService customPropertySetService) {
        super(ormService, thesaurus, meteringService, serverDeviceService, customPropertySetService);
    }

    @Override
    protected void decodeReference() {
        try {
            super.decodeReference();
            device.ifPresent(dv -> {
                connectionTask = findConnectionTask(dv);
                if (!connectionTask.isPresent()) {
                    connectionTask = findHistoryConnectionTask();
                }
            });
        }
        catch (Exception ignored){
        }
    }

    @Override
    public UnexpectedNumberOfUpdatesException.Operation getOperation(UnexpectedNumberOfUpdatesException.Operation operation, AuditDomainContextType context) {
        return UnexpectedNumberOfUpdatesException.Operation.UPDATE;
    }

    @Override
    public Object getContextReference() {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        connectionTask
                .map(ct -> {
                    builder.put("name", ct.getName());
                    return builder;
         });
        return builder.build();
    }

    protected List<AuditLogChange> getAuditLogChangesFromDevice() {
        try {
            List<AuditLogChange> auditLogChanges = new ArrayList<>();
            auditLogChanges.addAll(new CustomPropertySetDecoder(this).getAuditLogs());
            auditLogChanges.addAll(new CommonPropertiesDecoder(this).getAuditLogs());
            return auditLogChanges;
        }
        catch (Exception ignored) {
        }
        return Collections.emptyList();
    }

    private Optional<ConnectionTask<?, ?>> findConnectionTask(Device device) {
        return device.getConnectionTasks().stream().filter(c -> c.getId() == getAuditTrailReference().getPkContext1())
                .findFirst();
    }

    private Optional<ConnectionTask<?, ?>> findHistoryConnectionTask() {
        DataMapper<ConnectionTask> dataMapper = ormService.getDataModel(DeviceDataServices.COMPONENT_NAME).get().mapper(ConnectionTask.class);

        List<ConnectionTask> historyEntries = getHistoryEntries(dataMapper, getHistoryByJournalClauses(getAuditTrailReference().getPkContext1()));
        List<ConnectionTask<?,?>> historyEntriesExtended = new ArrayList<>();
        if (historyEntries.size()>0){
            historyEntriesExtended.add(historyEntries.get(0));
        }
        return historyEntriesExtended.stream()
                .findFirst();
    }

    protected Optional<RegisteredCustomPropertySet> getCustomPropertySet() {
        return customPropertySetService
                .findAllCustomPropertySets()
                .stream()
                .filter(x -> x.getId() == getAuditTrailReference().getPkContext2())
                .findFirst();
    }

    public Optional<ConnectionTask<?, ?>>  getConnectionTask(){
        return connectionTask;
    }

    public Optional<Device> getDevice(){
        return device;
    }

    public CustomPropertySetService getCustomPropertySetService(){
        return customPropertySetService;
    }

    public OrmService getOrmService(){
        return ormService;
    }
}