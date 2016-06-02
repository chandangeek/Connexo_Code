package com.elster.jupiter.kore.api.impl;

import com.elster.jupiter.kore.api.impl.utils.MessageSeeds;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.LocationMember;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.metering.WaterDetail;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.hypermedia.LinkInfo;
import com.elster.jupiter.rest.util.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.util.hypermedia.Relation;
import com.elster.jupiter.rest.util.hypermedia.SelectableFieldFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.time.Clock;
import java.time.Instant;
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
    private final Provider<MetrologyConfigurationInfoFactory> metrologyConfigurationInfoFactory;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final Provider<MeterActivationInfoFactory> meterActivationInfoFactory;

    @Inject
    public UsagePointInfoFactory(Clock clock, MeteringService meteringService, ExceptionFactory exceptionFactory,
                                 Provider<MetrologyConfigurationInfoFactory> metrologyConfigurationInfoFactory,
                                 MetrologyConfigurationService metrologyConfigurationService,
                                 Provider<MeterActivationInfoFactory> meterActivationInfoFactory) {
        this.clock = clock;
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
        this.metrologyConfigurationInfoFactory = metrologyConfigurationInfoFactory;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.meterActivationInfoFactory = meterActivationInfoFactory;
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
        switch (usagePoint.getServiceCategory().getKind()) {
            case GAS:
                return new GasUsagePointInfo();
            case HEAT:
                return new HeatUsagePointInfo();
            case WATER:
                return new WaterUsagePointInfo();
            default:
                throw exceptionFactory.newException(MessageSeeds.UNSUPPORTED_TYPE);
        }
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
        map.put("serviceLocation", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.serviceLocation = usagePoint.getServiceLocationString());
        map.put("mrid", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.mrid = usagePoint.getMRID());
        map.put("outageRegion", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.outageRegion = usagePoint.getOutageRegion());
        map.put("readRoute", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.readRoute = usagePoint.getReadRoute());
        map.put("servicePriority", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.servicePriority = usagePoint.getServicePriority());
//        map.put("serviceKind", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.serviceKind = usagePoint.getServiceCategory()
//                .getKind().getDisplayName());
        map.put("installationTime", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.installationTime = usagePoint
                .getInstallationTime());
        map.put("serviceDeliveryRemark", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.serviceDeliveryRemark = usagePoint
                .getServiceDeliveryRemark());

        map.put("grounded", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent()) {
                if (GasDetail.class.isAssignableFrom(detail.get().getClass())) {
                    usagePointInfo.grounded = ((GasDetail) detail.get()).isGrounded();
                } else if (WaterDetail.class.isAssignableFrom(detail.get().getClass())) {
                    usagePointInfo.grounded = ((WaterDetail) detail.get()).isGrounded();
                } else if (ElectricityDetail.class.isAssignableFrom(detail.get().getClass())) {
                    usagePointInfo.grounded = ((ElectricityDetail) detail.get()).isGrounded();
                }
            }
        });
        map.put("collar", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent()) {
                usagePointInfo.collar = detail.get().isCollarInstalled();
            }
        });
        map.put("pressure", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent()) {
                if (GasDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((GasUsagePointInfo) usagePointInfo).pressure = ((GasDetail) detail.get()).getPressure();
                } else if (WaterDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((WaterUsagePointInfo) usagePointInfo).pressure = ((WaterDetail) detail.get()).getPressure();
                } else if (HeatDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((HeatUsagePointInfo) usagePointInfo).pressure = ((HeatDetail) detail.get()).getPressure();
                }
            }
        });
        map.put("physicalCapacity", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent()) {
                if (GasDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((GasUsagePointInfo) usagePointInfo).physicalCapacity = ((GasDetail) detail.get()).getPhysicalCapacity();
                } else if (WaterDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((WaterUsagePointInfo) usagePointInfo).physicalCapacity = ((WaterDetail) detail.get()).getPhysicalCapacity();
                } else if (HeatDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((HeatUsagePointInfo) usagePointInfo).physicalCapacity = ((HeatDetail) detail.get()).getPhysicalCapacity();
                }
            }
        });
        map.put("limiter", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent()) {
                if (GasDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((GasUsagePointInfo) usagePointInfo).limiter = ((GasDetail) detail.get()).isLimiter();
                } else if (WaterDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((WaterUsagePointInfo) usagePointInfo).limiter = ((WaterDetail) detail.get()).isLimiter();
                }
            }
        });
        map.put("loadLimiterType", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent()) {
                if (GasDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((GasUsagePointInfo) usagePointInfo).loadLimiterType = ((GasDetail) detail.get()).getLoadLimiterType();
                } else if (WaterDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((WaterUsagePointInfo) usagePointInfo).loadLimiterType = ((WaterDetail) detail.get()).getLoadLimiterType();
                }
            }
        });
        map.put("loadLimit", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent()) {
                if (GasDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((GasUsagePointInfo) usagePointInfo).loadLimit = ((GasDetail) detail.get()).getLoadLimit();
                } else if (WaterDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((WaterUsagePointInfo) usagePointInfo).loadLimit = ((WaterDetail) detail.get()).getLoadLimit();
                }
            }
        });
        map.put("bypass", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent()) {
                if (GasDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((GasUsagePointInfo) usagePointInfo).bypass = ((GasDetail) detail.get()).isBypassInstalled();
                } else if (WaterDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((WaterUsagePointInfo) usagePointInfo).bypass = ((WaterDetail) detail.get()).isBypassInstalled();
                } else if (HeatDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((HeatUsagePointInfo) usagePointInfo).bypass = ((HeatDetail) detail.get()).isBypassInstalled();
                }
            }
        });
        map.put("bypassStatus", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent()) {
                if (GasDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((GasUsagePointInfo) usagePointInfo).bypassStatus = ((GasDetail) detail.get()).getBypassStatus();
                } else if (WaterDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((WaterUsagePointInfo) usagePointInfo).bypassStatus = ((WaterDetail) detail.get()).getBypassStatus();
                } else if (HeatDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((HeatUsagePointInfo) usagePointInfo).bypassStatus = ((HeatDetail) detail.get()).getBypassStatus();
                }
            }
        });
        map.put("valve", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent()) {
                if (GasDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((GasUsagePointInfo) usagePointInfo).valve = ((GasDetail) detail.get()).isValveInstalled();
                } else if (WaterDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((WaterUsagePointInfo) usagePointInfo).valve = ((WaterDetail) detail.get()).isValveInstalled();
                } else if (HeatDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((HeatUsagePointInfo) usagePointInfo).valve = ((HeatDetail) detail.get()).isValveInstalled();
                }
            }
        });
        map.put("capped", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent()) {
                if (GasDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((GasUsagePointInfo) usagePointInfo).capped = ((GasDetail) detail.get()).isCapped();
                } else if (WaterDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((WaterUsagePointInfo) usagePointInfo).capped = ((WaterDetail) detail.get()).isCapped();
                }
            }
        });
        map.put("clamped", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent()) {
                if (GasDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((GasUsagePointInfo) usagePointInfo).clamped = ((GasDetail) detail.get()).isClamped();
                } else if (WaterDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((WaterUsagePointInfo) usagePointInfo).clamped = ((WaterDetail) detail.get()).isClamped();
                }
            }
        });
        map.put("interruptible", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(clock.instant());
            if (detail.isPresent()) {
                if (GasDetail.class.isAssignableFrom(detail.get().getClass())) {
                    ((GasUsagePointInfo) usagePointInfo).interruptible = ((GasDetail) detail.get()).isInterruptible();
                }
            }
        });
        map.put("metrologyConfiguration", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<MetrologyConfiguration> metrologyConfiguration = usagePoint.getMetrologyConfiguration();
            metrologyConfiguration.ifPresent(mc -> usagePointInfo.metrologyConfiguration = metrologyConfigurationInfoFactory
                    .get()
                    .asLink(mc, Relation.REF_RELATION, uriInfo));
        });

