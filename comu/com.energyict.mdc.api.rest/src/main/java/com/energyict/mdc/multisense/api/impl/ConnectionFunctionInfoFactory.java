/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.upl.UPLConnectionFunction;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 7/20/15.
 */
public class ConnectionFunctionInfoFactory extends SelectableFieldFactory<ConnectionFunctionInfo, Pair<DeviceProtocolPluggableClass, UPLConnectionFunction>> {

    public LinkInfo asLink(DeviceProtocolPluggableClass protocolPluggableClass, UPLConnectionFunction connectionFunction, Relation relation, UriInfo uriInfo) {
        ConnectionFunctionInfo info = new ConnectionFunctionInfo();
        copySelectedFields(info,Pair.of(protocolPluggableClass, connectionFunction),uriInfo, Collections.singletonList("id"));
        info.link = link(protocolPluggableClass, connectionFunction,relation,uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<Pair<DeviceProtocolPluggableClass, UPLConnectionFunction>> connectionFunctions, Relation relation, UriInfo uriInfo) {
        return connectionFunctions.stream().map(i-> asLink(i.getFirst(), i.getLast(), relation, uriInfo)).collect(toList());
    }

    private Link link(DeviceProtocolPluggableClass protocolPluggableClass, UPLConnectionFunction connectionFunction, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Connection function")
                .build(protocolPluggableClass.getId(), connectionFunction.getId());
    }

    public ConnectionFunctionInfo from(DeviceProtocolPluggableClass pluggableClass, UPLConnectionFunction connectionFunction, UriInfo uriInfo, List<String> fields) {
        ConnectionFunctionInfo info = new ConnectionFunctionInfo();
        copySelectedFields(info, Pair.of(pluggableClass,connectionFunction), uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<ConnectionFunctionInfo, Pair<DeviceProtocolPluggableClass, UPLConnectionFunction>>> buildFieldMap() {
        Map<String, PropertyCopier<ConnectionFunctionInfo, Pair<DeviceProtocolPluggableClass, UPLConnectionFunction>>> map = new HashMap<>();
        map.put("id", (connectionFunctionInfo, pair, uriInfo) -> connectionFunctionInfo.id = pair.getLast().getId());
        map.put("name", (connectionFunctionInfo, pair, uriInfo) -> connectionFunctionInfo.name = pair.getLast().getConnectionFunctionName());
        map.put("link", ((connectionFunctionInfo, pair, uriInfo) -> connectionFunctionInfo.link = link(pair.getFirst(), pair.getLast(), Relation.REF_SELF, uriInfo)));
        return map;
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(ConnectionFunctionResource.class)
                .path(ConnectionFunctionResource.class, "getConnectionFunction");
    }
}