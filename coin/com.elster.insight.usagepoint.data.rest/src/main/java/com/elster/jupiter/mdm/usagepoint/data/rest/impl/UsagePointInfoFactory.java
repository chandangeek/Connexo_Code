package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.issue.share.IssueFilter;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.mdm.usagepoint.config.rest.MetrologyConfigurationInfoFactory;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationBuilder;
import com.elster.jupiter.metering.LocationService;
import com.elster.jupiter.metering.LocationTemplate;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.WaterDetail;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.rest.util.PropertyDescriptionInfo;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.geo.SpatialCoordinates;
import com.elster.jupiter.util.geo.SpatialCoordinatesFactory;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component(name = "insight.usagepoint.info.factory", service = {InfoFactory.class}, immediate = true)
public class UsagePointInfoFactory implements InfoFactory<UsagePoint> {

    private volatile Clock clock;
    private volatile Thesaurus thesaurus;
    private volatile MeteringService meteringService;
    private volatile LocationService locationService;
    private volatile ServiceCallService serviceCallService;
    private volatile BpmService bpmService;
    private volatile IssueService issueService;
    private volatile CustomPropertySetInfoFactory customPropertySetInfoFactory;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile License license;
    private volatile MetrologyConfigurationInfoFactory metrologyConfigurationInfoFactory;
    private volatile LicenseService licenseService;

    public UsagePointInfoFactory() {
    }

    @Inject
    public UsagePointInfoFactory(Clock clock,
                                 NlsService nlsService,
                                 MeteringService meteringService,
                                 ServiceCallService serviceCallService,
                                 BpmService bpmService,
                                 IssueService issueService,
                                 ThreadPrincipalService threadPrincipalService,
                                 LocationService locationService,
                                 MetrologyConfigurationInfoFactory metrologyConfigurationInfoFactory,
                                 LicenseService licenseService) {
        this();
        this.setClock(clock);
        this.setNlsService(nlsService);
        this.setMeteringService(meteringService);
        this.setLocationService(locationService);
        this.setThreadPrincipalService(threadPrincipalService);
        this.setServiceCallService(serviceCallService);
        this.setBpmService(bpmService);
        this.setIssueService(issueService);
        this.setLicenseService(licenseService);
        this.metrologyConfigurationInfoFactory = metrologyConfigurationInfoFactory;
        activate();
    }

    @Activate
    public void activate() {
        customPropertySetInfoFactory = new CustomPropertySetInfoFactory(thesaurus, clock);
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(UsagePointApplication.COMPONENT_NAME, Layer.REST);
    }

    @Reference
    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    @Reference
    public void setLicenseService(LicenseService licenseService) {
        this.licenseService = licenseService;
    }


    @Override
    // for search only - so only populate fields that will be used/shown !!!
    public UsagePointSearchInfo from(UsagePoint usagePoint) {
        UsagePointSearchInfo info = new UsagePointSearchInfo();
        info.id = usagePoint.getId();
        info.mRID = usagePoint.getMRID();
        info.displayServiceCategory = usagePoint.getServiceCategory().getDisplayName();
        info.displayMetrologyConfiguration = usagePoint.getCurrentEffectiveMetrologyConfiguration().map(mc -> mc.getMetrologyConfiguration().getName()).orElse(null);
        info.displayType = this.getUsagePointDisplayType(usagePoint);
        info.displayConnectionState = usagePoint.getConnectionState().getName();
        info.geoCoordinates = usagePoint.getSpatialCoordinates().map(SpatialCoordinates::toString).orElse(null);
        info.location = usagePoint.getLocation().map(Location::toString).orElse(null);
        return info;
    }

