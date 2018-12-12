/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class UsagePointLifeCycleStateInfoFactory extends SelectableFieldFactory<UsagePointLifeCycleStateInfo, State> {

    public UsagePointLifeCycleStateInfoFactory() {
    }

    LinkInfo asLink(State usagePointLifeCycleState, Relation relation, UriInfo uriInfo) {
        UsagePointLifeCycleStateInfo info = new UsagePointLifeCycleStateInfo();
        copySelectedFields(info, usagePointLifeCycleState, uriInfo, Arrays.asList("id", "version"));
        info.link = link(usagePointLifeCycleState, relation, uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<State> usagePointLifeCycleStates, Relation relation, UriInfo uriInfo) {
        return usagePointLifeCycleStates.stream().map(i -> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(State usagePointLifeCycleState, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Usage point life cycle state")
                .build(usagePointLifeCycleState.getId());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(UsagePointLifeCycleStateResource.class)
                .path(UsagePointLifeCycleStateResource.class, "getUsagePointLifeCycleState");
    }

    public UsagePointLifeCycleStateInfo from(State usagePointLifeCycleState, UriInfo uriInfo, Collection<String> fields) {
        UsagePointLifeCycleStateInfo info = new UsagePointLifeCycleStateInfo();
        copySelectedFields(info, usagePointLifeCycleState, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<UsagePointLifeCycleStateInfo, State>> buildFieldMap() {
        Map<String, PropertyCopier<UsagePointLifeCycleStateInfo, State>> map = new HashMap<>();
        map.put("id", (usagePointLifeCycleStateInfo, usagePointLifeCycleState, uriInfo) -> usagePointLifeCycleStateInfo.id = usagePointLifeCycleState.getId());
        map.put("version", (usagePointLifeCycleStateInfo, usagePointLifeCycleState, uriInfo) -> usagePointLifeCycleStateInfo.version = usagePointLifeCycleState.getVersion());
        map.put("link", ((usagePointLifeCycleStateInfo, usagePointLifeCycleState, uriInfo) ->
                usagePointLifeCycleStateInfo.link = link(usagePointLifeCycleState, Relation.REF_SELF, uriInfo)));
        map.put("name", (usagePointLifeCycleStateInfo, usagePointLifeCycleState, uriInfo) -> usagePointLifeCycleStateInfo.name = usagePointLifeCycleState.getName());
        map.put("isInitial", (usagePointLifeCycleStateInfo, usagePointLifeCycleState, uriInfo) -> usagePointLifeCycleStateInfo.isInitial = usagePointLifeCycleState.isInitial());
        map.put("stage", (usagePointLifeCycleStateInfo, usagePointLifeCycleState, uriInfo) -> usagePointLifeCycleStateInfo.stage = usagePointLifeCycleState.getStage().orElse(stageNullObject()).getName());
        return map;
    }

    private Stage stageNullObject() {
        return new Stage() {
            @Override
            public long getId() {
                return 0;
            }

            @Override
            public String getName() {
                return null;
            }
        };
    }

}
