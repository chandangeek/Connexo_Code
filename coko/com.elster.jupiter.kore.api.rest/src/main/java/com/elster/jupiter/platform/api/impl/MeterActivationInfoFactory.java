package com.elster.jupiter.platform.api.impl;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.rest.util.IntervalInfo;
import com.elster.jupiter.rest.util.hypermedia.LinkInfo;
import com.elster.jupiter.rest.util.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.util.hypermedia.Relation;
import com.elster.jupiter.rest.util.hypermedia.SelectableFieldFactory;

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

public class MeterActivationInfoFactory extends SelectableFieldFactory<MeterActivationInfo, MeterActivation> {

    private final Provider<UsagePointInfoFactory> usagePointInfoFactory;

    @Inject
    public MeterActivationInfoFactory(Provider<UsagePointInfoFactory> usagePointInfoFactory) {
        this.usagePointInfoFactory = usagePointInfoFactory;
    }

    public LinkInfo asLink(MeterActivation meterActivation, Relation relation, UriInfo uriInfo) {
        MeterActivationInfo info = new MeterActivationInfo();
        copySelectedFields(info, meterActivation, uriInfo, Arrays.asList("id", "version"));
        info.link = link(meterActivation, relation, uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<? extends MeterActivation> meterActivations, Relation relation, UriInfo uriInfo) {
        return meterActivations.stream().map(i -> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(MeterActivation meterActivation, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Meter activation")
                .build(meterActivation.getUsagePoint()
                        .get()
                        .getId(), meterActivation.getId());// we're querying meter activations of a usage point, so usage point should always be present
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(MeterActivationResource.class)
                .path(MeterActivationResource.class, "getMeterActivation");
    }

    public MeterActivationInfo from(MeterActivation meterActivation, UriInfo uriInfo, Collection<String> fields) {
        MeterActivationInfo info = new MeterActivationInfo();
        copySelectedFields(info, meterActivation, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<MeterActivationInfo, MeterActivation>> buildFieldMap() {
        Map<String, PropertyCopier<MeterActivationInfo, MeterActivation>> map = new HashMap<>();
        map.put("id", (meterActivationInfo, meterActivation, uriInfo) -> meterActivationInfo.id = meterActivation.getId());
        map.put("version", (meterActivationInfo, meterActivation, uriInfo) -> meterActivationInfo.version = meterActivation
                .getVersion());
        map.put("link", ((meterActivationInfo, meterActivation, uriInfo) ->
                meterActivationInfo.link = link(meterActivation, Relation.REF_SELF, uriInfo)));
        map.put("usagePoint", ((meterActivationInfo, meterActivation, uriInfo) ->
                meterActivation.getUsagePoint()
                        .ifPresent(up -> meterActivationInfo.usagePoint = usagePointInfoFactory.get()
                                .asLink(up, Relation.REF_PARENT, uriInfo))));
        map.put("interval", (meterActivationInfo, meterActivation, uriInfo) -> meterActivationInfo.interval = IntervalInfo
                .from(meterActivation.getInterval().toOpenRange()));
        map.put("meter", (meterActivationInfo, meterActivation, uriInfo) -> meterActivation.getMeter()
                .ifPresent(m -> meterActivationInfo.meter = m.getId()));
        return map;
    }
}