    public UsagePointInfo fullInfoFrom(UsagePoint usagePoint) {
        UsagePointInfo info = new UsagePointInfo();
        info.id = usagePoint.getId();
        info.mRID = usagePoint.getMRID();
        info.serviceLocationId = usagePoint.getServiceLocation().map(ServiceLocation::getId).orElse(0L);
        info.name = usagePoint.getName();
        info.isSdp = usagePoint.isSdp();
        info.isVirtual = usagePoint.isVirtual();
        info.outageRegion = usagePoint.getOutageRegion();
        info.readRoute = usagePoint.getReadRoute();
        info.servicePriority = usagePoint.getServicePriority();
        info.serviceDeliveryRemark = usagePoint.getServiceDeliveryRemark();
        info.installationTime = usagePoint.getInstallationTime().toEpochMilli();
        info.version = usagePoint.getVersion();
        info.createTime = usagePoint.getCreateDate().toEpochMilli();
        info.modTime = usagePoint.getModificationDate().toEpochMilli();
        info.connectionState = new IdWithNameInfo(usagePoint.getConnectionState().getId(), thesaurus.getFormat(ConnectionStateTranslationKeys
                .getTranslatedKeys(usagePoint.getConnectionState())).format());
        info.displayConnectionState = usagePoint.getConnectionState().getName();
        info.displayServiceCategory = usagePoint.getServiceCategory().getDisplayName();
        info.displayType = this.getUsagePointDisplayType(usagePoint);

        usagePoint.getCurrentEffectiveMetrologyConfiguration()
                .map(EffectiveMetrologyConfigurationOnUsagePoint::getMetrologyConfiguration)
                .ifPresent(mc -> {
                    info.metrologyConfiguration = new MetrologyConfigurationInfo(mc, usagePoint, this.thesaurus);
                    info.displayMetrologyConfiguration = mc.getName();
                    info.metrologyConfiguration.metrologyContracts = metrologyConfigurationInfoFactory.getMetrologyContractsInfo(mc);
                });

        addDetailsInfo(info, usagePoint);
        addCustomPropertySetInfo(info, usagePoint);
        addLocationInfo(info, usagePoint);
        return info;
    }

    private void addDetailsInfo(UsagePointInfo info, UsagePoint usagePoint) {

        Optional<? extends UsagePointDetail> detailHolder = usagePoint.getDetail(clock.instant());
        if (detailHolder.isPresent()) {
            if (detailHolder.get() instanceof ElectricityDetail) {
                info.techInfo = new ElectricityUsagePointDetailsInfo((ElectricityDetail) detailHolder.get());
            } else if (detailHolder.get() instanceof GasDetail) {
                info.techInfo = new GasUsagePointDetailsInfo((GasDetail) detailHolder.get());
            } else if (detailHolder.get() instanceof WaterDetail) {
                info.techInfo = new WaterUsagePointDetailsInfo((WaterDetail) detailHolder.get());
            } else if (detailHolder.get() instanceof HeatDetail) {
                info.techInfo = new HeatUsagePointDetailsInfo((HeatDetail) detailHolder.get());
            }
        }
    }

    private void addCustomPropertySetInfo(UsagePointInfo info, UsagePoint usagePoint) {
        UsagePointCustomPropertySetExtension customPropertySetExtension = usagePoint.forCustomProperties();
        info.customPropertySets = customPropertySetExtension.getAllPropertySets()
                .stream()
                .map(rcps -> customPropertySetInfoFactory.getFullInfo(rcps, rcps.getValues()))
                .collect(Collectors.toList());
        info.customPropertySets.sort((cas1, cas2) -> cas1.name.compareTo(cas2.name));
    }

    private void addLocationInfo(UsagePointInfo info, UsagePoint usagePoint) {
        info.extendedGeoCoordinates = new CoordinatesInfo(meteringService, usagePoint.getMRID());
        info.extendedLocation = new LocationInfo(meteringService, locationService, thesaurus, usagePoint.getMRID());

        info.geoCoordinates = info.extendedGeoCoordinates.coordinatesDisplay;
        info.location = info.extendedLocation.locationValue;
    }

    @Override
    public List<PropertyDescriptionInfo> modelStructure() {
        List<PropertyDescriptionInfo> propertyDescriptionInfoList = new ArrayList<>();
        propertyDescriptionInfoList.add(this.createDescription(UsagePointModelTranslationKeys.MRID_MODEL, String.class));
        propertyDescriptionInfoList.add(this.createDescription(UsagePointModelTranslationKeys.SERVICE_CATEGORY_MODEL, String.class));
        propertyDescriptionInfoList.add(this.createDescription(UsagePointModelTranslationKeys.METROLOGY_CONFIGURATION_MODEL, String.class));
        propertyDescriptionInfoList.add(this.createDescription(UsagePointModelTranslationKeys.TYPE_MODEL, String.class));
        propertyDescriptionInfoList.add(this.createDescription(UsagePointModelTranslationKeys.CONNECTION_STATE_MODEL, String.class));
        propertyDescriptionInfoList.add(this.createDescription(UsagePointModelTranslationKeys.LOCATION_MODEL, String.class));
        return propertyDescriptionInfoList;
    }

    @Override
    public Class getDomainClass() {
        if (!licenseService.getLicenseForApplication("INS").isPresent()) {
            return EmptyDomain.class;
        }
        return UsagePoint.class;
    }

