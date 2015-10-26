package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.rest.UsagePointInfo;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.rest.util.PropertyDescriptionInfo;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Factory class to create Info objects. This class will register on the InfoFactoryWhiteboard and is used by DynamicSearch.
 * Created by bvn on 6/9/15.
 */
@Component(name="usagepoint.info.factory", service = { InfoFactory.class }, immediate = true)
public class UsagePointInfoFactory implements InfoFactory<UsagePoint> {

    private volatile Clock clock;
    private volatile Thesaurus thesaurus;

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(MeteringApplication.COMPONENT_NAME, Layer.REST);
    }

    @Override
    public Object from(UsagePoint usagePoint) {
        UsagePointInfo info = new UsagePointInfo();
        info.id = usagePoint.getId();
        info.mRID = usagePoint.getMRID();
        info.serviceCategory = usagePoint.getServiceCategory().getKind();
        info.serviceLocationId = usagePoint.getServiceLocationId();
        info.aliasName = usagePoint.getAliasName();
        info.description = usagePoint.getDescription();
        info.name = usagePoint.getName();
        info.isSdp = usagePoint.isSdp();
        info.isVirtual = usagePoint.isVirtual();
        info.outageRegion = usagePoint.getOutageRegion();
        info.readCycle = usagePoint.getReadCycle();
        info.readRoute = usagePoint.getReadRoute();
        info.servicePriority = usagePoint.getServicePriority();
        info.version = usagePoint.getVersion();
        info.createTime = usagePoint.getCreateDate().toEpochMilli();
        info.modTime = usagePoint.getModificationDate().toEpochMilli();
        Optional<? extends UsagePointDetail> detailHolder = usagePoint.getDetail(clock.instant());
        if (detailHolder.isPresent()) {
            UsagePointDetail detail = detailHolder.get();
            info.minimalUsageExpected = detail.isMinimalUsageExpected();
            info.amiBillingReady = detail.getAmiBillingReady();
            info.checkBilling = detail.isCheckBilling();
            info.connectionState = detail.getConnectionState();
            info.serviceDeliveryRemark = detail.getServiceDeliveryRemark();
            if (detail instanceof ElectricityDetail) {
                ElectricityDetail eDetail = (ElectricityDetail) detail;
                info.estimatedLoad = eDetail.getEstimatedLoad();
                info.grounded = eDetail.isGrounded();
                info.nominalServiceVoltage = eDetail.getNominalServiceVoltage();
                info.phaseCode = eDetail.getPhaseCode();
                info.ratedCurrent = eDetail.getRatedCurrent();
                info.ratedPower = eDetail.getRatedPower();
            }
        }

        return info;
    }

    @Override
    public List<PropertyDescriptionInfo> modelStructure() {
        List<PropertyDescriptionInfo> infos = new ArrayList<>();
        infos.add(createDescription(TranslationSeeds.MRID, String.class));
        infos.add(createDescription(TranslationSeeds.SERVICE_CATEGORY, String.class));
        infos.add(createDescription(TranslationSeeds.ALIAS_NAME, String.class));
        infos.add(createDescription(TranslationSeeds.DESCRIPTION, String.class));
        infos.add(createDescription(TranslationSeeds.NAME, String.class));
        infos.add(createDescription(TranslationSeeds.BILLING_READY, String.class));
        infos.add(createDescription(TranslationSeeds.CHECK_BILLING, Boolean.class));
        infos.add(createDescription(TranslationSeeds.CONNECTION_STATE, String.class));
        infos.add(createDescription(TranslationSeeds.ESTIMATED_LOAD, String.class));
        infos.add(createDescription(TranslationSeeds.GROUNDED, Boolean.class));
        infos.add(createDescription(TranslationSeeds.DSP, Boolean.class));
        infos.add(createDescription(TranslationSeeds.VIRTUAL, Boolean.class));
        infos.add(createDescription(TranslationSeeds.MIN_USAGE_EXPECTED, Boolean.class));
        infos.add(createDescription(TranslationSeeds.SERVICE_VOLTAGE, String.class));
        infos.add(createDescription(TranslationSeeds.OUTAGE_REGION, String.class));
        infos.add(createDescription(TranslationSeeds.PHASE_CODE, String.class));
        infos.add(createDescription(TranslationSeeds.RATED_CURRENT, String.class));
        infos.add(createDescription(TranslationSeeds.RATED_POWER, String.class));
        infos.add(createDescription(TranslationSeeds.READ_CYCLE, String.class));
        infos.add(createDescription(TranslationSeeds.READ_ROUTE, String.class));
        infos.add(createDescription(TranslationSeeds.REMARK, String.class));
        infos.add(createDescription(TranslationSeeds.PRIORITY, String.class));
        infos.add(createDescription(TranslationSeeds.ISSUES, String.class));
        return infos;
    }

    private PropertyDescriptionInfo createDescription(TranslationSeeds propertyName, Class<?> aClass) {
        return new PropertyDescriptionInfo(propertyName.getKey(), aClass, thesaurus.getString(propertyName.getKey(), propertyName.getDefaultFormat()));
    }


    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }
}
