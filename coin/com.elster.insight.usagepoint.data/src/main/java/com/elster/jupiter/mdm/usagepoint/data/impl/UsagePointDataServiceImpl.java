package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.mdm.usagepoint.data.ChannelDataValidationSummary;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataService;
import com.elster.jupiter.mdm.usagepoint.data.exceptions.MessageSeeds;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.EffectiveMetrologyContractOnUsagePoint;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component(
        name = "UsagePointDataServiceImpl",
        service = {UsagePointDataService.class, MessageSeedProvider.class},
        property = {"name=" + UsagePointDataService.COMPONENT_NAME},
        immediate = true)
public class UsagePointDataServiceImpl implements UsagePointDataService, MessageSeedProvider {

    private volatile Clock clock;
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile MeteringService meteringService;
    private volatile UsagePointConfigurationService usagePointConfigurationService;

    @SuppressWarnings("unused")
    public UsagePointDataServiceImpl() {
        // OSGI
    }

    @Inject
    public UsagePointDataServiceImpl(Clock clock,
                                     OrmService ormService,
                                     MeteringService meteringService,
                                     NlsService nlsService,
                                     CustomPropertySetService customPropertySetService,
                                     UsagePointConfigurationService usagePointConfigurationService) {
        setClock(clock);
        setOrmService(ormService);
        setMeteringService(meteringService);
        setNlsService(nlsService);
        setCustomPropertySetService(customPropertySetService);
        setUsagePointConfigurationService(usagePointConfigurationService);
        activate();
    }

    Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Clock.class).toInstance(clock);
                bind(CustomPropertySetService.class).toInstance(customPropertySetService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(UsagePointDataService.class).toInstance(UsagePointDataServiceImpl.this);
                bind(MeteringService.class).toInstance(meteringService);
                bind(UsagePointConfigurationService.class).toInstance(usagePointConfigurationService);
            }
        };
    }

    @Activate
    public void activate() {
        dataModel.register(getModule());
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(UsagePointDataService.COMPONENT_NAME, "Usage Point Data");
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(UsagePointDataService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setUsagePointConfigurationService(UsagePointConfigurationService usagePointConfigurationService) {
        this.usagePointConfigurationService = usagePointConfigurationService;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public ChannelDataValidationSummary getValidationSummary(Channel channel, Range<Instant> interval) {
        // FIXME: finish this
        ChannelDataValidationSummary summary = new ChannelDataValidationSummaryImpl();
        return summary;
    }

    @Override
    public Map<ReadingTypeDeliverable, ChannelDataValidationSummary> getValidationSummary(EffectiveMetrologyContractOnUsagePoint contract,
                                                                                          Range<Instant> interval) {
        ChannelsContainer container = contract.getChannelsContainer();
        return contract.getMetrologyContract().getDeliverables().stream().collect(Collectors.toMap(
                Function.identity(),
                deliverable -> container.getChannel(deliverable.getReadingType()) // channel cannot be unfound
                        .map(channel -> getValidationSummary(channel, interval))
                        .orElseGet(ChannelDataValidationSummaryImpl::new),
                (summary1, summary2) -> { // merge should not appear since no ReadingTypeDeliverable duplication allowed
                    throw new LocalizedException(thesaurus,
                            MessageSeeds.DUPLICATE_READINGTYPE_ON_METROLOGY_CONTRACT,
                            contract.getMetrologyContract().getId()) {
                    };
                },
                LinkedHashMap::new
        ));
    }
}