    private PropertyDescriptionInfo createDescription(UsagePointModelTranslationKeys propertyName, Class<?> aClass) {
        return new PropertyDescriptionInfo(propertyName.getKey(), aClass, thesaurus.getString(propertyName.getKey(), propertyName
                .getDefaultFormat()));
    }

    private String getUsagePointDisplayType(UsagePoint usagePoint) {
        if (usagePoint.isSdp() && usagePoint.isVirtual()) {
            return "Unmeasured SDP";
        }
        if (!usagePoint.isSdp() && usagePoint.isVirtual()) {
            return "Unmeasured non-SDP";
        }
        if (usagePoint.isSdp() && !usagePoint.isVirtual()) {
            return "Measured SDP";
        }
        if (!usagePoint.isSdp() && !usagePoint.isVirtual()) {
            return "Measured non-SDP";
        }

        return null;
    }

    public UsagePointBuilder newUsagePointBuilder(UsagePointInfo usagePointInfo) {
        UsagePointBuilder usagePointBuilder = meteringService.getServiceCategory(ServiceKind.valueOf(usagePointInfo.serviceCategory))
                .orElseThrow(IllegalArgumentException::new)
                .newUsagePoint(
                        usagePointInfo.mRID,
                        usagePointInfo.installationTime != null ? Instant.ofEpochMilli(usagePointInfo.installationTime) : clock.instant())
                .withName(usagePointInfo.name)
                .withIsSdp(usagePointInfo.isSdp)
                .withIsVirtual(usagePointInfo.isVirtual)
                .withReadRoute(usagePointInfo.readRoute)
                .withServicePriority(usagePointInfo.servicePriority)
                .withServiceDeliveryRemark(usagePointInfo.serviceDeliveryRemark)
                .withServiceLocationString(usagePointInfo.extendedLocation.unformattedLocationValue);

        SpatialCoordinates geoCoordinates = getGeoCoordinates(usagePointInfo);
        if (geoCoordinates != null) {
            usagePointBuilder.withGeoCoordinates(geoCoordinates);
        }

        Location location = getLocation(usagePointInfo);
        if (location != null) {
            usagePointBuilder.withLocation(location);
        }
        return usagePointBuilder;
    }

    SpatialCoordinates getGeoCoordinates(UsagePointInfo usagePointInfo) {
        if ((usagePointInfo.extendedGeoCoordinates != null) && (usagePointInfo.extendedGeoCoordinates.spatialCoordinates != null)) {
            return new SpatialCoordinatesFactory().fromStringValue(usagePointInfo.extendedGeoCoordinates.spatialCoordinates);
        }
        return null;
    }

    public Location getLocation(UsagePointInfo usagePointInfo) {
        if ((usagePointInfo.extendedLocation.locationId != null) && (usagePointInfo.extendedLocation.locationId == -1)
                && (usagePointInfo.extendedLocation.properties != null) && (usagePointInfo.extendedLocation.properties.length > 0)) {
            List<PropertyInfo> propertyInfoList = Arrays.asList(usagePointInfo.extendedLocation.properties);
            List<String> locationData = propertyInfoList.stream()
                    .map(d -> d.propertyValueInfo.value.toString())
                    .collect(Collectors.toList());
            LocationBuilder builder = meteringService.getServiceCategory(ServiceKind.valueOf(usagePointInfo.serviceCategory))
                    .orElseThrow(IllegalArgumentException::new)
                    .newUsagePoint(
                            usagePointInfo.mRID,
                            usagePointInfo.installationTime != null ? Instant.ofEpochMilli(usagePointInfo.installationTime) : clock.instant()).newLocationBuilder();
            Map<String, Integer> ranking = meteringService
                    .getLocationTemplate()
                    .getTemplateMembers()
                    .stream()
                    .collect(Collectors.toMap(LocationTemplate.TemplateField::getName,
                            LocationTemplate.TemplateField::getRanking));
            Optional<LocationBuilder.LocationMemberBuilder> memberBuilder = builder.getMemberBuilder(threadPrincipalService.getLocale().getLanguage());
            if (memberBuilder.isPresent()) {
                setLocationAttributes(memberBuilder.get(), locationData, ranking);
            } else {
                setLocationAttributes(builder.member(), locationData, ranking).add();
            }
            return builder.create();
        } else if ((usagePointInfo.extendedLocation.locationId != null) && (usagePointInfo.extendedLocation.locationId > 0)) {
            return locationService.findLocationById(usagePointInfo.extendedLocation.locationId).get();
        }
        return null;
    }

