package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.WaterDetail;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.rest.util.PropertyDescriptionInfo;
import com.elster.insight.usagepoint.config.rest.MetrologyConfigurationInfo;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(name = "insight.usagepoint.info.factory", service = {InfoFactory.class}, immediate = true)
public class UsagePointInfoFactory implements InfoFactory<UsagePoint> {

    private volatile Clock clock;
    private volatile Thesaurus thesaurus;
    private volatile MeteringService meteringService;
    private volatile CustomPropertySetInfoFactory customPropertySetInfoFactory;

    public UsagePointInfoFactory() {
    }

    @Inject
    public UsagePointInfoFactory(Clock clock, Thesaurus thesaurus, MeteringService meteringService, CustomPropertySetInfoFactory customPropertySetInfoFactory) {
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.meteringService = meteringService;
        this.customPropertySetInfoFactory = customPropertySetInfoFactory;
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
    public void setCustomPropertySetInfoFactory(CustomPropertySetInfoFactory customPropertySetInfoFactory) {
        this.customPropertySetInfoFactory = customPropertySetInfoFactory;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(UsagePointApplication.COMPONENT_NAME, Layer.REST);
    }

    @Override
    public UsagePointInfo from(UsagePoint usagePoint) {
        UsagePointInfo info = new UsagePointInfo(usagePoint);
        info.id = usagePoint.getId();
        info.mRID = usagePoint.getMRID();
        info.serviceLocationId = usagePoint.getServiceLocationId();
        info.name = usagePoint.getName();
        info.isSdp = usagePoint.isSdp();
        info.isVirtual = usagePoint.isVirtual();
        info.outageRegion = usagePoint.getOutageRegion();
        info.readRoute = usagePoint.getReadRoute();
        info.servicePriority = usagePoint.getServicePriority();
        info.serviceDeliveryRemark = usagePoint.getServiceDeliveryRemark();
        info.installationTime = usagePoint.getInstallationTime().getEpochSecond();
        info.version = usagePoint.getVersion();
        info.createTime = usagePoint.getCreateDate().toEpochMilli();
        info.modTime = usagePoint.getModificationDate().toEpochMilli();
        Optional<? extends UsagePointDetail> detailHolder = usagePoint.getDetail(clock.instant());
        if(detailHolder.isPresent()) {
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
        info.metrologyConfiguration = usagePoint.getMetrologyConfiguration()
                .isPresent() ? new MetrologyConfigurationInfo(usagePoint.getMetrologyConfiguration().get()) : null;

        UsagePointCustomPropertySetExtension customPropertySetExtension = usagePoint.forCustomProperties();

        info.customPropertySets = customPropertySetExtension.getAllCustomPropertySets()
                .stream()
                .map(rcps -> customPropertySetInfoFactory.getFullInfo(rcps, customPropertySetExtension.getCustomPropertySetValue(rcps)))
                .collect(Collectors.toList());
        return info;
    }

    @Override
    public List<PropertyDescriptionInfo> modelStructure() {
        return null;
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }

    public UsagePointBuilder newUsagePointBuilder(UsagePointInfo usagePointInfo) {
        return meteringService.getServiceCategory(ServiceKind.valueOf(usagePointInfo.serviceCategory))
                .orElseThrow(IllegalArgumentException::new)
                .newUsagePoint(usagePointInfo.mRID)
                .withName(usagePointInfo.name)
                .withIsSdp(usagePointInfo.isSdp)
                .withIsVirtual(usagePointInfo.isVirtual)
                .withReadRoute(usagePointInfo.readRoute)
                .withServicePriority(usagePointInfo.servicePriority)
                .withServiceDeliveryRemark(usagePointInfo.serviceDeliveryRemark)
                .withInstallationTime(usagePointInfo.installationTime != null ? Instant.ofEpochMilli(usagePointInfo.installationTime) : null);
    }
}
