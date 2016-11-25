package com.elster.jupiter.kore.api.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.cps.rest.impl.CustomPropertySetApplication;
import com.elster.jupiter.kore.api.impl.servicecall.UsagePointCommandCustomPropertySet;
import com.elster.jupiter.kore.api.impl.servicecall.UsagePointCommandHelper;
import com.elster.jupiter.kore.api.impl.utils.MessageSeeds;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.hypermedia.RestExceptionMapper;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.hibernate.validator.HibernateValidator;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.ws.rs.core.Application;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.elster.jupiter.orm.Version.version;

@Component(name = "com.elster.jupiter.pulse.public.rest",
        service = {Application.class, TranslationKeyProvider.class, MessageSeedProvider.class},
        immediate = true,
        property = {"alias=/coko", "app=SYS", "name=" + PublicRestApplication.COMPONENT_NAME, "version=v1.0"})
public class PublicRestApplication extends Application implements TranslationKeyProvider, MessageSeedProvider {

    static final String APP_KEY = "SYS";
    public static final String COMPONENT_NAME = "PRA";

    private volatile Thesaurus thesaurus;
    private volatile NlsService nlsService;
    private volatile TransactionService transactionService;
    private volatile OrmService ormService;
    private volatile Clock clock;
    private volatile MeteringService meteringService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile MetrologyConfigurationService metrologyConfigurationService;
    private volatile ServiceCallService serviceCallService;
    private volatile MessageService messageService;
    private volatile UpgradeService upgradeService;
    private volatile PropertySpecService propertySpecService;
    private volatile PropertyValueInfoService propertyValueInfoService;
    private volatile UserService userService;

    @Activate
    public void activate() {
        DataModel dataModel = upgradeService.newNonOrmDataModel();
        InstallIdentifier identifier = InstallIdentifier.identifier("Insight", "PRA");
        if (upgradeService.isInstalled(identifier, version(1, 0))) {
            this.registerCustomPropertySets();
        }

        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(ServiceCallService.class).toInstance(serviceCallService);
                bind(CustomPropertySetService.class).toInstance(customPropertySetService);
                bind(MessageService.class).toInstance(messageService);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(OrmService.class).toInstance(ormService);
                bind(UserService.class).toInstance(userService);
            }
        });

        upgradeService.register(
                identifier,
                dataModel,
                Installer.class,
                ImmutableMap.of(
                        version(10, 2), UpgraderV10_2.class,
                        version(10, 3), UpgraderV10_3.class
                ));
    }

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                UsagePointResource.class,
                UsagePointCustomPropertySetResource.class,
                MeterActivationResource.class,
                MetrologyConfigurationResource.class,
                EndDeviceResource.class,
                EffectiveMetrologyConfigurationResource.class,
                RestExceptionMapper.class
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
        this.thesaurus.join(nlsService.getThesaurus(CustomPropertySetApplication.COMPONENT_NAME, Layer.REST));
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
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
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Collections.emptyList();
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
    public void setPropertySpecService(PropertySpecService propertySpecService){
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setPropertyValueInfoService(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    private Factory<Validator> getValidatorFactory() {
        return new Factory<Validator>() {
            private final ValidatorFactory validatorFactory = Validation.byDefaultProvider()
                    .providerResolver(() -> ImmutableList.of(new HibernateValidator()))
                    .configure()
                    .messageInterpolator(thesaurus)
                    .buildValidatorFactory();

            @Override
            public Validator provide() {
                return validatorFactory.getValidator();
            }

            @Override
            public void dispose(Validator validator) {

            }
        };
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(thesaurus).to(Thesaurus.class);
            bind(nlsService).to(NlsService.class);
            bind(transactionService).to(TransactionService.class);
            bind(clock).to(Clock.class);
            bind(meteringService).to(MeteringService.class);
            bind(customPropertySetService).to(CustomPropertySetService.class);
            bind(metrologyConfigurationService).to(MetrologyConfigurationService.class);
            bind(messageService).to(MessageService.class);
            bind(serviceCallService).to(ServiceCallService.class);
            bind(propertyValueInfoService).to(PropertyValueInfoService.class);
            bindFactory(getValidatorFactory()).to(Validator.class);

            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(EffectivityHelper.class).to(EffectivityHelper.class);

            bind(ResourceHelper.class).to(ResourceHelper.class);

            bind(CustomPropertySetInfoFactory.class).to(CustomPropertySetInfoFactory.class);
            bind(UsagePointInfoFactory.class).to(UsagePointInfoFactory.class);
            bind(UsagePointCustomPropertySetInfoFactory.class).to(UsagePointCustomPropertySetInfoFactory.class);
            bind(MeterActivationInfoFactory.class).to(MeterActivationInfoFactory.class);
            bind(MetrologyConfigurationInfoFactory.class).to(MetrologyConfigurationInfoFactory.class);
            bind(ElectricityDetailInfoFactory.class).to(ElectricityDetailInfoFactory.class);
            bind(ElectricityDetailResource.class).to(ElectricityDetailResource.class);
            bind(GasDetailInfoFactory.class).to(GasDetailInfoFactory.class);
            bind(GasDetailResource.class).to(GasDetailResource.class);
            bind(HeatDetailInfoFactory.class).to(HeatDetailInfoFactory.class);
            bind(HeatDetailsResource.class).to(HeatDetailsResource.class);
            bind(WaterDetailInfoFactory.class).to(WaterDetailInfoFactory.class);
            bind(WaterDetailResource.class).to(WaterDetailResource.class);
            bind(UsagePointCommandHelper.class).to(UsagePointCommandHelper.class);
            bind(EndDeviceInfoFactory.class).to(EndDeviceInfoFactory.class);
            bind(EndDeviceResource.class).to(EndDeviceResource.class);
            bind(EffectiveMetrologyConfigurationInfoFactory.class).to(EffectiveMetrologyConfigurationInfoFactory.class);
            bind(EffectiveMetrologyConfigurationResource.class).to(EffectiveMetrologyConfigurationResource.class);
            bind(MeterReadingsFactory.class).to(MeterReadingsFactory.class);
            bind(MetrologyConfigurationPurposeInfoFactory.class).to(MetrologyConfigurationPurposeInfoFactory.class);
        }
    }

    private void registerCustomPropertySets(){
        customPropertySetService.addCustomPropertySet(new UsagePointCommandCustomPropertySet(propertySpecService, thesaurus));
    }

}
