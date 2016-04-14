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
        UsagePointInfo info = new DefaultUsagePointInfo();
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
        UsagePointInfo info = getUsagePointInfo(usagePoint);
        copySelectedFields(info, usagePoint, uriInfo, fields);
        return info;
    }

    protected UsagePointInfo getUsagePointInfo(UsagePoint usagePoint) {
        Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
        if (detail.isPresent()) {
            if (ElectricityDetail.class.isAssignableFrom(detail.get().getClass())) {
                return new ElectricityTechnicalInfo();
            } else if (GasDetail.class.isAssignableFrom(detail.get().getClass())) {
                return new GasTechnicalInfo();
            } else if (HeatDetail.class.isAssignableFrom(detail.get().getClass())) {
                return new HeatTechnicalInfo();
            } else if (WaterDetail.class.isAssignableFrom(detail.get().getClass())) {
                return new WaterTechnicalInfo();
            }
        }
        throw exceptionFactory.newException(MessageSeeds.UNSUPPORTED_TYPE);
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

        // GAS
        map.put("grounded", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent()) {
                if (GasDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((GasTechnicalInfo) usagePointInfo).grounded = ((GasDetail) detail.get()).isGrounded();
                } else if (WaterDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((WaterTechnicalInfo) usagePointInfo).grounded = ((WaterDetail) detail.get()).isGrounded();
                }
            }
        });
        map.put("pressure", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent()) {
                if (GasDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((GasTechnicalInfo) usagePointInfo).pressure = ((GasDetail) detail.get()).getPressure();
                } else if (WaterDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((WaterTechnicalInfo) usagePointInfo).pressure = ((WaterDetail) detail.get()).getPressure();
                } else if (HeatDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((HeatTechnicalInfo) usagePointInfo).pressure = ((HeatDetail) detail.get()).getPressure();
                }
            }
        });
        map.put("physicalCapacity", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent()) {
                if (GasDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((GasTechnicalInfo) usagePointInfo).physicalCapacity = ((GasDetail) detail.get()).getPhysicalCapacity();
                } else if (WaterDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((WaterTechnicalInfo) usagePointInfo).physicalCapacity = ((WaterDetail) detail.get()).getPhysicalCapacity();
                } else if (HeatDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((HeatTechnicalInfo) usagePointInfo).physicalCapacity = ((HeatDetail) detail.get()).getPhysicalCapacity();
                }
            }
        });
        map.put("limiter", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent()) {
                if (GasDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((GasTechnicalInfo) usagePointInfo).limiter = ((GasDetail) detail.get()).isLimiter();
                } else if (ElectricityDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((ElectricityTechnicalInfo) usagePointInfo).limiter = ((ElectricityDetail) detail.get()).isLimiter();
                } else if (WaterDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((WaterTechnicalInfo) usagePointInfo).limiter = ((WaterDetail) detail.get()).isLimiter();
                }
            }
        });
        map.put("loadLimiterType", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent()) {
                if (GasDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((GasTechnicalInfo) usagePointInfo).loadLimiterType = ((GasDetail) detail.get()).getLoadLimiterType();
                } else if (ElectricityDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((ElectricityTechnicalInfo) usagePointInfo).loadLimiterType = ((ElectricityDetail) detail.get()).getLoadLimiterType();
                } else if (WaterDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((WaterTechnicalInfo) usagePointInfo).loadLimiterType = ((WaterDetail) detail.get()).getLoadLimiterType();
                }
            }
        });
        map.put("loadLimit", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent()) {
                if (GasDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((GasTechnicalInfo) usagePointInfo).loadLimit = ((GasDetail) detail.get()).getLoadLimit();
                } else if (ElectricityDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((ElectricityTechnicalInfo) usagePointInfo).loadLimit = ((ElectricityDetail) detail.get()).getLoadLimit();
                } else if (WaterDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((WaterTechnicalInfo) usagePointInfo).loadLimit = ((WaterDetail) detail.get()).getLoadLimit();
                }
            }
        });
        map.put("bypass", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent()) {
                if (GasDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((GasTechnicalInfo) usagePointInfo).bypass = ((GasDetail) detail.get()).isBypassInstalled();
                } else if (WaterDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((WaterTechnicalInfo) usagePointInfo).bypass = ((WaterDetail) detail.get()).isBypassInstalled();
                } else if (HeatDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((HeatTechnicalInfo) usagePointInfo).bypass = ((HeatDetail) detail.get()).isBypassInstalled();
                }
            }
        });
        map.put("bypassStatus", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent()) {
                if (GasDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((GasTechnicalInfo) usagePointInfo).bypassStatus = ((GasDetail) detail.get()).getBypassStatus();
                } else if (WaterDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((WaterTechnicalInfo) usagePointInfo).bypassStatus = ((WaterDetail) detail.get()).getBypassStatus();
                } else if (HeatDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((HeatTechnicalInfo) usagePointInfo).bypassStatus = ((HeatDetail) detail.get()).getBypassStatus();
                }
            }
        });
        map.put("valve", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent()) {
                if (GasDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((GasTechnicalInfo) usagePointInfo).valve = ((GasDetail) detail.get()).isValveInstalled();
                } else if (WaterDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((WaterTechnicalInfo) usagePointInfo).valve = ((WaterDetail) detail.get()).isValveInstalled();
                } else if (HeatDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((HeatTechnicalInfo) usagePointInfo).valve = ((HeatDetail) detail.get()).isValveInstalled();
                }
            }
        });
        map.put("capped", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent()) {
                if (GasDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((GasTechnicalInfo) usagePointInfo).capped = ((GasDetail) detail.get()).isCapped();
                } else if (WaterDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((WaterTechnicalInfo) usagePointInfo).capped = ((WaterDetail) detail.get()).isCapped();
                }
            }
        });
        map.put("clamped", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent()) {
                if (GasDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((GasTechnicalInfo) usagePointInfo).clamped = ((GasDetail) detail.get()).isClamped();
                } else if (WaterDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((WaterTechnicalInfo) usagePointInfo).clamped = ((WaterDetail) detail.get()).isClamped();
                }
            }
        });
        map.put("interruptible", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent()) {
                if (GasDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((GasTechnicalInfo) usagePointInfo).interruptible = ((GasDetail) detail.get()).isInterruptible();
                } else if (ElectricityDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((ElectricityTechnicalInfo) usagePointInfo).interruptible = ((ElectricityDetail) detail.get()).isInterruptible();
                }
            }
        });
        map.put("nominalServiceVoltage", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent() && ElectricityDetail.class.isAssignableFrom(detail.get().getClass())) {
                ((ElectricityTechnicalInfo) usagePointInfo).nominalServiceVoltage = ((ElectricityDetail) detail.get()).getNominalServiceVoltage();
            }
        });
        map.put("phaseCode", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent() && ElectricityDetail.class.isAssignableFrom(detail.get().getClass())) {
                ((ElectricityTechnicalInfo) usagePointInfo).phaseCode = ((ElectricityDetail) detail.get()).getPhaseCode();
            }
        });
        map.put("ratedCurrent", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent() && ElectricityDetail.class.isAssignableFrom(detail.get().getClass())) {
                ((ElectricityTechnicalInfo) usagePointInfo).ratedCurrent = ((ElectricityDetail) detail.get()).getRatedCurrent();
            }
        });
        map.put("ratedPower", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent() && ElectricityDetail.class.isAssignableFrom(detail.get().getClass())) {
                ((ElectricityTechnicalInfo) usagePointInfo).ratedPower = ((ElectricityDetail) detail.get()).getRatedPower();
            }
        });
        map.put("estimatedLoad", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent() && ElectricityDetail.class.isAssignableFrom(detail.get().getClass())) {
                ((ElectricityTechnicalInfo) usagePointInfo).estimatedLoad = ((ElectricityDetail) detail.get()).getEstimatedLoad();
            }
        });

//        map.put("serviceCategory", ((usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.serviceCategory = serviceCategoryInfoFactory.asLink(usagePoint.getServiceCategory(), Relation.REF_RELATION, uriInfo)));
//
//        map.put("meterActivations", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.meterActivations = meterActivationInfoFactory.asLink(usagePoint.getMeterActivations(), Relation.REL_RELATION, uriInfo));
//        map.put("accountabilities", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.accountabilities = accountabilitieInfoFactory.asLink(usagePoint.getAccountabilities(), Relation.REL_RELATION, uriInfo));
//        map.put("usagePointConfigurations", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.usagePointConfigurations = usagePointConfigurationInfoFactory.asLink(usagePoint.getUsagePointConfigurations(), Relation.REL_RELATION, uriInfo));
        return map;
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
        UsagePointDetailBuilder builder = usagePointInfo.createDetail(usagePoint, clock);
        builder.validate();
        builder.create();
        return usagePoint;


    }
}
