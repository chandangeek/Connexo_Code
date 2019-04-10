package com.energyict.mdc.device.data.impl.audit.communicationTasks;

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
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.protocol.api.ConnectionFunction;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
        return UnexpectedNumberOfUpdatesException.Operation.UPDATE;
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

        List<ComTaskExecution> allEntries = new ArrayList<>();
        allEntries.addAll(actualEntries);
        allEntries.addAll(historyByModTimeEntries);
        allEntries.addAll(historyByJournalTimeEntries);
        allEntries = allEntries.stream()
                .filter(distinctByKey(p -> p.getVersion()))
                .sorted(Comparator.comparing(ComTaskExecution::getVersion))
                .collect(Collectors.toList());

        ComTaskExecution from = allEntries.get(0);
        ComTaskExecution to = allEntries.get(allEntries.size() - 1);
            auditStatus(from, to).ifPresent(auditLogChanges::add);
            auditUrgency(from, to).ifPresent(auditLogChanges::add);
            auditConnectionMethod(from, to).ifPresent(auditLogChanges::add);
            auditUseDefaultConnectionMethod(from, to).ifPresent(auditLogChanges::add);

        return auditLogChanges;
    }

    private Optional<ComTask> getComTask(){
        DataModel dataModel = ormService.getDataModel(TaskService.COMPONENT_NAME).get();
        DataMapper<ComTask> dataMapper = dataModel.mapper(ComTask.class);

        return getComTaskAfterID(dataMapper).stream().findFirst();
    }

    private Map<String, Object> getActualClauses(long comTaskId) {
        return ImmutableMap.of("COMTASK", comTaskId,
                "device", device.get());
    }

    public <T> List<T> getComTaskAfterID(DataMapper<T> dataMapper) {
        Condition inputCondition = Condition.TRUE;
        Condition condition = inputCondition
                .and(where("id").isEqualTo(getAuditTrailReference().getPkContext1()));
        return dataMapper.select(condition);
    }

    private ImmutableSetMultimap<Operator, Pair<String, Object>> getHistoryByModTimeClauses(long comTaskId) {
        return ImmutableSetMultimap.of(Operator.EQUAL, Pair.of("COMTASK", comTaskId),
                Operator.EQUAL, Pair.of("device", device.get()),
                Operator.GREATERTHANOREQUAL, Pair.of("modTime", getAuditTrailReference().getModTimeStart()),
                Operator.LESSTHANOREQUAL, Pair.of("modTime", getAuditTrailReference().getModTimeEnd()));
    }

    private ImmutableSetMultimap<Operator, Pair<String, Object>> getHistoryByJournalClauses(long comTaskId) {
        return ImmutableSetMultimap.of(Operator.EQUAL, Pair.of("COMTASK", comTaskId),
                Operator.EQUAL, Pair.of("device", device.get()),
                Operator.GREATERTHANOREQUAL, Pair.of("journalTime", getAuditTrailReference().getModTimeStart()),
                Operator.LESSTHANOREQUAL, Pair.of("journalTime", getAuditTrailReference().getModTimeEnd()));
    }

    public ImmutableSetMultimap<Operator, Pair<String, Object>> getConnectionTaskHistoryByJournalClauses(Long id) {
        return ImmutableSetMultimap.of(Operator.EQUAL, Pair.of("ID", id),
                Operator.GREATERTHANOREQUAL, Pair.of("journalTime", getAuditTrailReference().getModTimeStart()),
                Operator.LESSTHANOREQUAL, Pair.of("journalTime", getAuditTrailReference().getModTimeEnd()));
    }

    public ImmutableSetMultimap<Operator, Pair<String, Object>> getConnectionTaskHistoryByModTimeClauses(Long id) {
        return ImmutableSetMultimap.of(Operator.EQUAL, Pair.of("ID", id),
                Operator.GREATERTHANOREQUAL, Pair.of("modTime", getAuditTrailReference().getModTimeStart()),
                Operator.LESSTHANOREQUAL, Pair.of("modTime", getAuditTrailReference().getModTimeEnd()));
    }


    public Optional<AuditLogChange> auditStatus(ComTaskExecution from, ComTaskExecution to) {
        if (to.getStatus().compareTo(from.getStatus()) != 0) {
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(COMTASK_STATUS));
            auditLogChange.setType(SimplePropertyType.TEXT.name());
            auditLogChange.setValue(to.getStatusDisplayName());
            auditLogChange.setPreviousValue(from.getStatusDisplayName());

            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    public Optional<AuditLogChange> auditUrgency(ComTaskExecution from, ComTaskExecution to) {
        if (to.getPlannedPriority() != from.getPlannedPriority()) {
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(COMTASK_URGENCY));
            auditLogChange.setType(SimplePropertyType.TEXT.name());
            auditLogChange.setValue(to.getPlannedPriority());
            auditLogChange.setPreviousValue(from.getPlannedPriority());

            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    public Optional<AuditLogChange> auditUseDefaultConnectionMethod(ComTaskExecution from, ComTaskExecution to) {

        if (to.usesDefaultConnectionTask() != from.usesDefaultConnectionTask()) {
            com.elster.jupiter.audit.AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(COMTASK_USE_DEFAULT_CONNECTION_TASK));
            auditLogChange.setType(SimplePropertyType.TEXT.name());
            auditLogChange.setValue(to.usesDefaultConnectionTask());
            auditLogChange.setPreviousValue(from.usesDefaultConnectionTask());

            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    public Optional<AuditLogChange> auditConnectionMethod(ComTaskExecution from, ComTaskExecution to) {
        String toConnectionTaskName = getConnectionTaskName(to);
        String fromConnectionTaskName = getConnectionTaskName(from);

       if (!toConnectionTaskName.equals(fromConnectionTaskName)) {
            com.elster.jupiter.audit.AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(CONNECTION_METHOD));
            auditLogChange.setType(SimplePropertyType.TEXT.name());
            auditLogChange.setValue(toConnectionTaskName);
            auditLogChange.setPreviousValue(fromConnectionTaskName);
            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    String getConnectionTaskName(ComTaskExecution comTaskExecution){
        if (comTaskExecution.getConnectionTaskId() == 0 && !comTaskExecution.getConnectionTask().isPresent() &&
                comTaskExecution.getConnectionFunctionId() == 0) {
            return "";
        }
        else if (comTaskExecution.getConnectionFunctionId() !=0){
            return getConnectionFunction(comTaskExecution)
                    .map(connectionFunction -> getThesaurus().getFormat(PropertyTranslationKeys.CONNECTION_FUNCTION).format(connectionFunction.getConnectionFunctionDisplayName()))
                    .orElseGet(() -> "");
        }
        else if (comTaskExecution.getConnectionTaskId() !=0 && !comTaskExecution.getConnectionTask().isPresent()){
            // connection task was removed; find it in journal entries
            DataModel dataModel = ormService.getDataModel(DeviceDataServices.COMPONENT_NAME).get();
            DataMapper<ConnectionTask> dataMapper = dataModel.mapper(ConnectionTask.class);
            long connectionTaskId = comTaskExecution.getConnectionTaskId();

            List<ConnectionTask> historyByModTimeEntries = getHistoryEntries(dataMapper, getConnectionTaskHistoryByModTimeClauses(connectionTaskId));
            List<ConnectionTask> historyByJournalTimeEntries = getHistoryEntries(dataMapper, getConnectionTaskHistoryByJournalClauses(connectionTaskId));

            List<ConnectionTask> allEntries = new ArrayList<>();
            allEntries.addAll(historyByModTimeEntries);
            allEntries.addAll(historyByJournalTimeEntries);
            return allEntries.stream()
                    .findFirst()
                    .map(connectionTask -> connectionTask.getName())
                    .orElseGet(() -> "");
        }
        return comTaskExecution.getConnectionTask().get().getName();
    }

    private Optional<ConnectionFunction> getConnectionFunction(ComTaskExecution comTaskExecution){
        Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClass = device.get().getDeviceConfiguration().getDeviceType().getDeviceProtocolPluggableClass();
        List<ConnectionFunction> supportedConnectionFunctions = deviceProtocolPluggableClass.isPresent()
                ? deviceProtocolPluggableClass.get().getConsumableConnectionFunctions()
                : Collections.emptyList();
        return supportedConnectionFunctions.stream().filter(cf -> cf.getId() == comTaskExecution.getConnectionFunctionId()).findFirst();
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
