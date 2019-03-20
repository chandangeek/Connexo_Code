package com.energyict.mdc.device.data.impl.audit.comtasks;

import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.audit.AuditLogChangeBuilder;
import com.elster.jupiter.metering.MeterConfiguration;
import com.elster.jupiter.metering.MeterReadingTypeConfiguration;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.ReadingTypeObisCodeUsage;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.data.impl.audit.AbstractDeviceAuditDecoder;
import com.energyict.mdc.device.data.impl.audit.channelRegisterSpecifications.SqlStatements;
import com.energyict.mdc.device.data.impl.search.PropertyTranslationKeys;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;

import com.google.common.collect.ImmutableMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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
        try {
            List<AuditLogChange> auditLogChanges = new ArrayList<>();

            //getAuditLogChangeForDevice().ifPresent(auditLogChanges::add);
            auditLogChanges.addAll(getAuditLogChangeForEndDevice());

            return auditLogChanges
                    .stream()
                    .distinct()
                    .collect(Collectors.toList());

        } catch (Exception e) {
        }
        return Collections.emptyList();
    }

    private Map<String, Object> getActualClauses() {
        return ImmutableMap.of("DEVICE", device.get().getId());
    }


    private Optional<ComTaskExecution> getComTask(){
        DataModel dataModel = ormService.getDataModel(DeviceDataServices.COMPONENT_NAME).get();
        DataMapper<ComTaskExecution> dataMapper = dataModel.mapper(ComTaskExecution.class);

        return getActualEntries(dataMapper, getActualClauses())
                .stream()
                .findFirst();
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
            //auditLogChangeUrgency.setPreviousValue(fromReadingTypeConfig.getOverflowValue().get());
            auditLogChanges.add(auditLogChangeUrgency);

            AuditLogChange auditLogChangeConnectionMethod = new AuditLogChangeBuilder();
            auditLogChangeConnectionMethod.setName(getDisplayName(PropertyTranslationKeys.CONNECTION_METHOD));
            auditLogChangeConnectionMethod.setType(SimplePropertyType.TEXT.name());
            auditLogChangeConnectionMethod.setValue(comTaskExecution.getConnectionTask().get().getName());
            //auditLogChange.setPreviousValue(fromReadingTypeConfig.getOverflowValue().get());
            auditLogChanges.add(auditLogChangeConnectionMethod);

            AuditLogChange auditLogActivation= new AuditLogChangeBuilder();
            auditLogActivation.setName(getDisplayName(PropertyTranslationKeys.COMTASK_STATUS));
            auditLogActivation.setType(SimplePropertyType.TEXT.name());
            if(comTaskExecution.isOnHold())
                auditLogActivation.setValue("Inactive");
            else
                auditLogActivation.setValue("Active");

            auditLogChanges.add(auditLogActivation);

        }

        return auditLogChanges;
    }
}
