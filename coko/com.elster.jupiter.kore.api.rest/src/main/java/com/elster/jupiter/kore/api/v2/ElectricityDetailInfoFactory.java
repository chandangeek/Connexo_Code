package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.metering.ElectricityDetail;
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

public class ElectricityDetailInfoFactory extends SelectableFieldFactory<ElectricityDetailInfo, ElectricityDetail> {

    private final EffectivityHelper effectivityHelper;

    @Inject
    public ElectricityDetailInfoFactory(EffectivityHelper effectivityHelper) {
        this.effectivityHelper = effectivityHelper;
    }

    public LinkInfo asLink(ElectricityDetail electricityDetail, Relation relation, UriInfo uriInfo) {
        ElectricityDetailInfo info = new ElectricityDetailInfo();
        copySelectedFields(info, electricityDetail, uriInfo, Arrays.asList("id", "version"));
        info.link = new ArrayList<>();
        info.link.add(link(electricityDetail, relation, uriInfo));
        return info;
    }

    public List<LinkInfo> asLink(Collection<ElectricityDetail> electricityDetails, Relation relation, UriInfo uriInfo) {
        return electricityDetails.stream().map(i -> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(ElectricityDetail electricityDetail, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Electricity detail")
                .build(electricityDetail.getUsagePoint().getMRID(), electricityDetail.getRange()
                        .lowerEndpoint()
                        .toEpochMilli());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(UsagePointResource.class)
                .path(UsagePointResource.class, "getDetailsResource")
                .path(ElectricityDetailResource.class, "getElectricityDetails");
    }

    public ElectricityDetailInfo from(ElectricityDetail electricityDetail, UriInfo uriInfo, Collection<String> fields) {
        ElectricityDetailInfo info = new ElectricityDetailInfo();
        copySelectedFields(info, electricityDetail, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<ElectricityDetailInfo, ElectricityDetail>> buildFieldMap() {
        Map<String, PropertyCopier<ElectricityDetailInfo, ElectricityDetail>> map = new HashMap<>();
        map.put("id", (electricityDetailInfo, electricityDetail, uriInfo) -> electricityDetailInfo.id = electricityDetail
                .getRange()
                .lowerEndpoint());
        map.put("version", (electricityDetailInfo, electricityDetail, uriInfo) -> electricityDetailInfo.version = electricityDetail
                .getUsagePoint()
                .getVersion());
        map.put("link", ((electricityDetailInfo, electricityDetail, uriInfo) -> {
            electricityDetailInfo.link = new ArrayList<>();
            effectivityHelper.previousDetails(electricityDetail)
                    .ifPresent(prev -> electricityDetailInfo.link.add(link((ElectricityDetail) prev, Relation.REF_PREVIOUS, uriInfo)));
            electricityDetailInfo.link.add(link(electricityDetail, Relation.REF_SELF, uriInfo));
            effectivityHelper.nextDetails(electricityDetail)
                    .ifPresent(next -> electricityDetailInfo.link.add(link((ElectricityDetail) next, Relation.REF_NEXT, uriInfo)));
        }));
        map.put("current", (electricityDetailInfo, electricityDetail, uriInfo) -> electricityDetailInfo.current = electricityDetail
                .isCurrent());
        map.put("nominalServiceVoltage", (electricityDetailInfo, electricityDetail, uriInfo) -> electricityDetailInfo.nominalServiceVoltage = electricityDetail
                .getNominalServiceVoltage());
        map.put("phaseCode", (electricityDetailInfo, electricityDetail, uriInfo) -> electricityDetailInfo.phaseCode = electricityDetail
                .getPhaseCode());
        map.put("ratedCurrent", (electricityDetailInfo, electricityDetail, uriInfo) -> electricityDetailInfo.ratedCurrent = electricityDetail
                .getRatedCurrent());
        map.put("ratedPower", (electricityDetailInfo, electricityDetail, uriInfo) -> electricityDetailInfo.ratedPower = electricityDetail
                .getRatedPower());
        map.put("estimatedLoad", (electricityDetailInfo, electricityDetail, uriInfo) -> electricityDetailInfo.estimatedLoad = electricityDetail
                .getEstimatedLoad());
        map.put("limiter", (electricityDetailInfo, electricityDetail, uriInfo) -> electricityDetailInfo.limiter = electricityDetail
                .isLimiter());
        map.put("loadLimiterType", (electricityDetailInfo, electricityDetail, uriInfo) -> electricityDetailInfo.loadLimiterType = electricityDetail
                .getLoadLimiterType());
        map.put("loadLimit", (electricityDetailInfo, electricityDetail, uriInfo) -> electricityDetailInfo.loadLimit = electricityDetail
                .getLoadLimit());
        map.put("interruptible", (electricityDetailInfo, electricityDetail, uriInfo) -> electricityDetailInfo.interruptible = electricityDetail
                .isInterruptible());
        map.put("grounded", (electricityDetailInfo, electricityDetail, uriInfo) -> electricityDetailInfo.grounded = electricityDetail
                .isGrounded());
        map.put("collar", (electricityDetailInfo, electricityDetail, uriInfo) -> electricityDetailInfo.collar = electricityDetail
                .isCollarInstalled());
        map.put("effectivity", (electricityDetailInfo, electricityDetail, uriInfo) -> electricityDetailInfo.effectivity = effectivityHelper
                .getEffectiveRange(electricityDetail));
        return map;
    }

    public UsagePointDetailBuilder createDetail(UsagePoint usagePoint, ElectricityDetailInfo info) {
        return usagePoint.newElectricityDetailBuilder(info.effectivity.lowerEnd)
                .withCollar(info.collar)
                .withGrounded(info.grounded)
                .withNominalServiceVoltage(info.nominalServiceVoltage)
                .withPhaseCode(info.phaseCode)
                .withRatedCurrent(info.ratedCurrent)
                .withRatedPower(info.ratedPower)
                .withEstimatedLoad(info.estimatedLoad)
                .withLimiter(info.limiter)
                .withLoadLimiterType(info.loadLimiterType)
                .withLoadLimit(info.loadLimit)
                .withInterruptible(info.interruptible);
    }

}
