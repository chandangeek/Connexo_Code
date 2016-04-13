package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.metering.WaterDetail;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;

import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class UsagePointInfoFactory extends SelectableFieldFactory<UsagePointInfo, UsagePoint> {

    private final Clock clock;
    private final MeteringService meteringService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public UsagePointInfoFactory(Clock clock, MeteringService meteringService, ExceptionFactory exceptionFactory) {
        this.clock = clock;
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
    }

    public LinkInfo asLink(UsagePoint usagePoint, Relation relation, UriInfo uriInfo) {
        UsagePointInfo info = new UsagePointInfo();
        copySelectedFields(info, usagePoint, uriInfo, Arrays.asList("id", "version"));
        info.link = link(usagePoint, relation, uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<UsagePoint> usagePoints, Relation relation, UriInfo uriInfo) {
        return usagePoints.stream().map(i -> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(UsagePoint usagePoint, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Usage point")
                .build(usagePoint.getId());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(UsagePointResource.class)
                .path(UsagePointResource.class, "getUsagePoint");
    }

    public UsagePointInfo from(UsagePoint usagePoint, UriInfo uriInfo, Collection<String> fields) {
        UsagePointInfo info = new UsagePointInfo();
        copySelectedFields(info, usagePoint, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<UsagePointInfo, UsagePoint>> buildFieldMap() {
        Map<String, PropertyCopier<UsagePointInfo, UsagePoint>> map = new HashMap<>();
        map.put("id", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.id = usagePoint.getId());
        map.put("version", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.version = usagePoint.getVersion());
        map.put("link", ((usagePointInfo, usagePoint, uriInfo) ->
                usagePointInfo.link = link(usagePoint, Relation.REF_SELF, uriInfo)));
        map.put("name", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.name = usagePoint.getName());
        map.put("aliasName", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.aliasName = usagePoint.getAliasName());
        map.put("description", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.description = usagePoint.getDescription());
        map.put("location", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.location = usagePoint.getServiceLocationString());
        map.put("mrid", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.mrid = usagePoint.getMRID());
        map.put("outageRegion", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.outageRegion = usagePoint.getOutageRegion());
        map.put("readRoute", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.readRoute = usagePoint.getReadRoute());
        map.put("servicePriority", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.servicePriority = usagePoint.getServicePriority());
        map.put("serviceKind", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.serviceKind = usagePoint.getServiceCategory()
                .getKind());
        map.put("installationTime", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.installationTime = usagePoint
                .getInstallationTime());
        map.put("serviceDeliveryRemark", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.serviceDeliveryRemark = usagePoint
                .getServiceDeliveryRemark());
        map.put("details", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent()) {
                if (ElectricityDetail.class.isAssignableFrom(detail.get().getClass())) {
                    usagePointInfo.details = getElectricityTechnicalInfo((ElectricityDetail) detail.get());
                } else if (GasDetail.class.isAssignableFrom(detail.get().getClass())) {
                    usagePointInfo.details = getGasTechnicalInfo((GasDetail) detail.get());
                } else if (HeatDetail.class.isAssignableFrom(detail.get().getClass())) {
                    usagePointInfo.details = getHeatTechnicalInfo((HeatDetail) detail.get());
                } else if (WaterDetail.class.isAssignableFrom(detail.get().getClass())) {
                    usagePointInfo.details = getWaterTechnicalInfo((WaterDetail) detail.get());
                }
            }
        });
//        map.put("serviceCategory", ((usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.serviceCategory = serviceCategoryInfoFactory.asLink(usagePoint.getServiceCategory(), Relation.REF_RELATION, uriInfo)));
//
//        map.put("meterActivations", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.meterActivations = meterActivationInfoFactory.asLink(usagePoint.getMeterActivations(), Relation.REL_RELATION, uriInfo));
//        map.put("accountabilities", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.accountabilities = accountabilitieInfoFactory.asLink(usagePoint.getAccountabilities(), Relation.REL_RELATION, uriInfo));
//        map.put("usagePointConfigurations", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.usagePointConfigurations = usagePointConfigurationInfoFactory.asLink(usagePoint.getUsagePointConfigurations(), Relation.REL_RELATION, uriInfo));
        return map;
    }

    private UsagePointTechnicalInfo getWaterTechnicalInfo(WaterDetail waterDetail) {
        WaterTechnicalInfo info = new WaterTechnicalInfo();
        info.grounded = waterDetail.isGrounded();
        info.pressure = waterDetail.getPressure();
        info.physicalCapacity = waterDetail.getPhysicalCapacity();
        info.limiter = waterDetail.isLimiter();
        info.loadLimiterType = waterDetail.getLoadLimiterType();
        info.loadLimit = waterDetail.getLoadLimit();
        info.bypass = waterDetail.isBypassInstalled();
        info.bypassStatus = waterDetail.getBypassStatus();
        info.valve = waterDetail.isValveInstalled();
        info.capped = waterDetail.isCapped();
        info.clamped = waterDetail.isClamped();
        info.collar = waterDetail.isCollarInstalled();
        return info;
    }

    private UsagePointTechnicalInfo getHeatTechnicalInfo(HeatDetail heatDetail) {
        HeatTechnicalInfo info = new HeatTechnicalInfo();
        info.pressure = heatDetail.getPressure();
        info.physicalCapacity = heatDetail.getPhysicalCapacity();
        info.bypass = heatDetail.isBypassInstalled();
        info.bypassStatus = heatDetail.getBypassStatus();
        info.valve = heatDetail.isValveInstalled();
        info.collar = heatDetail.isCollarInstalled();
        return info;
    }

    private UsagePointTechnicalInfo getGasTechnicalInfo(GasDetail gasDetail) {
        GasTechnicalInfo info = new GasTechnicalInfo();
        info.grounded = gasDetail.isGrounded();
        info.pressure = gasDetail.getPressure();
        info.physicalCapacity = gasDetail.getPhysicalCapacity();
        info.limiter = gasDetail.isLimiter();
        info.loadLimiterType = gasDetail.getLoadLimiterType();
        info.loadLimit = gasDetail.getLoadLimit();
        info.bypass = gasDetail.isBypassInstalled();
        info.bypassStatus = gasDetail.getBypassStatus();
        info.valve = gasDetail.isValveInstalled();
        info.capped = gasDetail.isCapped();
        info.clamped = gasDetail.isClamped();
        info.interruptible = gasDetail.isInterruptible();
        info.collar = gasDetail.isCollarInstalled();
        return info;
    }

    private UsagePointTechnicalInfo getElectricityTechnicalInfo(ElectricityDetail electricityDetail) {
        ElectricityTechnicalInfo info = new ElectricityTechnicalInfo();
        info.estimatedLoad = electricityDetail.getEstimatedLoad();
        info.interruptible = electricityDetail.isInterruptible();
        info.limiter = electricityDetail.isLimiter();
        info.loadLimiterType = electricityDetail.getLoadLimiterType();
        info.loadLimit = electricityDetail.getLoadLimit();
        info.nominalServiceVoltage = electricityDetail.getNominalServiceVoltage();
        info.phaseCode = electricityDetail.getPhaseCode();
        info.ratedCurrent = electricityDetail.getRatedCurrent();
        info.ratedPower = electricityDetail.getRatedPower();
        info.grounded = electricityDetail.isGrounded();
        info.collar = electricityDetail.isCollarInstalled();
        return info;
    }

    public UsagePoint createUsagePoint(UsagePointInfo usagePointInfo) {
        UsagePoint usagePoint = meteringService.getServiceCategory(usagePointInfo.serviceKind)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_SERVICE_CATEGORY))
                .newUsagePoint(usagePointInfo.mrid, usagePointInfo.installationTime)
                .withAliasName(usagePointInfo.aliasName)
                .withDescription(usagePointInfo.description)
                .withName(usagePointInfo.name)
                .withOutageRegion(usagePointInfo.outageRegion)
                .withReadRoute(usagePointInfo.readRoute)
                .withServiceDeliveryRemark(usagePointInfo.serviceDeliveryRemark)
                .withServiceLocationString(usagePointInfo.location)
                .withServicePriority(usagePointInfo.servicePriority)
                .create();
        UsagePointDetailBuilder builder = usagePointInfo.details.createDetail(usagePoint, clock);
        builder.validate();
        builder.create();
        return usagePoint;


    }
}
