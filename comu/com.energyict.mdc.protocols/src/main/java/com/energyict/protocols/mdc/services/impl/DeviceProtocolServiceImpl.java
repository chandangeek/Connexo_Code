package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.protocol.api.exceptions.ProtocolCreationException;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.UnableToCreateProtocolInstance;
import com.energyict.protocols.impl.channels.CustomPropertySetTranslationKeys;
import com.energyict.protocols.impl.channels.ip.IpMessageSeeds;
import com.energyict.protocols.mdc.protocoltasks.CTRTranslationKeys;
import com.energyict.protocols.mdc.protocoltasks.EiWebPlusDialectProperties;

import com.energyict.protocolimplv2.abnt.AbntTranslationKeys;
import com.energyict.protocolimplv2.ace4000.ACE4000Properties;
import com.energyict.protocolimplv2.common.CommonV2TranslationKeys;
import com.energyict.protocolimplv2.elster.garnet.GarnetTranslationKeys;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;
import org.osgi.service.component.annotations.Component;

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
        return Stream.of(
                    Stream.of(EiWebPlusDialectProperties.TranslationKeys.values()),
                    Stream.of(SecurityPropertySpecName.values()).map(ConnexoTranslationKeyAdapter::new),
                    Stream.of(CustomPropertySetTranslationKeys.values()),
                    Stream.of(AbntTranslationKeys.values()),
                    Stream.of(CTRTranslationKeys .values()),
                    Stream.of(GarnetTranslationKeys.values()),
                    Stream.of(CommonV2TranslationKeys.values()),
                    Stream.of(ACE4000Properties.TranslationKeys.values()),
                    Stream.of(TranslationKeys.values()))
                .flatMap(Function.identity())
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