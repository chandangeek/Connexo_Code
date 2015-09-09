package com.energyict.mdc.masterdata.rest.impl;

import com.energyict.mdc.common.rest.ExceptionLogger;
import com.energyict.mdc.common.rest.TransactionWrapper;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import com.elster.jupiter.license.License;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(name = "com.energyict.mds.rest", service = {Application.class, TranslationKeyProvider.class, MessageSeedProvider.class}, immediate = true, property = {"alias=/mds", "app=MDC", "name=" + MasterDataApplication.COMPONENT_NAME})
public class MasterDataApplication extends Application implements TranslationKeyProvider,MessageSeedProvider {

    public static final String APP_KEY = "MDC";
    public static final String COMPONENT_NAME = "MDR";

    private volatile MeteringService meteringService;
    private volatile MasterDataService masterDataService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile TransactionService transactionService;
    private volatile NlsService nlsService;
    private volatile JsonService jsonService;
    private volatile Thesaurus thesaurus;
    private volatile License license;
    private volatile MdcReadingTypeUtilService mdcReadingTypeUtilService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                ReadingTypeResource.class,
                LogBookTypeResource.class,
                RegisterTypeResource.class,
                LoadProfileTypeResource.class,
                TransactionWrapper.class,
                ExceptionLogger.class
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
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setMasterDataService(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setMdcReadingTypeUtilService(MdcReadingTypeUtilService mdcReadingTypeUtilService) {
        this.mdcReadingTypeUtilService = mdcReadingTypeUtilService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(TranslationKeys.values());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference(target="(com.elster.jupiter.license.rest.key=" + APP_KEY  + ")")
    public void setLicense(License license) {
        this.license = license;
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(meteringService).to(MeteringService.class);
            bind(masterDataService).to(MasterDataService.class);
            bind(deviceConfigurationService).to(DeviceConfigurationService.class);
            bind(transactionService).to(TransactionService.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(nlsService).to(NlsService.class);
            bind(jsonService).to(JsonService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(mdcReadingTypeUtilService).to(MdcReadingTypeUtilService.class);
        }
    }

}