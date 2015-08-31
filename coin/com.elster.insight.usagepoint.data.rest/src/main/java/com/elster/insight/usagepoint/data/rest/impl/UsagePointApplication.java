package com.elster.insight.usagepoint.data.rest.impl;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.core.Application;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.insight.common.rest.ExceptionFactory;
import com.elster.insight.usagepoint.data.Register;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.validation.rest.ValidationRuleInfoFactory;
import com.google.common.collect.ImmutableSet;

@Component(name = "com.elster.insight.udr.rest", service = {Application.class, TranslationKeyProvider.class}, immediate = true, property = {"alias=/udr", "app=INS", "name=" + UsagePointApplication.COMPONENT_NAME})
public class UsagePointApplication extends Application implements TranslationKeyProvider {

    private final Logger logger = Logger.getLogger(UsagePointApplication.class.getName());

    public static final String APP_KEY = "INS";
    public static final String COMPONENT_NAME = "UDR";


    private volatile TransactionService transactionService;
    private volatile NlsService nlsService;
    private volatile JsonService jsonService;
    private volatile Thesaurus thesaurus;
    private volatile MeteringService meteringService;
    private volatile RestQueryService restQueryService;
    private volatile Clock clock;
    private volatile MessageService messageService;

    @Override
    public Set<Class<?>> getClasses() {
                return ImmutableSet.of(
                        ChannelResource.class,
                        UsagePointResource.class,
                        RegisterResource.class,
                        DeviceResource.class
//                        DeviceResource.class,
//                        ProtocolDialectResource.class,
//                        RegisterResource.class,
//                        RegisterDataResource.class,
//                        DeviceValidationResource.class,
//                        LoadProfileResource.class,
//                        BulkScheduleResource.class,
//                        DeviceScheduleResource.class,
//                        DeviceSharedScheduleResource.class,
//                        DeviceComTaskResource.class,
//                        LogBookResource.class,
//                        DeviceFieldResource.class,
//                        ChannelResource.class,
//                        ChannelResource.class,
//                        DeviceGroupResource.class,
//                        SecurityPropertySetResource.class,
//                        ConnectionMethodResource.class,
//                        ComSessionResource.class,
//                        DeviceMessageResource.class,
//                        DeviceLabelResource.class,
//                        ConnectionResource.class,
//                        DeviceProtocolPropertyResource.class,
//                        KpiResource.class,
//                        AdhocGroupResource.class,
//                        DeviceEstimationResource.class,
//                        DeviceHistoryResource.class,
//                        DeviceLifeCycleActionResource.class,
//                        DeviceStateAccessFeature.class
        );
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<TranslationKey> getKeys() {
        Set<String> uniqueIds = new HashSet<>();
        List<TranslationKey> keys = new ArrayList<>();
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            if (uniqueIds.add(messageSeed.getKey())) {
                keys.add(messageSeed);
            }
        }

        Arrays.stream(DefaultTranslationKey.values())
                .forEach(keys::add);
        return keys;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }


    @Reference
    public void setRestQueryService(RestQueryService restQueryService) {
        this.restQueryService = restQueryService;
    }

    @Reference
    public void setClockService(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }
    
    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(ChannelResource.class).to(ChannelResource.class);
            bind(RegisterResource.class).to(RegisterResource.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(transactionService).to(TransactionService.class);
            bind(ResourceHelper.class).to(ResourceHelper.class);
            bind(ChannelResourceHelper.class).to(ChannelResourceHelper.class);
            bind(RegisterResourceHelper.class).to(RegisterResourceHelper.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(nlsService).to(NlsService.class);
            bind(jsonService).to(JsonService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(meteringService).to(MeteringService.class);
            bind(restQueryService).to(RestQueryService.class);
            bind(clock).to(Clock.class);
            bind(ValidationInfoFactory.class).to(ValidationInfoFactory.class);
            bind(ValidationRuleInfoFactory.class).to(ValidationRuleInfoFactory.class);
            bind(UsagePointDataInfoFactory.class).to(UsagePointDataInfoFactory.class);
            bind(com.elster.jupiter.validation.rest.PropertyUtils.class).to(com.elster.jupiter.validation.rest.PropertyUtils.class);
            bind(EstimationRuleInfoFactory.class).to(EstimationRuleInfoFactory.class);
            bind(messageService).to(MessageService.class);
        }
    }
}