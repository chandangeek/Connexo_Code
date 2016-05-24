package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.license.License;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.WaterDetail;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.rest.util.PropertyDescriptionInfo;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(name = "insight.usagepoint.info.factory", service = {InfoFactory.class}, immediate = true)
public class UsagePointInfoFactory implements InfoFactory<UsagePoint> {

    private volatile Clock clock;
    private volatile Thesaurus thesaurus;
    private volatile MeteringService meteringService;
    private volatile CustomPropertySetInfoFactory customPropertySetInfoFactory;
    private volatile License license;

    public UsagePointInfoFactory() {
    }

    @Inject
    public UsagePointInfoFactory(Clock clock, NlsService nlsService, MeteringService meteringService) {
        this();
        this.setClock(clock);
        this.setNlsService(nlsService);
        this.setMeteringService(meteringService);
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
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(UsagePointApplication.COMPONENT_NAME, Layer.REST);
    }

    @Reference(
            target = "(com.elster.jupiter.license.application.key=INS)",
            cardinality = ReferenceCardinality.OPTIONAL)
    public void setLicense(License license) {
        this.license = license;
    }

    @Override
    public UsagePointInfo from(UsagePoint usagePoint) {
        UsagePointInfo info = new UsagePointInfo();
        info.id = usagePoint.getId();
        info.mRID = usagePoint.getMRID();
        info.serviceLocationId = usagePoint.getServiceLocation().map(ServiceLocation::getId).orElse(0L);
        info.location = usagePoint.getServiceLocationString();
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
        info.connectionState = new IdWithNameInfo(usagePoint.getConnectionState(), thesaurus.getFormat(ConnectionStateTranslationKeys
                .getTranslatedKeys(usagePoint.getConnectionState())).format());
        info.displayConnectionState = usagePoint.getConnectionState().getName();
        info.displayServiceCategory = usagePoint.getServiceCategory().getDisplayName();
        info.displayType = this.getUsagePointDisplayType(usagePoint);
        info.meterActivations = usagePoint.getMeterActivations()
                .stream()
                .map(ma -> new MeterActivationInfo(ma, true))
                .collect(Collectors.toList());


        usagePoint.getMetrologyConfiguration()
                .filter(config -> config instanceof UsagePointMetrologyConfiguration)
                .map(UsagePointMetrologyConfiguration.class::cast)
                .ifPresent(mc -> {
                    info.metrologyConfiguration = new MetrologyConfigurationInfo(mc, usagePoint, this.thesaurus);
                    info.displayMetrologyConfiguration = mc.getName();
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
        meteringService.findUsagePointGeoCoordinates(usagePoint.getMRID())
                .ifPresent(coordinates -> info.geoCoordinates = coordinates.getCoordinates().toString());


        Optional<Location> location = meteringService.findUsagePointLocation(usagePoint.getMRID());
        String formattedLocation = "";
        if (location.isPresent()) {
            List<List<String>> formattedLocationMembers = meteringService.getFormattedLocationMembers(location.get()
                    .getId());
            formattedLocationMembers.stream().skip(1).forEach(list ->
                    list.stream().filter(Objects::nonNull).findFirst().ifPresent(member -> list.set(0, "\\r\\n" + member)));
            formattedLocation = formattedLocationMembers.stream()
                    .flatMap(List::stream).filter(Objects::nonNull)
                    .collect(Collectors.joining(", "));
        }
        info.location = formattedLocation;
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

    private PropertyDescriptionInfo createDescription(UsagePointModelTranslationKeys propertyName, Class<?> aClass) {
        return new PropertyDescriptionInfo(propertyName.getKey(), aClass, thesaurus.getString(propertyName.getKey(), propertyName
                .getDefaultFormat()));
    }

    @Override
    public Class getDomainClass() {
        if (!Optional.ofNullable(this.license).isPresent()) {
            return EmptyDomain.class;
        }
        return UsagePoint.class;
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
        return meteringService.getServiceCategory(ServiceKind.valueOf(usagePointInfo.serviceCategory))
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
                .withServiceLocationString(usagePointInfo.location);
    }

    public UsagePointInfo getMetersOnUsagePointInfo(UsagePoint usagePoint) {
        UsagePointInfo info = new UsagePointInfo();
        info.id = usagePoint.getId();
        info.mRID = usagePoint.getMRID();
        info.installationTime = usagePoint.getInstallationTime().toEpochMilli();
        info.version = usagePoint.getVersion();

        Map<MeterRole, MeterRoleInfo> mandatoryMeterRoles = new LinkedHashMap<>();
        usagePoint.getMetrologyConfiguration()
                .filter(metrologyConfiguration -> metrologyConfiguration instanceof UsagePointMetrologyConfiguration)
                .map(UsagePointMetrologyConfiguration.class::cast)
                .ifPresent(metrologyConfiguration -> {
                    info.metrologyConfiguration = new MetrologyConfigurationInfo();
                    info.metrologyConfiguration.id = metrologyConfiguration.getId();
                    info.metrologyConfiguration.name = metrologyConfiguration.getName();
                    metrologyConfiguration.getMeterRoles()
                            .stream()
                            .forEach(meterRole -> mandatoryMeterRoles.put(meterRole, new MeterRoleInfo(meterRole)));
                });

        Map<MeterRole, MeterInfo> meterRoleToMeterInfoMapping = usagePoint.getMeterActivations(usagePoint.getInstallationTime()).stream()
                .filter(meterActivation -> meterActivation.getMeterRole().isPresent() && meterActivation.getMeter().isPresent())
                .collect(Collectors.toMap(meterActivation -> meterActivation.getMeterRole().get(), meterActivation -> {
                    Meter meter = meterActivation.getMeter().get();
                    MeterInfo meterInfo = new MeterInfo();
                    meterInfo.id = meter.getId();
                    meterInfo.mRID = meter.getMRID();
                    meterInfo.name = meter.getName();
                    meterInfo.version = meter.getVersion();
                    return meterInfo;
                }));

        info.meterActivations = mandatoryMeterRoles.entrySet().stream()
                .map(meterRoleEntry -> {
                    MeterActivationInfo meterActivationInfo = new MeterActivationInfo();
                    meterActivationInfo.meterRole = meterRoleEntry.getValue();
                    meterActivationInfo.meter = meterRoleToMeterInfoMapping.get(meterRoleEntry.getKey());
                    return meterActivationInfo;
                })
                .collect(Collectors.toList());
        return info;
    }

    static class EmptyDomain {
    }
}