    private LocationBuilder.LocationMemberBuilder setLocationAttributes(LocationBuilder.LocationMemberBuilder builder, List<String> locationData, Map<String, Integer> ranking) {
        builder.setCountryCode(locationData.get(ranking.get("countryCode")))
                .setCountryName(locationData.get(ranking.get("countryName")))
                .setAdministrativeArea(locationData.get(ranking.get("administrativeArea")))
                .setLocality(locationData.get(ranking.get("locality")))
                .setSubLocality(locationData.get(ranking.get("subLocality")))
                .setStreetType(locationData.get(ranking.get("streetType")))
                .setStreetName(locationData.get(ranking.get("streetName")))
                .setStreetNumber(locationData.get(ranking.get("streetNumber")))
                .setEstablishmentType(locationData.get(ranking.get("establishmentType")))
                .setEstablishmentName(locationData.get(ranking.get("establishmentName")))
                .setEstablishmentNumber(locationData.get(ranking.get("establishmentNumber")))
                .setAddressDetail(locationData.get(ranking.get("addressDetail")))
                .setZipCode(locationData.get(ranking.get("zipCode")))
                .isDaultLocation(true)
                .setLocale(threadPrincipalService.getLocale().getLanguage());

        return builder;
    }


    public List<MeterActivationInfo> getMetersOnUsagePointInfo(UsagePoint usagePoint, String authorization) {
        Map<MeterRole, MeterRoleInfo> mandatoryMeterRoles = new LinkedHashMap<>();
        usagePoint.getCurrentEffectiveMetrologyConfiguration()
                .map(EffectiveMetrologyConfigurationOnUsagePoint::getMetrologyConfiguration)
                .ifPresent(metrologyConfiguration -> metrologyConfiguration.getMeterRoles()
                        .forEach(meterRole -> mandatoryMeterRoles.put(meterRole, new MeterRoleInfo(meterRole))));

        Map<MeterRole, MeterActivation> meterRoleToMeterInfoMapping = usagePoint.getMeterActivations(usagePoint.getInstallationTime())
                .stream()
                .filter(meterActivation -> meterActivation.getMeterRole().isPresent() && meterActivation.getMeter().isPresent())
                .collect(Collectors.toMap(meterActivation -> meterActivation.getMeterRole().get(), Function.identity()));

        return mandatoryMeterRoles.entrySet()
                .stream()
                .map(meterRoleEntry -> {
                    MeterActivationInfo meterActivationInfo = new MeterActivationInfo();
                    meterActivationInfo.meterRole = meterRoleEntry.getValue();
                    MeterActivation meterActivationForMeterRole = meterRoleToMeterInfoMapping.get(meterRoleEntry.getKey());
                    if (meterActivationForMeterRole != null) {
                        Meter meter = meterActivationForMeterRole.getMeter().get();
                        meterActivationInfo.meter = new MeterInfo();
                        meterActivationInfo.meter.id = meter.getId();
                        meterActivationInfo.meter.mRID = meter.getMRID();
                        meterActivationInfo.meter.name = meter.getName();
                        meterActivationInfo.meter.version = meter.getVersion();
                        meterActivationInfo.meter.watsGoingOnMeterStatus = getWatsGoingOnMeterStatus(meter, authorization);
                        meterActivationInfo.meterRole.activationTime = meterActivationForMeterRole.getStart();
                        meterActivationInfo.id = meterActivationForMeterRole.getId();
                        meterActivationInfo.meter.url = meter.getHeadEndInterface()
                                .map(he -> he.getURLForEndDevice(meter)
                                        .map(URL::toString)
                                        .orElse(null))
                                .orElse(null);
                    }
                    return meterActivationInfo;
                })
                .collect(Collectors.toList());
    }

    public WatsGoingOnMeterStatusInfo getWatsGoingOnMeterStatus(Meter meter, String authorization) {
        WatsGoingOnMeterStatusInfo info = new WatsGoingOnMeterStatusInfo();
        IssueFilter issueFilter = issueService.newIssueFilter();
        issueFilter.addDevice(meter);
        issueFilter.addStatus(issueService.findStatus(IssueStatus.OPEN).get());
        info.openIssues = issueService.findIssues(issueFilter).find().size();
        info.ongoingServiceCalls = serviceCallService.findServiceCalls(meter, EnumSet.of(DefaultState.ONGOING)).size();
        info.ongoingProcesses = bpmService.getRunningProcesses(authorization, filterFor(meter)).total;
        return info;
    }

    private String filterFor(Meter meter) {
        return "?variableid=deviceId&variablevalue=" + meter.getMRID();
    }

    static class EmptyDomain {
    }
}