package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;

import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class GasDetailInfoFactory extends SelectableFieldFactory<GasDetailInfo, GasDetail> {

    private final EffectivityHelper effectivityHelper;

    @Inject
    public GasDetailInfoFactory(EffectivityHelper effectivityHelper) {
        this.effectivityHelper = effectivityHelper;
    }

    public LinkInfo asLink(GasDetail gasDetail, Relation relation, UriInfo uriInfo) {
        GasDetailInfo info = new GasDetailInfo();
        copySelectedFields(info, gasDetail, uriInfo, Arrays.asList("id", "version"));
        info.link = new ArrayList<>();
        info.link.add(link(gasDetail, relation, uriInfo));
        return info;
    }

    public List<LinkInfo> asLink(Collection<GasDetail> gasDetails, Relation relation, UriInfo uriInfo) {
        return gasDetails.stream().map(i -> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(UsagePointDetail gasDetail, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Gas details")
                .build(gasDetail.getUsagePoint().getMRID(), gasDetail.getRange().lowerEndpoint().toEpochMilli());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(UsagePointResource.class)
                .path(UsagePointResource.class, "getDetailsResource")
                .path(GasDetailResource.class, "getGasDetails");
    }

    public GasDetailInfo from(GasDetail gasDetail, UriInfo uriInfo, Collection<String> fields) {
        GasDetailInfo info = new GasDetailInfo();
        copySelectedFields(info, gasDetail, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<GasDetailInfo, GasDetail>> buildFieldMap() {
        Map<String, PropertyCopier<GasDetailInfo, GasDetail>> map = new HashMap<>();
        map.put("id", (gasDetailInfo, gasDetail, uriInfo) -> gasDetailInfo.id = gasDetail.getRange().lowerEndpoint());
        map.put("version", (gasDetailInfo, gasDetail, uriInfo) -> gasDetailInfo.version = gasDetail.getUsagePoint()
                .getVersion());
        map.put("link", ((gasDetailInfo, gasDetail, uriInfo) -> {
            gasDetailInfo.link = new ArrayList<>();
            effectivityHelper.previousDetails(gasDetail)
                    .ifPresent(prev -> gasDetailInfo.link.add(link((GasDetail) prev, Relation.REF_PREVIOUS, uriInfo)));
            gasDetailInfo.link.add(link(gasDetail, Relation.REF_SELF, uriInfo));
            effectivityHelper.nextDetails(gasDetail)
                    .ifPresent(next -> gasDetailInfo.link.add(link((GasDetail) next, Relation.REF_NEXT, uriInfo)));
        }));
        map.put("pressure", (gasDetailInfo, gasDetail, uriInfo) -> gasDetailInfo.pressure = gasDetail.getPressure());
        map.put("current", (gasDetailInfo, gasDetail, uriInfo) -> gasDetailInfo.current = gasDetail.isCurrent());
        map.put("physicalCapacity", (gasDetailInfo, gasDetail, uriInfo) -> gasDetailInfo.physicalCapacity = gasDetail.getPhysicalCapacity());
        map.put("limiter", (gasDetailInfo, gasDetail, uriInfo) -> gasDetailInfo.limiter = gasDetail.isLimiter());
        map.put("loadLimiterType", (gasDetailInfo, gasDetail, uriInfo) -> gasDetailInfo.loadLimiterType = gasDetail.getLoadLimiterType());
        map.put("loadLimit", (gasDetailInfo, gasDetail, uriInfo) -> gasDetailInfo.loadLimit = gasDetail.getLoadLimit());
        map.put("bypass", (gasDetailInfo, gasDetail, uriInfo) -> gasDetailInfo.bypass = gasDetail.isBypassInstalled());
        map.put("bypassStatus", (gasDetailInfo, gasDetail, uriInfo) -> gasDetailInfo.bypassStatus = gasDetail.getBypassStatus());
        map.put("valve", (gasDetailInfo, gasDetail, uriInfo) -> gasDetailInfo.valve = gasDetail.isValveInstalled());
        map.put("capped", (gasDetailInfo, gasDetail, uriInfo) -> gasDetailInfo.capped = gasDetail.isCapped());
        map.put("clamped", (gasDetailInfo, gasDetail, uriInfo) -> gasDetailInfo.clamped = gasDetail.isClamped());
        map.put("interruptible", (gasDetailInfo, gasDetail, uriInfo) -> gasDetailInfo.interruptible = gasDetail.isInterruptible());
        map.put("grounded", (gasDetailInfo, gasDetail, uriInfo) -> gasDetailInfo.grounded = gasDetail
                .isGrounded());
        map.put("collar", (gasDetailInfo, gasDetail, uriInfo) -> gasDetailInfo.collar = gasDetail
                .isCollarInstalled());
        map.put("effectivity", (gasDetailInfo, gasDetail, uriInfo) -> gasDetailInfo.effectivity = effectivityHelper.getEffectiveRange(gasDetail));

        return map;
    }

    public UsagePointDetailBuilder createDetail(UsagePoint usagePoint, GasDetailInfo info) {
        return usagePoint.newGasDetailBuilder(info.effectivity.lowerEnd)
                .withCollar(info.collar)
                .withGrounded(info.grounded)
                .withPressure(info.pressure)
                .withPhysicalCapacity(info.physicalCapacity)
                .withLimiter(info.limiter)
                .withLoadLimit(info.loadLimit)
                .withLoadLimiterType(info.loadLimiterType)
                .withBypass(info.bypass)
                .withBypassStatus(info.bypassStatus)
                .withValve(info.valve)
                .withCap(info.capped)
                .withClamp(info.clamped)
                .withInterruptible(info.interruptible);
    }


}
