/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.MessagesTask;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 7/17/15.
 */
public class ComTaskInfoFactory extends SelectableFieldFactory<ComTaskInfo, ComTask> {

    private final Provider<ProtocolTaskInfoFactory> protocolTaskInfoFactoryProvider;
    private final Provider<DeviceMessageCategoryInfoFactory> deviceMessageCategoryInfoFactory;

    @Inject
    public ComTaskInfoFactory(Provider<ProtocolTaskInfoFactory> protocolTaskInfoFactoryProvider,
                              Provider<DeviceMessageCategoryInfoFactory> deviceMessageCategoryInfoFactoryProvider) {
        this.protocolTaskInfoFactoryProvider = protocolTaskInfoFactoryProvider;
        this.deviceMessageCategoryInfoFactory = deviceMessageCategoryInfoFactoryProvider;
    }

    public LinkInfo asLink(ComTask comTask, Relation relation, UriInfo uriInfo) {
        ComTaskInfo info = new ComTaskInfo();
        copySelectedFields(info,comTask,uriInfo, Arrays.asList("id","version"));
        info.link = link(comTask,relation,uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<ComTask> comTasks, Relation relation, UriInfo uriInfo) {
        return comTasks.stream().map(i-> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(ComTask comTask, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Scheduled communication task")
                .build(comTask.getId());
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
        map.put("version", (comTaskInfo, comTask, uriInfo) -> comTaskInfo.version = comTask.getVersion());
        map.put("name", (comTaskInfo, comTask, uriInfo) -> comTaskInfo.name = comTask.getName());
        map.put("commands", (comTaskInfo, comTask, uriInfo) ->
            comTaskInfo.commands = protocolTaskInfoFactoryProvider.get().asLink(comTask.getProtocolTasks(), Relation.REF_RELATION, uriInfo));
        map.put("categories", (comTaskInfo, comTask, uriInfo) -> {
            List<DeviceMessageCategory> categories = comTask.getProtocolTasks()
                    .stream()
                    .filter(protocolTask -> MessagesTask.class.isAssignableFrom(protocolTask.getClass()))
                    .map(MessagesTask.class::cast)
                    .flatMap(messagesTask -> messagesTask.getDeviceMessageCategories().stream())
                    .collect(toList());
            comTaskInfo.categories = deviceMessageCategoryInfoFactory.get().asLink(categories, Relation.REF_RELATION, uriInfo);
        });
        map.put("link", ((comTaskInfo, comTask, uriInfo) ->
            comTaskInfo.link = asLink(comTask, Relation.REF_SELF, uriInfo).link
        ));

        return map;
    }

}
