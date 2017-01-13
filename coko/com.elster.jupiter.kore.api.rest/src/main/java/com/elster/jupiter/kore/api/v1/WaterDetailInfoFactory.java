package com.elster.jupiter.kore.api.v1;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.metering.WaterDetail;
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

public class WaterDetailInfoFactory extends SelectableFieldFactory<WaterDetailInfo, WaterDetail> {

    private final EffectivityHelper effectivityHelper;

    @Inject
    public WaterDetailInfoFactory(EffectivityHelper effectivityHelper) {
        this.effectivityHelper = effectivityHelper;
    }

    public LinkInfo asLink(WaterDetail waterDetail, Relation relation, UriInfo uriInfo) {
        WaterDetailInfo info = new WaterDetailInfo();
        copySelectedFields(info, waterDetail, uriInfo, Arrays.asList("id", "version"));
        info.link = new ArrayList<>();
        info.link.add(link(waterDetail, relation, uriInfo));
        return info;
    }

    public List<LinkInfo> asLink(Collection<WaterDetail> waterDetails, Relation relation, UriInfo uriInfo) {
        return waterDetails.stream().map(i -> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(WaterDetail waterDetail, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Water detail")
                .build(waterDetail.getUsagePoint().getMRID(), waterDetail.getRange().lowerEndpoint().toEpochMilli());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(UsagePointResource.class)
                .path(UsagePointResource.class, "getDetailsResource")
                .path(WaterDetailResource.class, "getWaterDetail");
    }

    public WaterDetailInfo from(WaterDetail waterDetail, UriInfo uriInfo, Collection<String> fields) {
        WaterDetailInfo info = new WaterDetailInfo();
        copySelectedFields(info, waterDetail, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<WaterDetailInfo, WaterDetail>> buildFieldMap() {
        Map<String, PropertyCopier<WaterDetailInfo, WaterDetail>> map = new HashMap<>();
        map.put("id", (waterDetailInfo, waterDetail, uriInfo) -> waterDetailInfo.id = waterDetail.getRange()
                .lowerEndpoint());
        map.put("version", (waterDetailInfo, waterDetail, uriInfo) -> waterDetailInfo.version = waterDetail.getUsagePoint()
                .getVersion());
        map.put("link", ((waterDetailInfo, waterDetail, uriInfo) -> {
            waterDetailInfo.link = new ArrayList<Link>();
            effectivityHelper.previousDetails(waterDetail)
                    .ifPresent(prev -> waterDetailInfo.link.add(link((WaterDetail) prev, Relation.REF_PREVIOUS, uriInfo)));
            waterDetailInfo.link.add(link(waterDetail, Relation.REF_SELF, uriInfo));
            effectivityHelper.nextDetails(waterDetail)
                    .ifPresent(next -> waterDetailInfo.link.add(link((WaterDetail) next, Relation.REF_NEXT, uriInfo)));
        }));
        map.put("pressure", (waterDetailInfo, waterDetail, uriInfo) -> waterDetailInfo.pressure = waterDetail.getPressure());
        map.put("current", (heatDetailInfo, heatDetail, uriInfo) -> heatDetailInfo.current = heatDetail.isCurrent());
        map.put("physicalCapacity", (waterDetailInfo, waterDetail, uriInfo) -> waterDetailInfo.physicalCapacity = waterDetail
                .getPhysicalCapacity());
        map.put("limiter", (waterDetailInfo, waterDetail, uriInfo) -> waterDetailInfo.limiter = waterDetail.isLimiter());
        map.put("loadLimiterType", (waterDetailInfo, waterDetail, uriInfo) -> waterDetailInfo.loadLimiterType = waterDetail
                .getLoadLimiterType());
        map.put("loadLimit", (waterDetailInfo, waterDetail, uriInfo) -> waterDetailInfo.loadLimit = waterDetail.getLoadLimit());
        map.put("bypass", (waterDetailInfo, waterDetail, uriInfo) -> waterDetailInfo.bypass = waterDetail.isBypassInstalled());
        map.put("bypassStatus", (waterDetailInfo, waterDetail, uriInfo) -> waterDetailInfo.bypassStatus = waterDetail.getBypassStatus());
        map.put("valve", (waterDetailInfo, waterDetail, uriInfo) -> waterDetailInfo.valve = waterDetail.isValveInstalled());
        map.put("capped", (waterDetailInfo, waterDetail, uriInfo) -> waterDetailInfo.capped = waterDetail.isCapped());
        map.put("clamped", (waterDetailInfo, waterDetail, uriInfo) -> waterDetailInfo.clamped = waterDetail.isClamped());
        map.put("grounded", (waterDetailInfo, waterDetail, uriInfo) -> waterDetailInfo.grounded = waterDetail.isGrounded());
        map.put("collar", (waterDetailInfo, waterDetail, uriInfo) -> waterDetailInfo.collar = waterDetail.isCollarInstalled());
        map.put("effectivity", (waterDetailInfo, waterDetail, uriInfo) -> waterDetailInfo.effectivity = effectivityHelper
                .getEffectiveRange(waterDetail));
        return map;
    }

    public UsagePointDetailBuilder createDetail(UsagePoint usagePoint, WaterDetailInfo info) {
        return usagePoint.newWaterDetailBuilder(info.effectivity.lowerEnd)
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
                .withClamp(info.clamped);
    }


}
