package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.rest.util.PropertyDescriptionInfo;
import com.elster.jupiter.util.units.Quantity;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory class to create Info objects. This class will register on the InfoFactoryWhiteboard and is used by DynamicSearch.
 * Created by bvn on 6/9/15.
 */
@Component(name="usagepoint.info.factory", service = { InfoFactory.class }, immediate = true)
public class UsagePointInfoFactory implements InfoFactory<UsagePoint> {

    private volatile Clock clock;
    private volatile Thesaurus thesaurus;
    private volatile Thesaurus valueThesaurus;
    private volatile MeteringService meteringService;

    @Inject
    public UsagePointInfoFactory(Clock clock, Thesaurus thesaurus, MeteringService meteringService) {
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.meteringService = meteringService;
    }

    public UsagePointInfoFactory() {
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(MeteringApplication.COMPONENT_NAME, Layer.REST);
        this.valueThesaurus = nlsService.getThesaurus(MeteringApplication.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Override
    public UsagePointTranslatedInfo from(UsagePoint usagePoint) {
        UsagePointTranslatedInfo info = new UsagePointTranslatedInfo(usagePoint, clock);
        info.displayServiceCategory = usagePoint.getServiceCategory().getKind().getDisplayName(thesaurus);
        return info;
    }

    @Override
    public List<PropertyDescriptionInfo> modelStructure() {
        List<PropertyDescriptionInfo> infos = new ArrayList<>();
        infos.add(createDescription(TranslationSeeds.MRID, String.class));
        infos.add(createDescription(TranslationSeeds.SERVICE_CATEGORY_DISPLAY, String.class));
        infos.add(createDescription(TranslationSeeds.NAME, String.class));
        infos.add(createDescription(TranslationSeeds.ESTIMATED_LOAD, Quantity.class));
        infos.add(createDescription(TranslationSeeds.GROUNDED, Boolean.class));
        infos.add(createDescription(TranslationSeeds.DSP, Boolean.class));
        infos.add(createDescription(TranslationSeeds.VIRTUAL, Boolean.class));
        infos.add(createDescription(TranslationSeeds.SERVICE_VOLTAGE, Quantity.class));
        infos.add(createDescription(TranslationSeeds.OUTAGE_REGION, String.class));
        infos.add(createDescription(TranslationSeeds.PHASE_CODE, String.class));
        infos.add(createDescription(TranslationSeeds.RATED_CURRENT, Quantity.class));
        infos.add(createDescription(TranslationSeeds.RATED_POWER, Quantity.class));
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

    public UsagePointBuilder newUsagePointBuilder(UsagePointInfo usagePointInfo) {
        return meteringService.getServiceCategory(usagePointInfo.serviceCategory)
                .orElseThrow(IllegalArgumentException::new)
                .newUsagePoint(
                        usagePointInfo.mRID,
                        usagePointInfo.installationTime != null ? Instant.ofEpochMilli(usagePointInfo.installationTime) : clock.instant())
                .withName(usagePointInfo.name);
    }
}
