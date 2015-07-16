package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * Created by bvn on 7/15/15.
 */
public class PartialConnectionTaskInfoFactory extends SelectableFieldFactory<PartialConnectionTaskInfo, PartialConnectionTask> {
    public PartialConnectionTaskInfo from(PartialConnectionTask partialConnectionTask, UriInfo uriInfo, Collection<String> fields) {
        PartialConnectionTaskInfo info = new PartialConnectionTaskInfo();
        copySelectedFields(info, partialConnectionTask, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<PartialConnectionTaskInfo, PartialConnectionTask>> buildFieldMap() {
        HashMap<String, PropertyCopier<PartialConnectionTaskInfo, PartialConnectionTask>> map = new HashMap<>();
        map.put("id",(partialConnectionTaskInfo, partialConnectionTask, uriInfo) -> partialConnectionTaskInfo.id = partialConnectionTask.getId());
        map.put("name",(partialConnectionTaskInfo, partialConnectionTask, uriInfo) -> partialConnectionTaskInfo.name = partialConnectionTask.getName());
        map.put("direction",(partialConnectionTaskInfo, partialConnectionTask, uriInfo) -> partialConnectionTaskInfo.direction = ConnectionTaskType.from(partialConnectionTask));
        map.put("link",(partialConnectionTaskInfo, partialConnectionTask, uriInfo) -> {
            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().
                    path(PartialConnectionTaskResource.class).
                    path(PartialConnectionTaskResource.class, "getPartialConnectionTask");
            partialConnectionTaskInfo.link = Link.fromUriBuilder(uriBuilder).rel(LinkInfo.REF_SELF).build(partialConnectionTask.getConfiguration().getDeviceType().getId(), partialConnectionTask.getConfiguration().getId(), partialConnectionTask.getId());
        });
        return map;
    }
}
