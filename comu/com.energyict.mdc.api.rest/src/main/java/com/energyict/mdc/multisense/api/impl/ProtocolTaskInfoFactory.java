package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.rest.Categories;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 7/20/15.
 */
public class ProtocolTaskInfoFactory extends SelectableFieldFactory<ProtocolTaskInfo, ProtocolTask> {

    public LinkInfo asLink(ProtocolTask protocolTask, Relation relation, UriInfo uriInfo) {
        ProtocolTaskInfo info = new ProtocolTaskInfo();
        copySelectedFields(info,protocolTask,uriInfo, Arrays.asList("id"));
        info.link = link(protocolTask,relation,uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<ProtocolTask> protocolTasks, Relation relation, UriInfo uriInfo) {
        return protocolTasks.stream().map(i-> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(ProtocolTask protocolTask, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Protocol task")
                .build(protocolTask.getId());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(ProtocolTaskResource.class)
                .path(ProtocolTaskResource.class, "getProtocolTask");
    }

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
        map.put("link", ((protocolTaskInfo, protocolTask, uriInfo) -> protocolTaskInfo.link = link(protocolTask, Relation.REF_SELF, uriInfo)));
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
