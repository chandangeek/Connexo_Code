/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.kore.api.impl.MessageSeeds;
import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.util.geo.SpatialCoordinates;

import com.google.common.collect.Range;

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

    private final MeteringService meteringService;
    private final CustomPropertySetService customPropertySetService;
    private final ExceptionFactory exceptionFactory;
    private final Provider<EffectiveMetrologyConfigurationInfoFactory> effectiveMetrologyConfigurationInfoFactory;
    private final Provider<MeterActivationInfoFactory> meterActivationInfoFactory;
    private final UsagePointCustomPropertySetInfoFactory customPropertySetInfoFactory;
    private final LocationInfoFactory locationInfoFactory;
    private final Clock clock;

    @Inject
    public UsagePointInfoFactory(MeteringService meteringService, CustomPropertySetService customPropertySetService, ExceptionFactory exceptionFactory,
                                 Provider<EffectiveMetrologyConfigurationInfoFactory> effectiveMetrologyConfigurationInfoFactory,
                                 Provider<MeterActivationInfoFactory> meterActivationInfoFactory,
                                 UsagePointCustomPropertySetInfoFactory customPropertySetInfoFactory,
                                 LocationInfoFactory locationInfoFactory, Clock clock) {
        this.meteringService = meteringService;
        this.customPropertySetService = customPropertySetService;
        this.exceptionFactory = exceptionFactory;
        this.effectiveMetrologyConfigurationInfoFactory = effectiveMetrologyConfigurationInfoFactory;
        this.meterActivationInfoFactory = meterActivationInfoFactory;
        this.customPropertySetInfoFactory = customPropertySetInfoFactory;
        this.locationInfoFactory = locationInfoFactory;
        this.clock = clock;
    }

    LinkInfo asLink(UsagePoint usagePoint, Relation relation, UriInfo uriInfo) {
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
                .build(usagePoint.getMRID());
    }

    private Link detailsLink(UsagePointDetail usagePointDetail, Relation relation, UriInfo uriInfo) {
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(UsagePointResource.class)
                .path(UsagePointResource.class, "getDetailsResource")
                .path("/{lowerEnd}"); // I don't want to figure out what the actual resource is, they all have the same URI anyway

        return Link.fromUriBuilder(uriBuilder)
                .rel(relation.rel())
                .title("Usage point")
                .build(usagePointDetail.getUsagePoint().getMRID(), usagePointDetail.getRange()
                        .lowerEndpoint()
                        .toEpochMilli());
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
        map.put("serviceLocation", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.serviceLocation = usagePoint.getServiceLocationString());
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

        map.put("metrologyConfiguration", (usagePointInfo, usagePoint, uriInfo) -> {
            Optional<EffectiveMetrologyConfigurationOnUsagePoint> metrologyConfiguration = usagePoint.getCurrentEffectiveMetrologyConfiguration();
            metrologyConfiguration.ifPresent(mc -> usagePointInfo.metrologyConfiguration = effectiveMetrologyConfigurationInfoFactory
                    .get()
                    .asLink(mc, Relation.REF_RELATION, uriInfo));
        });
        map.put("detail", (usagePointInfo, usagePoint, uriInfo) -> {
            List<? extends UsagePointDetail> details = usagePoint.getDetail(Range.all());
            if (!details.isEmpty()) {
                usagePointInfo.detail = new LinkInfo<>();
                usagePointInfo.detail.link = detailsLink(details.get(details.size() - 1), Relation.REF_RELATION, uriInfo);
                usagePointInfo.detail.id = details.get(details.size() - 1).getRange().lowerEndpoint().toEpochMilli();
            }
        });
        map.put("connectionState", (usagePointInfo, usagePoint, uriInfo) -> {
            usagePointInfo.connectionState = new UsagePointConnectionStateInfo();
            usagePoint.getCurrentConnectionState().ifPresent(connectionState -> {
                usagePointInfo.connectionState.connectionStateId = connectionState.getConnectionState().getId();
                usagePointInfo.connectionState.startDate = connectionState.getRange().lowerEndpoint();
                if (connectionState.getRange().hasUpperBound()) {
                    usagePointInfo.connectionState.endDate = connectionState.getRange().upperEndpoint();
                }
            });
        });
        map.put("meterActivations", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.meterActivations = meterActivationInfoFactory
                .get()
                .asLink(usagePoint.getMeterActivations(), Relation.REF_RELATION, uriInfo));
        map.put("location", (usagePointInfo, usagePoint, uriInfo) ->
                usagePointInfo.location = usagePoint.getLocation().map(locationInfoFactory::asInfo).orElse(null)
        );
        map.put("coordinates", (usagePointInfo, usagePoint, uriInfo) ->
                usagePointInfo.coordinates = usagePoint.getSpatialCoordinates().map(locationInfoFactory::asInfo).orElse(null));
        map.put("isSdp", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.isSdp = usagePoint.isSdp());
        map.put("isVirtual", (usagePointInfo, usagePoint, uriInfo) -> usagePointInfo.isVirtual = usagePoint.isVirtual());
        return map;
    }

    public UsagePoint createUsagePoint(UsagePointInfo usagePointInfo) {
        UsagePointBuilder usagePointBuilder = meteringService.getServiceCategory(usagePointInfo.serviceKind)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_SERVICE_CATEGORY))
                .newUsagePoint(usagePointInfo.name, usagePointInfo.installationTime)
                .withAliasName(usagePointInfo.aliasName)
                .withDescription(usagePointInfo.description)
                .withOutageRegion(usagePointInfo.outageRegion)
                .withReadRoute(usagePointInfo.readRoute)
                .withServiceDeliveryRemark(usagePointInfo.serviceDeliveryRemark)
                .withServiceLocationString(usagePointInfo.serviceLocation)
                .withServicePriority(usagePointInfo.servicePriority)
                .withIsSdp(usagePointInfo.isSdp)
                .withIsVirtual(usagePointInfo.isVirtual);
        Location location = locationInfoFactory.fromInfo(usagePointInfo.location);
        if (location != null) {
            usagePointBuilder.withLocation(location);
        }
        SpatialCoordinates coordinates = locationInfoFactory.fromInfo(usagePointInfo.coordinates);
        if (coordinates != null) {
            usagePointBuilder.withGeoCoordinates(coordinates);
        }
        addCustomPropertySetsValues(usagePointBuilder, usagePointInfo.customPropertySets);
        UsagePoint usagePoint = usagePointBuilder.create();
        createDefaultDetails(usagePoint);
        if (usagePointInfo.connectionState != null && usagePointInfo.connectionState.connectionStateId != null) {
            setConnectionStateFromInfo(usagePoint, usagePointInfo, usagePointInfo.installationTime);
        }
        return usagePoint;
    }

    private void addCustomPropertySetsValues(UsagePointBuilder usagePointBuilder, List<UsagePointCustomPropertySetInfo> propertySetInfos) {
        if (propertySetInfos != null) {
            for (UsagePointCustomPropertySetInfo customPropertySetInfo : propertySetInfos) {
                RegisteredCustomPropertySet registeredCustomPropertySet = customPropertySetService.findActiveCustomPropertySets(UsagePoint.class).stream()
                        .filter(propertySet -> propertySet.getId() == customPropertySetInfo.id)
                        .findAny()
                        .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.CAS_IS_NOT_ATTACHED_TO_USAGE_POINT, customPropertySetInfo.id));
                CustomPropertySet<?, ?> customPropertySet = registeredCustomPropertySet.getCustomPropertySet();
                CustomPropertySetValues values = customPropertySetInfoFactory.getCustomPropertySetValues(customPropertySetInfo, customPropertySet.getPropertySpecs());
                usagePointBuilder.addCustomPropertySetValues(registeredCustomPropertySet, values);
            }
        }
    }

    private void createDefaultDetails(UsagePoint usagePoint) {
        switch (usagePoint.getServiceCategory().getKind()) {
            case ELECTRICITY:
                usagePoint.newElectricityDetailBuilder(usagePoint.getInstallationTime()).create();
                break;
            case GAS:
                usagePoint.newGasDetailBuilder(usagePoint.getInstallationTime()).create();
                break;
            case HEAT:
                usagePoint.newHeatDetailBuilder(usagePoint.getInstallationTime()).create();
                break;
            case WATER:
                usagePoint.newWaterDetailBuilder(usagePoint.getInstallationTime()).create();
                break;
            default:
                throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.UNSUPPORTED_SERVICE_KIND);
        }
    }

    void updateUsagePoint(UsagePoint usagePoint, UsagePointInfo usagePointInfo) {
        usagePoint.setName(usagePointInfo.name);
        usagePoint.setAliasName(usagePointInfo.aliasName);
        usagePoint.setDescription(usagePointInfo.description);
        usagePoint.setOutageRegion(usagePointInfo.outageRegion);
        usagePoint.setReadRoute(usagePointInfo.readRoute);
        usagePoint.setServiceDeliveryRemark(usagePointInfo.serviceDeliveryRemark);
        usagePoint.setServicePriority(usagePointInfo.servicePriority);
        Location location = locationInfoFactory.fromInfo(usagePointInfo.location);
        if (location != null) {
            usagePoint.setLocation(location.getId());
        }
        SpatialCoordinates coordinates = locationInfoFactory.fromInfo(usagePointInfo.coordinates);
        if (coordinates != null) {
            usagePoint.setSpatialCoordinates(coordinates);
        }
        if (usagePointInfo.connectionState != null && usagePointInfo.connectionState.connectionStateId != null) {
            setConnectionStateFromInfo(usagePoint, usagePointInfo, clock.instant());
        }
        usagePoint.update();
    }

    private void setConnectionStateFromInfo(UsagePoint usagePoint, UsagePointInfo usagePointInfo, Instant defaultStartTime) {
        ConnectionState connectionState = findConnectionState(usagePointInfo);
        Instant startTime = usagePointInfo.connectionState.startDate;
        usagePoint.setConnectionState(connectionState, startTime != null ? startTime : defaultStartTime);
    }

    private ConnectionState findConnectionState(UsagePointInfo usagePointInfo) {
        return Arrays.stream(ConnectionState.supportedValues())
                .filter(connectionState -> connectionState.getId().equalsIgnoreCase(usagePointInfo.connectionState.connectionStateId))
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_CONNECTION_STATE));
    }
}
