package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.rest.Categories;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javax.ws.rs.core.UriInfo;

/**
 * Created by bvn on 7/20/15.
 */
public class ProtocolTaskInfoFactory extends SelectableFieldFactory<ProtocolTaskInfo, ProtocolTask> {

    public ProtocolTaskInfo from(ProtocolTask protocolTask, UriInfo uriInfo, Collection<String> fields) {
        ProtocolTaskInfo info = new ProtocolTaskInfo();
        copySelectedFields(info, protocolTask, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<ProtocolTaskInfo, ProtocolTask>> buildFieldMap() {
        Map<String, PropertyCopier<ProtocolTaskInfo, ProtocolTask>> map = new HashMap<>();
        map.put("id", (protocolTaskInfo, protocolTask, uriInfo) -> protocolTaskInfo.id = protocolTask.getId());
        map.put("category", (protocolTaskInfo, protocolTask, uriInfo) -> getProtocolTaskCategory(protocolTask).ifPresent(task->protocolTaskInfo.category=task.getId()));
        map.put("action", (protocolTaskInfo, protocolTask, uriInfo) -> protocolTaskInfo.action = getAction(protocolTask));
        return map;
    }

    private String getAction(ProtocolTask protocolTask) {
        Optional<Categories> protocolTaskCategory = getProtocolTaskCategory(protocolTask);
        if (protocolTaskCategory.isPresent()) {
            return protocolTaskCategory.get().getActionAsStr(protocolTaskCategory.get().getAction(protocolTask));
        }
        return null;
    }

    private Optional<Categories> getProtocolTaskCategory(ProtocolTask protocolTask) {
        return Stream.of(Categories.values())
                .filter(category->category.getProtocolTaskClass().isAssignableFrom(protocolTask.getClass()))
                .findFirst();
    }

}