//        map.put("serviceCategory", ((usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.serviceCategory = serviceCategoryInfoFactory.asLink(usagePoint.getServiceCategory(), Relation.REF_RELATION, uriInfo)));
//
        map.put("meterActivations", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.meterActivations = meterActivationInfoFactory
                .get()
                .asLink(usagePoint.getMeterActivations(), Relation.REF_RELATION, uriInfo));
        map.put("locations", (usagePointInfo, usagePoint, uriInfo) -> {
            usagePoint.getLocation()
                    .ifPresent(l -> usagePointInfo.locations = l.getMembers()
                            .stream()
                            .map(this::newLocationInfo)
                            .collect(toList()));
        });
//        map.put("accountabilities", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.accountabilities = accountabilitieInfoFactory.asLink(usagePoint.getAccountabilities(), Relation.REL_RELATION, uriInfo));
//        map.put("usagePointConfigurations", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.usagePointConfigurations = usagePointConfigurationInfoFactory.asLink(usagePoint.getUsagePointConfigurations(), Relation.REL_RELATION, uriInfo));
        return map;
    }

    private LocationInfo newLocationInfo(LocationMember locationMember) {
        LocationInfo info = new LocationInfo();
        info.locationId = locationMember.getLocationId();
        info.addressDetail = locationMember.getAddressDetail();
        info.administrativeArea = locationMember.getAdministrativeArea();
        info.countryCode = locationMember.getCountryCode();
        info.countryName = locationMember.getCountryName();
        info.defaultLocation = locationMember.isDefaultLocation();
        info.establishmentName = locationMember.getEstablishmentName();
        info.establishmentNumber = locationMember.getEstablishmentNumber();
        info.establishmentType = locationMember.getEstablishmentType();
        info.locale = locationMember.getLocale();
        info.locality = locationMember.getLocality();
        info.streetName = locationMember.getStreetName();
        info.streetNumber = locationMember.getStreetNumber();
        info.streetType = locationMember.getStreetType();
        info.subLocality = locationMember.getSubLocality();
        info.zipCode = locationMember.getZipCode();
        return info;
    }

    public UsagePoint createUsagePoint(UsagePointInfo usagePointInfo) {
        UsagePoint usagePoint = meteringService.getServiceCategory(usagePointInfo.getServiceKind())
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_SERVICE_CATEGORY))
                .newUsagePoint(usagePointInfo.mrid, usagePointInfo.installationTime)
                .withAliasName(usagePointInfo.aliasName)
                .withDescription(usagePointInfo.description)
                .withName(usagePointInfo.name)
                .withOutageRegion(usagePointInfo.outageRegion)
                .withReadRoute(usagePointInfo.readRoute)
                .withServiceDeliveryRemark(usagePointInfo.serviceDeliveryRemark)
                .withServiceLocationString(usagePointInfo.serviceLocation)
                .withServicePriority(usagePointInfo.servicePriority)
                .create();
        Instant now = clock.instant();
        if (usagePointInfo.metrologyConfiguration != null && usagePointInfo.metrologyConfiguration.id != null) {
            Optional<MetrologyConfiguration> metrologyConfiguration = metrologyConfigurationService.findMetrologyConfiguration(usagePointInfo.metrologyConfiguration.id);
            if (!metrologyConfiguration.isPresent()) {
                throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_METROLOGY_CONFIGURATION);
            }
            usagePoint.apply(metrologyConfiguration.get(), now);
        }
        UsagePointDetailBuilder builder = usagePointInfo.createDetail(usagePoint, now);
        builder.validate();
        builder.create();
        return usagePoint;
    }

    public void updateUsagePoint(UsagePoint usagePoint, UsagePointInfo usagePointInfo) {
        usagePoint.setName(usagePointInfo.name);
        usagePoint.setAliasName(usagePointInfo.aliasName);
        usagePoint.setDescription(usagePointInfo.description);
        usagePoint.setInstallationTime(usagePointInfo.installationTime);
        usagePoint.setMRID(usagePointInfo.mrid);
        usagePoint.setOutageRegion(usagePointInfo.outageRegion);
        usagePoint.setReadRoute(usagePointInfo.readRoute);
        usagePoint.setServiceDeliveryRemark(usagePointInfo.serviceDeliveryRemark);
        usagePoint.setServicePriority(usagePointInfo.servicePriority);
        usagePoint.update();
        Instant now = clock.instant();
        Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(now);
        if (detail.isPresent()) {
            usagePoint.terminateDetail(detail.get(), now);
            UsagePointDetailBuilder newDetail = usagePointInfo.createDetail(usagePoint, now);
            newDetail.validate();
            newDetail.create();
        }
        if (usagePointInfo.metrologyConfiguration != null && usagePointInfo.metrologyConfiguration.id != null) {
            Optional<MetrologyConfiguration> metrologyConfiguration = metrologyConfigurationService.findMetrologyConfiguration(usagePointInfo.metrologyConfiguration.id);
            if (!metrologyConfiguration.isPresent()) {
                throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_METROLOGY_CONFIGURATION);
            }
            if (usagePoint.getMetrologyConfiguration().isPresent() && usagePoint.getMetrologyConfiguration()
                    .get()
                    .getId() != usagePointInfo.metrologyConfiguration.id) {
                usagePoint.removeMetrologyConfiguration(now);
                usagePoint.apply(metrologyConfiguration.get(), now);
            } else {
                if (!usagePoint.getMetrologyConfiguration().isPresent()) {
                    usagePoint.apply(metrologyConfiguration.get(), now);
                }
            }
        } else {
            if (usagePoint.getMetrologyConfiguration().isPresent()) {
                usagePoint.removeMetrologyConfiguration(now);
            }
        }
    }
}
