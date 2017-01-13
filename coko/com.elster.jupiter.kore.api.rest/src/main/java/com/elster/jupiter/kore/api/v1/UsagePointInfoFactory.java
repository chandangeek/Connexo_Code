package com.elster.jupiter.kore.api.v1;

import com.elster.jupiter.kore.api.impl.MessageSeeds;
import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.LocationMember;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;

import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
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
    private final ExceptionFactory exceptionFactory;
    private final Provider<EffectiveMetrologyConfigurationInfoFactory> effectiveMetrologyConfigurationInfoFactory;
    private final Provider<MeterActivationInfoFactory> meterActivationInfoFactory;

    @Inject
    public UsagePointInfoFactory(MeteringService meteringService, ExceptionFactory exceptionFactory,
                                 Provider<EffectiveMetrologyConfigurationInfoFactory> effectiveMetrologyConfigurationInfoFactory,
                                 Provider<MeterActivationInfoFactory> meterActivationInfoFactory) {
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
        this.effectiveMetrologyConfigurationInfoFactory = effectiveMetrologyConfigurationInfoFactory;
        this.meterActivationInfoFactory = meterActivationInfoFactory;
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
        map.put("connectionState", (usagePointInfo, usagePoint, uriInfo) ->
                usagePoint.getCurrentConnectionState().ifPresent(connectionState -> {
                    usagePointInfo.connectionState = new UsagePointConnectionStateInfo();
                    usagePointInfo.connectionState.connectionStateId = connectionState.getId();
                }));
        map.put("meterActivations", (usagePointInfo, usagePoint, uriInfo) ->
                usagePointInfo.meterActivations = meterActivationInfoFactory.get()
                        .asLink(usagePoint.getMeterActivations(), Relation.REF_RELATION, uriInfo));
        map.put("locations", (usagePointInfo, usagePoint, uriInfo) -> {
            usagePoint.getLocation()
                    .ifPresent(l -> usagePointInfo.locations = l.getMembers()
                            .stream()
                            .map(this::newLocationInfo)
                            .collect(toList()));
        });
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
        return meteringService.getServiceCategory(usagePointInfo.serviceKind)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_SERVICE_CATEGORY))
                .newUsagePoint(usagePointInfo.name, usagePointInfo.installationTime)
                .withAliasName(usagePointInfo.aliasName)
                .withDescription(usagePointInfo.description)
                .withOutageRegion(usagePointInfo.outageRegion)
                .withReadRoute(usagePointInfo.readRoute)
                .withServiceDeliveryRemark(usagePointInfo.serviceDeliveryRemark)
                .withServiceLocationString(usagePointInfo.serviceLocation)
                .withServicePriority(usagePointInfo.servicePriority)
                .create();
    }

    void updateUsagePoint(UsagePoint usagePoint, UsagePointInfo usagePointInfo) {
        usagePoint.setName(usagePointInfo.name);
        usagePoint.setAliasName(usagePointInfo.aliasName);
        usagePoint.setDescription(usagePointInfo.description);
        usagePoint.setInstallationTime(usagePointInfo.installationTime);
        usagePoint.setOutageRegion(usagePointInfo.outageRegion);
        usagePoint.setReadRoute(usagePointInfo.readRoute);
        usagePoint.setServiceDeliveryRemark(usagePointInfo.serviceDeliveryRemark);
        usagePoint.setServicePriority(usagePointInfo.servicePriority);

        if (usagePointInfo.connectionState != null && usagePointInfo.connectionState.startDate != null) {
            if (usagePointInfo.connectionState.connectionStateId == null && !usagePoint.getCurrentConnectionState().isPresent()) {
                validateUsagePoint(usagePoint);
            }
            usagePoint.setConnectionState(findConnectionState(usagePointInfo),
                    Instant.ofEpochMilli(usagePointInfo.connectionState.startDate));
        } else if (usagePointInfo.connectionState != null && !usagePoint.getCurrentConnectionState().map(ConnectionState::getId)
                .map(usagePointInfo.connectionState.connectionStateId::equalsIgnoreCase).orElse(false)) {
            usagePoint.setConnectionState(findConnectionState(usagePointInfo));
        }

        usagePoint.update();
    }

    private ConnectionState findConnectionState(UsagePointInfo usagePointInfo) {
        return Arrays.stream(ConnectionState.values())
                .filter(connectionState -> connectionState.getId()
                        .equalsIgnoreCase(usagePointInfo.connectionState.connectionStateId))
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_CONNECTION_STATE));
    }

    private void validateUsagePoint(UsagePoint usagePoint) {
        UsagePointMetrologyConfiguration metrologyConfiguration = usagePoint.getCurrentEffectiveMetrologyConfiguration()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_METROLOGY_CONFIGURATION)).getMetrologyConfiguration();

        metrologyConfiguration.getMeterRoles().stream()
                .filter(meterRole -> usagePoint.getMeterActivations().stream()
                        .anyMatch(meterActivation -> meterActivation.getMeterRole().filter(mr -> mr.equals(meterRole)).isPresent()))
                .findAny()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_METER_ACTIVATION_FOR_METER_ROLE));

        if (metrologyConfiguration.getContracts().stream()
                .anyMatch(metrologyContract -> metrologyContract.isMandatory() && !(metrologyContract.getStatus(usagePoint).getKey().equalsIgnoreCase("COMPLETE")))) {
            throw exceptionFactory.newException(MessageSeeds.METROLOGY_CONTRACTS_INCOMPLETE);
        }
    }
}
