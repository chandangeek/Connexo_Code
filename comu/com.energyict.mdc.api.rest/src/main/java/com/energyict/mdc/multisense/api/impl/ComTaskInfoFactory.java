package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.MessagesTask;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 7/17/15.
 */
public class ComTaskInfoFactory extends SelectableFieldFactory<ComTaskInfo, ComTask> {

    public LinkInfo asLink(ComTask comTask, Relation relation, UriInfo uriInfo) {
        return asLink(comTask, relation, getUriBuilder(uriInfo));
    }

    public List<LinkInfo> asLink(Collection<ComTask> comTasks, Relation relation, UriInfo uriInfo) {
        UriBuilder uriBuilder = getUriBuilder(uriInfo);
        return comTasks.stream().map(i-> asLink(i, relation, uriBuilder)).collect(toList());
    }

    private LinkInfo asLink(ComTask comTask, Relation relation, UriBuilder uriBuilder) {
        LinkInfo info = new LinkInfo();
        info.id = comTask.getId();
        info.link = Link.fromUriBuilder(uriBuilder)
                .rel(relation.rel())
                .title("Scheduled communication task")
                .build(comTask.getId());
        return info;
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(ComTaskResource.class)
                .path(ComTaskResource.class, "getComTask");
    }

    public ComTaskInfo from(ComTask comTask, UriInfo uriInfo, Collection<String> fields) {
        ComTaskInfo info = new ComTaskInfo();
        copySelectedFields(info, comTask, uriInfo, fields);
        return info;
    }


    @Override
    protected Map<String, PropertyCopier<ComTaskInfo, ComTask>> buildFieldMap() {
        Map<String, PropertyCopier<ComTaskInfo, ComTask>> map = new HashMap<>();
        map.put("id", (comTaskInfo, comTask, uriInfo) -> comTaskInfo.id = comTask.getId());
        map.put("name", (comTaskInfo, comTask, uriInfo) -> comTaskInfo.name = comTask.getName());
        map.put("commands", (comTaskInfo, comTask, uriInfo) -> {
            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                    .path(ProtocolTaskResource.class)
                    .path(ProtocolTaskResource.class, "getProtocolTask");
            comTaskInfo.commands = comTask.getProtocolTasks().stream()
                    .map(pt->{
                        LinkInfo linkInfo = new LinkInfo();
                        linkInfo.id = pt.getId();
                        linkInfo.link = Link.fromUriBuilder(uriBuilder).rel(Relation.REF_RELATION.rel()).build(pt.getId());
                        return linkInfo;
                    }).collect(toList());
        } );
        map.put("categories", (comTaskInfo, comTask, uriInfo) -> {
            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                    .path(DeviceMessageCategoryResource.class)
                    .path(DeviceMessageCategoryResource.class, "getDeviceMessageCategory");
            comTaskInfo.categories = comTask.getProtocolTasks()
                    .stream()
                    .filter(protocolTask -> MessagesTask.class.isAssignableFrom(protocolTask.getClass()))
                    .map(MessagesTask.class::cast)
                    .flatMap(messagesTask -> messagesTask.getDeviceMessageCategories().stream())
                    .map(mt -> {
                        LinkInfo linkInfo = new LinkInfo();
                        linkInfo.id = (long) mt.getId();
                        linkInfo.link = Link.fromUriBuilder(uriBuilder).rel(Relation.REF_RELATION.rel()).build(mt.getId());
                        return linkInfo;
                    }).collect(toList());
        });
        map.put("link", ((comTaskInfo, comTask, uriInfo) ->
            comTaskInfo.link = Link.fromUriBuilder(uriInfo.
                    getBaseUriBuilder().
                    path(ComTaskResource.class).
                    path(ComTaskResource.class, "getComTask")).
                    rel(Relation.REF_SELF.rel()).
                    title("communication task").
                    build(comTask.getId())
        ));

        return map;
    }

}
