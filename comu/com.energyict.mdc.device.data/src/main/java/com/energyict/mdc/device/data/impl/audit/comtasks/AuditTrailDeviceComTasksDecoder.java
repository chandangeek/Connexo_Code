package com.energyict.mdc.device.data.impl.audit.comtasks;

import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.audit.AuditLogChangeBuilder;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.data.impl.audit.AbstractDeviceAuditDecoder;
import com.energyict.mdc.device.data.impl.search.PropertyTranslationKeys;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;


import com.google.common.collect.ImmutableMap;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.elster.jupiter.util.conditions.Where.where;

public class AuditTrailDeviceComTasksDecoder extends AbstractDeviceAuditDecoder {

    AuditTrailDeviceComTasksDecoder(OrmService ormService, Thesaurus thesaurus, MeteringService meteringService, ServerDeviceService serverDeviceService) {
        this.ormService = ormService;
        this.meteringService = meteringService;
        this.serverDeviceService = serverDeviceService;
        this.setThesaurus(thesaurus);
    }

    @Override
    public UnexpectedNumberOfUpdatesException.Operation getOperation(UnexpectedNumberOfUpdatesException.Operation operation, AuditDomainContextType context) {
        return UnexpectedNumberOfUpdatesException.Operation.UPDATE;
    }

    @Override
    public Object getContextReference() {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        Optional<ComTaskExecution> comTask = getComTask();
        String fullAliasName =  comTask
                .map(o -> o.getComTask().getName())
                .orElseGet(() -> "");


        AtomicBoolean completed = new AtomicBoolean(false);
        device.get().getComTaskExecutions().stream()
                .filter(comTaskExec -> comTaskExec.getComTask().getName().equals(fullAliasName))
                .findFirst()
                .map(comTaskExec -> {
                    completed.set(true);
                    builder.put("sourceType", "COMTASK");
                    builder.put("sourceTypeName", getDisplayName(PropertyTranslationKeys.COMTASK));
                    builder.put("sourceName", comTaskExec.getComTask().getName());
                    builder.put("sourceId", comTaskExec.getComTask().getId());
                    return builder;
                    });


        return builder.build();
    }

    @Override
    public List<AuditLogChange> getAuditLogChanges() {
        return Collections.emptyList();
    }



    private Optional<ComTaskExecution> getComTask(){
        DataModel dataModel = ormService.getDataModel(DeviceDataServices.COMPONENT_NAME).get();
        DataMapper<ComTaskExecution> dataMapper = dataModel.mapper(ComTaskExecution.class);

        return getEntries(dataMapper).stream().findFirst();
    }


    private List<AuditLogChange> getAuditLogChangeForEndDevice() {
        List<AuditLogChange> auditLogChanges = new ArrayList<>();
        Optional<ComTaskExecution> comTask = getComTask();

        if(comTask.isPresent()){

            ComTaskExecution comTaskExecution = comTask.get();

            AuditLogChange auditLogChangeUrgency = new AuditLogChangeBuilder();
            auditLogChangeUrgency.setName(getDisplayName(PropertyTranslationKeys.COMTASK_URGENCY));
            auditLogChangeUrgency.setType(SimplePropertyType.NUMBER.name());
            auditLogChangeUrgency.setValue(comTaskExecution.getPlannedPriority());
            auditLogChanges.add(auditLogChangeUrgency);

            AuditLogChange auditLogChangeConnectionMethod = new AuditLogChangeBuilder();
            auditLogChangeConnectionMethod.setName(getDisplayName(PropertyTranslationKeys.CONNECTION_METHOD));
            auditLogChangeConnectionMethod.setType(SimplePropertyType.TEXT.name());
            auditLogChangeConnectionMethod.setValue(comTaskExecution.getConnectionTask().get().getName());
            auditLogChanges.add(auditLogChangeConnectionMethod);

            AuditLogChange auditLogActivation= new AuditLogChangeBuilder();
            auditLogActivation.setName(getDisplayName(PropertyTranslationKeys.COMTASK_STATUS));
            auditLogActivation.setType(SimplePropertyType.TEXT.name());
            auditLogActivation.setValue("Active");
            if(comTaskExecution.isOnHold())
                auditLogActivation.setValue("Inactive");

            auditLogChanges.add(auditLogActivation);

        }

        return auditLogChanges;
    }


    public <T> List<T> getEntries(DataMapper<T> dataMapper) {
        Condition inputCondition = Condition.TRUE;
        Condition condition = inputCondition
                .and(where("DEVICE").isEqualTo(getAuditTrailReference().getPkDomain()))
                .and(where("COMTASK").isEqualTo(getAuditTrailReference().getPkContext1()));
        return dataMapper.select(condition);
    }
}
