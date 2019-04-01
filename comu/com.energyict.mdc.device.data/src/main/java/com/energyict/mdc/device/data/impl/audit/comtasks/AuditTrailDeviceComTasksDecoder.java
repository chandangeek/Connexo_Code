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
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.data.impl.audit.AbstractDeviceAuditDecoder;
import com.energyict.mdc.device.data.impl.search.PropertyTranslationKeys;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;


import com.google.common.collect.ImmutableMap;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.energyict.mdc.device.data.impl.search.PropertyTranslationKeys.COMTASK_STATUS;
import static com.energyict.mdc.device.data.impl.search.PropertyTranslationKeys.COMTASK_URGENCY;
import static com.energyict.mdc.device.data.impl.search.PropertyTranslationKeys.COMTASK_USE_DEFAULT_CONNECTION_TASK;
import static com.energyict.mdc.device.data.impl.search.PropertyTranslationKeys.CONNECTION_METHOD;

public class AuditTrailDeviceComTasksDecoder extends AbstractDeviceAuditDecoder {

    AuditTrailDeviceComTasksDecoder(OrmService ormService, Thesaurus thesaurus, MeteringService meteringService, ServerDeviceService serverDeviceService) {
        this.ormService = ormService;
        this.meteringService = meteringService;
        this.serverDeviceService = serverDeviceService;
        this.setThesaurus(thesaurus);
    }

    @Override
    public UnexpectedNumberOfUpdatesException.Operation getOperation(UnexpectedNumberOfUpdatesException.Operation operation, AuditDomainContextType context) {
        return operation;
    }

    @Override
    public Object getContextReference() {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        Optional<ComTask> comTask = getComTask();

        AtomicBoolean completed = new AtomicBoolean(false);
        if(comTask.isPresent()) {
            completed.set(true);
            builder.put("sourceType", "COMTASK");
            builder.put("sourceTypeName", getDisplayName(PropertyTranslationKeys.COMTASK));
            builder.put("name", comTask.get().getName());
            builder.put("sourceId", comTask.get().getId());
        }
        return builder.build();
    }

    @Override
    public List<AuditLogChange> getAuditLogChanges() {

        List<AuditLogChange> auditLogChanges = new ArrayList<>();
        DataModel dataModel = ormService.getDataModel(DeviceDataServices.COMPONENT_NAME).get();
        DataMapper<ComTaskExecution> dataMapper = dataModel.mapper(ComTaskExecution.class);
        long comTaskId = getAuditTrailReference().getPkContext1();

        List<ComTaskExecution> actualEntries = getActualEntries(dataMapper, getActualClauses(comTaskId));
        List<ComTaskExecution> historyByModTimeEntries = getHistoryEntries(dataMapper, getHistoryByModTimeClauses(comTaskId));
        List<ComTaskExecution> historyByJournalTimeEntries = getHistoryEntries(dataMapper, getHistoryByJournalClauses(comTaskId));

        Optional<ComTaskExecution> to = actualEntries.stream()
                .findFirst()
                .map(Optional::of)
                .orElseGet(() -> historyByModTimeEntries.stream().findFirst());

        Optional<ComTaskExecution> from = historyByJournalTimeEntries.stream().findFirst();

        boolean insert = getAuditTrailReference().getOperation() == UnexpectedNumberOfUpdatesException.Operation.INSERT;

        if (to.isPresent() && from.isPresent() || insert){
            auditStatus(from.get(), to.get(), insert).ifPresent(auditLogChanges::add);
            auditUrgency(from.get(), to.get(), insert).ifPresent(auditLogChanges::add);
            auditConnectionMethod(from.get(), to.get(), insert).ifPresent(auditLogChanges::add);
            auditUseDefaultConnectionMethod(from.get(), to.get(), insert).ifPresent(auditLogChanges::add);
        }

        return auditLogChanges;
    }

    private Optional<ComTask> getComTask(){
        DataModel dataModel = ormService.getDataModel(TaskService.COMPONENT_NAME).get();
        DataMapper<ComTask> dataMapper = dataModel.mapper(ComTask.class);

        return getComTaskAfterID(dataMapper).stream().findFirst();
    }


    private Map<String, Object> getActualClauses(long comTaskId) {
        return ImmutableMap.of("COMTASK", comTaskId);
    }
    public <T> List<T> getComTaskAfterID(DataMapper<T> dataMapper) {
        Condition inputCondition = Condition.TRUE;
        Condition condition = inputCondition
                .and(where("id").isEqualTo(getAuditTrailReference().getPkContext1()));
        return dataMapper.select(condition);
    }


    private Map<Operator, Pair<String, Object>> getHistoryByModTimeClauses(long comTaskId) {
        return ImmutableMap.of(Operator.EQUAL, Pair.of("COMTASK", comTaskId),
                Operator.GREATERTHANOREQUAL, Pair.of("modTime", getAuditTrailReference().getModTimeStart()),
                Operator.LESSTHANOREQUAL, Pair.of("modTime", getAuditTrailReference().getModTimeEnd()));
    }

    private Map<Operator, Pair<String, Object>> getHistoryByJournalClauses(long comTaskId) {
        return ImmutableMap.of(Operator.EQUAL, Pair.of("COMTASK", comTaskId),
                Operator.GREATERTHANOREQUAL, Pair.of("journalTime", getAuditTrailReference().getModTimeStart()),
                Operator.LESSTHANOREQUAL, Pair.of("journalTime", getAuditTrailReference().getModTimeEnd()));
    }


    public Optional<AuditLogChange> auditStatus(ComTaskExecution from, ComTaskExecution to, boolean insert) {
        if (to.getStatus().compareTo(from.getStatus()) != 0 || insert) {
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(COMTASK_STATUS));
            auditLogChange.setType(SimplePropertyType.TEXT.name());
            auditLogChange.setValue(to.getStatusDisplayName());
            auditLogChange.setPreviousValue(from.getStatusDisplayName());

            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    public Optional<AuditLogChange> auditUrgency(ComTaskExecution from, ComTaskExecution to, boolean insert) {
        if (to.getPlannedPriority() != from.getPlannedPriority() || insert) {
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(COMTASK_URGENCY));
            auditLogChange.setType(SimplePropertyType.TEXT.name());
            auditLogChange.setValue(to.getPlannedPriority());
            auditLogChange.setPreviousValue(from.getPlannedPriority());

            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    public Optional<AuditLogChange> auditUseDefaultConnectionMethod(ComTaskExecution from, ComTaskExecution to, boolean insert) {

        if (to.usesDefaultConnectionTask() != from.usesDefaultConnectionTask() || insert) {
            com.elster.jupiter.audit.AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(COMTASK_USE_DEFAULT_CONNECTION_TASK));
            auditLogChange.setType(SimplePropertyType.TEXT.name());
            auditLogChange.setValue(to.usesDefaultConnectionTask());
            auditLogChange.setPreviousValue(from.usesDefaultConnectionTask());

            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    public Optional<AuditLogChange> auditConnectionMethod(ComTaskExecution from, ComTaskExecution to, boolean insert) {
       if (to.getConnectionTaskId() != from.getConnectionTaskId() || insert) {
            com.elster.jupiter.audit.AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(CONNECTION_METHOD));
            auditLogChange.setType(SimplePropertyType.TEXT.name());
            if(to.getConnectionTask().isPresent())
                auditLogChange.setValue(to.getConnectionTask().get().getName());
            if(from.getConnectionTask().isPresent())
                auditLogChange.setPreviousValue(from.getConnectionTask().get().getName());
            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }
}
