package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.UsagePoint;
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

public class HeatDetailInfoFactory extends SelectableFieldFactory<HeatDetailInfo, HeatDetail> {

    private final EffectivityHelper effectivityHelper;

    @Inject
    public HeatDetailInfoFactory(EffectivityHelper effectivityHelper) {
        this.effectivityHelper = effectivityHelper;
    }

    public LinkInfo asLink(HeatDetail heatDetail, Relation relation, UriInfo uriInfo) {
        HeatDetailInfo info = new HeatDetailInfo();
        copySelectedFields(info, heatDetail, uriInfo, Arrays.asList("id", "version"));
        info.link = new ArrayList<>();
        info.link.add(link(heatDetail, relation, uriInfo));
        return info;
    }

    public List<LinkInfo> asLink(Collection<HeatDetail> heatDetails, Relation relation, UriInfo uriInfo) {
        return heatDetails.stream().map(i -> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(HeatDetail heatDetail, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Heat details")
                .build(heatDetail.getUsagePoint().getMRID(), heatDetail.getRange().lowerEndpoint().toEpochMilli());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(UsagePointResource.class)
                .path(UsagePointResource.class, "getDetailsResource")
                .path(HeatDetailsResource.class, "getHeatDetails");
    }

    public HeatDetailInfo from(HeatDetail heatDetail, UriInfo uriInfo, Collection<String> fields) {
        HeatDetailInfo info = new HeatDetailInfo();
        copySelectedFields(info, heatDetail, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<HeatDetailInfo, HeatDetail>> buildFieldMap() {
        Map<String, PropertyCopier<HeatDetailInfo, HeatDetail>> map = new HashMap<>();
        map.put("id", (heatDetailInfo, heatDetail, uriInfo) -> heatDetailInfo.id = heatDetail.getRange()
                .lowerEndpoint());
        map.put("version", (heatDetailInfo, heatDetail, uriInfo) -> heatDetailInfo.version = heatDetail.getUsagePoint()
                .getVersion());
        map.put("link", ((heatDetailInfo, heatDetail, uriInfo) -> {
            heatDetailInfo.link = new ArrayList<>();
            effectivityHelper.previousDetails(heatDetail)
                    .ifPresent(prev -> heatDetailInfo.link.add(link((HeatDetail) prev, Relation.REF_PREVIOUS, uriInfo)));
            heatDetailInfo.link.add(link(heatDetail, Relation.REF_SELF, uriInfo));
            effectivityHelper.nextDetails(heatDetail)
                    .ifPresent(next -> heatDetailInfo.link.add(link((HeatDetail) next, Relation.REF_NEXT, uriInfo)));

        }));
        map.put("pressure", (heatDetailInfo, heatDetail, uriInfo) -> heatDetailInfo.pressure = heatDetail.getPressure());
        map.put("current", (heatDetailInfo, heatDetail, uriInfo) -> heatDetailInfo.current = heatDetail.isCurrent());
        map.put("physicalCapacity", (heatDetailInfo, heatDetail, uriInfo) -> heatDetailInfo.physicalCapacity = heatDetail
                .getPhysicalCapacity());
        map.put("bypass", (heatDetailInfo, heatDetail, uriInfo) -> heatDetailInfo.bypass = heatDetail.isBypassInstalled());
        map.put("bypassStatus", (heatDetailInfo, heatDetail, uriInfo) -> heatDetailInfo.bypassStatus = heatDetail.getBypassStatus());
        map.put("valve", (heatDetailInfo, heatDetail, uriInfo) -> heatDetailInfo.valve = heatDetail.isValveInstalled());
        map.put("collar", (heatDetailInfo, heatDetail, uriInfo) -> heatDetailInfo.collar = heatDetail.isCollarInstalled());
        map.put("effectivity", (heatDetailInfo, heatDetail, uriInfo) -> heatDetailInfo.effectivity = effectivityHelper.getEffectiveRange(heatDetail));
        return map;
    }

    public UsagePointDetailBuilder createDetail(UsagePoint usagePoint, HeatDetailInfo info) {
        return usagePoint.newHeatDetailBuilder(info.effectivity.lowerEnd)
                .withCollar(info.collar)
                .withPressure(info.pressure)
                .withPhysicalCapacity(info.physicalCapacity)
                .withBypass(info.bypass)
                .withBypassStatus(info.bypassStatus)
                .withValve(info.valve);
    }


}
