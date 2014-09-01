package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventorAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.JsonMappingExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedFieldValidationExceptionMapper;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.common.rest.ExceptionLogger;
import com.energyict.mdc.common.rest.Installer;
import com.energyict.mdc.common.rest.TransactionWrapper;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.core.Application;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mds.rest", service = { Application.class, InstallService.class }, immediate = true, property = {"alias=/mds", "name=" + MasterDataApplication.COMPONENT_NAME})
public class MasterDataApplication extends Application implements InstallService {
    
    private final Logger logger = Logger.getLogger(MasterDataApplication.class.getName());

    public static final String COMPONENT_NAME = "MDR";

    private volatile MasterDataService masterDataService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile TransactionService transactionService;
    private volatile NlsService nlsService;
    private volatile JsonService jsonService;
    private volatile Thesaurus thesaurus;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                LogBookResource.class,
                LoadProfileResource.class,
                PhenomenonResource.class,
                TransactionWrapper.class,
                ExceptionLogger.class,
                ConstraintViolationExceptionMapper.class,
                LocalizedFieldValidationExceptionMapper.class,
                JsonMappingExceptionMapper.class,
                LocalizedExceptionMapper.class,
                EndDeviceDomainResource.class,
                EndDeviceSubDomainResource.class,
                EndDeviceEventOrActionResource.class
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
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Override
    public void install() {
        Installer installer = new Installer();
        installer.createTranslations(COMPONENT_NAME, thesaurus, Layer.REST, MessageSeeds.values());
        createTranslations();
    }
    
    private void createTranslations() {
        try {
            List<Translation> translations = new ArrayList<>();
            for (EndDeviceType endDeviceType : EndDeviceType.values()) {
                SimpleNlsKey nlsKey = SimpleNlsKey.key(COMPONENT_NAME, Layer.REST, EndDeviceType.class.getSimpleName() + endDeviceType.getMnemonic()).defaultMessage(endDeviceType.getMnemonic());
                translations.add(toTranslation(nlsKey, Locale.ENGLISH, endDeviceType.getMnemonic()));
            }
            
            for (EndDeviceDomain endDeviceDomain : EndDeviceDomain.values()) {
                SimpleNlsKey nlsKey = SimpleNlsKey.key(COMPONENT_NAME, Layer.REST, EndDeviceDomain.class.getSimpleName() + endDeviceDomain.getMnemonic()).defaultMessage(endDeviceDomain.getMnemonic());
                translations.add(toTranslation(nlsKey, Locale.ENGLISH, endDeviceDomain.getMnemonic()));
            }
            
            for (EndDeviceSubDomain endDeviceSubDomain : EndDeviceSubDomain.values()) {
                SimpleNlsKey nlsKey = SimpleNlsKey.key(COMPONENT_NAME, Layer.REST, EndDeviceSubDomain.class.getSimpleName() + endDeviceSubDomain.getMnemonic()).defaultMessage(endDeviceSubDomain.getMnemonic());
                translations.add(toTranslation(nlsKey, Locale.ENGLISH, endDeviceSubDomain.getMnemonic()));
            }
            
            for (EndDeviceEventorAction endDeviceEventorAction : EndDeviceEventorAction.values()) {
                SimpleNlsKey nlsKey = SimpleNlsKey.key(COMPONENT_NAME, Layer.REST, EndDeviceEventorAction.class.getSimpleName() + endDeviceEventorAction.getMnemonic()).defaultMessage(endDeviceEventorAction.getMnemonic());
                translations.add(toTranslation(nlsKey, Locale.ENGLISH, endDeviceEventorAction.getMnemonic()));
            }
            thesaurus.addTranslations(translations);
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }

    private Translation toTranslation(SimpleNlsKey nlsKey, Locale locale, String translation) {
        return new SimpleTranslation(nlsKey, locale, translation);
    }
    
    private static class SimpleTranslation implements Translation {

        private final SimpleNlsKey nlsKey;
        private final Locale locale;
        private final String translation;

        private SimpleTranslation(SimpleNlsKey nlsKey, Locale locale, String translation) {
            this.nlsKey = nlsKey;
            this.locale = locale;
            this.translation = translation;
        }

        @Override
        public NlsKey getNlsKey() {
            return nlsKey;
        }

        @Override
        public Locale getLocale() {
            return locale;
        }

        @Override
        public String getTranslation() {
            return translation;
        }
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(masterDataService).to(MasterDataService.class);
            bind(deviceConfigurationService).to(DeviceConfigurationService.class);
            bind(transactionService).to(TransactionService.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(nlsService).to(NlsService.class);
            bind(jsonService).to(JsonService.class);
            bind(thesaurus).to(Thesaurus.class);
        }
    }

}