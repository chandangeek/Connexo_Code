package com.energyict.mdc.device.data.impl.audit.comtasks;

import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.data.impl.audit.AbstractDeviceAuditDecoder;
import com.energyict.mdc.device.data.impl.search.PropertyTranslationKeys;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;


import com.google.common.collect.ImmutableMap;


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
            builder.put("sourceName", comTask.get().getName());
            builder.put("sourceId", comTask.get().getId());
        }
        return builder.build();
    }

    @Override
    public List<AuditLogChange> getAuditLogChanges() {
        return Collections.emptyList();
    }

    private Optional<ComTask> getComTask(){
        DataModel dataModel = ormService.getDataModel(TaskService.COMPONENT_NAME).get();
        DataMapper<ComTask> dataMapper = dataModel.mapper(ComTask.class);

        return getComTaskAfterID(dataMapper).stream().findFirst();
    }

    public <T> List<T> getComTaskAfterID(DataMapper<T> dataMapper) {
        Condition inputCondition = Condition.TRUE;
        Condition condition = inputCondition
                .and(where("id").isEqualTo(getAuditTrailReference().getPkContext1()));
        return dataMapper.select(condition);
    }
}
