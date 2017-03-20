package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.protocol.api.exceptions.ProtocolCreationException;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.UnableToCreateProtocolInstance;
import com.energyict.mdc.upl.DeviceGroupExtractor;
import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.RuntimeEnvironment;
import com.energyict.mdc.upl.crypto.KeyStoreService;
import com.energyict.mdc.upl.crypto.X509Service;
import com.energyict.mdc.upl.io.UPLSocketService;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.DeviceExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.Formatter;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.RegisterExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.SecurityService;
import com.energyict.protocolimplv2.securitysupport.CustomPropertySetTranslationKeys;
import com.energyict.protocols.impl.channels.ip.IpMessageSeeds;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link DeviceProtocolService} interface
 * and registers as a OSGi component.
 * <p>
 * Copyrights EnergyICT
 * Date: 06/11/13
 * Time: 11:03
 */
@Component(name = "com.energyict.mdc.service.deviceprotocols",
        service = {DeviceProtocolService.class, MessageSeedProvider.class, TranslationKeyProvider.class},
        immediate = true,
        property = "name=" + DeviceProtocolService.COMPONENT_NAME)
public class DeviceProtocolServiceImpl implements DeviceProtocolService, MessageSeedProvider, TranslationKeyProvider {

    private static final Map<String, InstanceFactory> uplFactories = new ConcurrentHashMap<>();

    /**
     * Below are dummy references with all the UPL services that can be used in the constructors of the 9.1 protocols.
     * Before a protocol can be instantiated by this service (@link com.energyict.mdc.protocol.api.services.DeviceProtocolService),
     * the other UPL services must be activated.
     */
    @Reference
    public void setRuntimeEnvironment(RuntimeEnvironment runtimeEnvironment) {
    }

    //TODO refactor the ObjectMapperService away, not needed as a service
/*
    @Reference
    public void setObjectMapperService(ObjectMapperService objectMapperService) {
    }
*/

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
    }

    @Reference
    public void setSecurityService(SecurityService securityService) {
    }

    @Reference
    public void setUplSocketService(UPLSocketService uplSocketService) {
    }

    @Reference
    public void setConverter(Converter converter) {
    }

    @Reference
    public void setDeviceMasterDataExtractor(DeviceMasterDataExtractor deviceMasterDataExtractor) {
    }

    @Reference
    public void setDeviceExtractor(DeviceExtractor deviceExtractor) {
    }

    @Reference
    public void setDeviceGroupExtractor(DeviceGroupExtractor deviceGroupExtractor) {
    }

    @Reference
    public void setRegisterExtractor(RegisterExtractor registerExtractor) {
    }

    @Reference
    public void setLoadProfileExtractor(LoadProfileExtractor loadProfileExtractor) {
    }

    @Reference
    public void setNumberLookupExtractor(NumberLookupExtractor numberLookupExtractor) {
    }

    @Reference
    public void setDeviceMessageFileExtractor(DeviceMessageFileExtractor deviceMessageFileExtractor) {
    }

    @Reference
    public void setTariffCalendarExtractor(TariffCalendarExtractor tariffCalendarExtractor) {
    }

    @Reference
    public void setTariffCalendarFinder(TariffCalendarFinder tariffCalendarFinder) {
    }

    @Reference
    public void setDeviceMessageFileFinder(DeviceMessageFileFinder deviceMessageFileFinder) {
    }

    @Reference
    public void setCollectedDataFactory(CollectedDataFactory collectedDataFactory) {
    }

    @Reference
    public void setIssueFactory(IssueFactory issueFactory) {
    }

    @Reference
    public void setFormatter(Formatter formatter) {
    }

    @Reference
    public void setX509Service(X509Service x509Service) {
    }

    @Reference
    public void setKeyStoreService(KeyStoreService keyStoreService) {
    }

    //TODO uncomment, these 3 services are needed in some of the constructors of the protocols! (but they do not yet exist in CXO)
    /*
    @Reference
    public void setNumberLookupFinder(NumberLookupFinder numberLookupFinder) {
    }

    @Reference
    public void setCertificateWrapperExtractor(CertificateWrapperExtractor certificateWrapperExtractor) {
    }

    @Reference
    public void setCertificateAliasFinder(CertificateAliasFinder certificateAliasFinder) {
    }*/

    @Override
    public Object createProtocol(String className) {
        try {
            return uplFactories
                    .computeIfAbsent(className, ConstructorBasedUplServiceInjection::from)
                    .newInstance();
        } catch (UnableToCreateProtocolInstance e) {
            throw new ProtocolCreationException(MessageSeeds.UNSUPPORTED_LEGACY_PROTOCOL_TYPE, className);
        }
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Stream.of(
                Stream.of(IpMessageSeeds.values()),
                Stream.of(MessageSeeds.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Override
    public String getComponentName() {
        return DeviceProtocolService.COMPONENT_NAME;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> allKeys = new ArrayList<>(getProtocolPropertyKeys());
        allKeys.addAll(Arrays.asList(com.energyict.protocols.mdc.services.impl.TranslationKeys.values()));
        allKeys.addAll(Arrays.asList(CustomPropertySetTranslationKeys.values()));
        return allKeys;
    }

    private List<TranslationKey> getProtocolPropertyKeys() {
        return Stream.of(
                Stream.of(com.energyict.mdc.channels.nls.PropertyTranslationKeys.values()),
                Stream.of(com.energyict.nls.PropertyTranslationKeys.values()),
                Stream.of(com.elster.protocolimpl.nls.PropertyTranslationKeys.values()),
                Stream.of(com.elster.us.nls.PropertyTranslationKeys.values()),
                Stream.of(com.energyict.protocolimpl.nls.PropertyTranslationKeys.values()),
                Stream.of(com.energyict.protocolimpl.properties.nls.PropertyTranslationKeys.values()))
                .flatMap(Function.identity())
                .map(com.energyict.mdc.upl.nls.TranslationKey.class::cast)
                .map(ConnexoTranslationKeyAdapter::new)
                .collect(Collectors.toList());
    }

    /**
     * Adapter between UPL TranslationKey and Connexo TranslationKey.
     */
    private static class ConnexoTranslationKeyAdapter implements com.elster.jupiter.nls.TranslationKey {
        private final com.energyict.mdc.upl.nls.TranslationKey actual;

        ConnexoTranslationKeyAdapter(com.energyict.mdc.upl.nls.TranslationKey actual) {
            this.actual = actual;
        }

        @Override
        public String getKey() {
            return this.actual.getKey();
        }

        @Override
        public String getDefaultFormat() {
            return this.actual.getDefaultFormat();
        }
    }
}