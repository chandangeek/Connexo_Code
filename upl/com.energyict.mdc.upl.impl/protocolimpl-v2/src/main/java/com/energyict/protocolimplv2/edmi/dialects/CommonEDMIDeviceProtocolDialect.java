package com.energyict.protocolimplv2.edmi.dialects;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.Thesaurus;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author sva
 * @since 22/02/2017 - 16:30
 */
public abstract class CommonEDMIDeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    public static final String CONNECTION_MODE = "ConnectionMode";

    protected static final BigDecimal DEFAULT_RETRIES = new BigDecimal(3);
    protected static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    abstract BigDecimal getDefaultRetries();
    abstract Duration getDefaultTimeout();
    abstract Duration getDefaultForcedDelay();
    abstract String getDefaultConnectionMode();

    public CommonEDMIDeviceProtocolDialect(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    public CommonEDMIDeviceProtocolDialect(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.retriesPropertySpec(),
                this.timeoutPropertySpec(),
                this.forcedDelayPropertySpec(),
                this.connectionModePropertySpec()
        );
    }

    protected PropertySpec retriesPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(DlmsProtocolProperties.RETRIES, false, PropertyTranslationKeys.V2_ELSTER_RETRIES, getPropertySpecService()::bigDecimalSpec)
                .setDefaultValue(getDefaultRetries())
                .finish();
    }

    protected PropertySpec timeoutPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(DlmsProtocolProperties.TIMEOUT, false, PropertyTranslationKeys.V2_ELSTER_TIMEOUT, getPropertySpecService()::durationSpec)
                .setDefaultValue(getDefaultTimeout())
                .finish();
    }

    protected PropertySpec forcedDelayPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(DlmsProtocolProperties.FORCED_DELAY, false, PropertyTranslationKeys.V2_ELSTER_FORCED_DELAY, getPropertySpecService()::durationSpec)
                .setDefaultValue(getDefaultForcedDelay())
                .finish();
    }

    private PropertySpec connectionModePropertySpec() {
        return UPLPropertySpecFactory.specBuilder(CONNECTION_MODE, false, PropertyTranslationKeys.V2_DLMS_CONNECTION_MODE, getPropertySpecService()::stringSpec)
                .addValues(Arrays.stream(ConnectionMode.values()).filter(ConnectionMode::isSelectable).map(ConnectionMode::getName).collect(Collectors.toList()))
                .setDefaultValue(getDefaultConnectionMode())
                .finish();
    }

    public enum ConnectionMode {
        EXTENDED_COMMAND_LINE("Extended command line", true),
        MINI_E_COMMAND_LINE("Mini-E command line", true),
        UNDEFINED("Undefined", false);

        private final String name;
        private final boolean selectable;

        ConnectionMode(String name, boolean selectable) {
            this.name = name;
            this.selectable = selectable;
        }

        public String getName() {
            return name;
        }

        public boolean isSelectable() {
            return selectable;
        }

        public static ConnectionMode fromName(String name) {
            for (ConnectionMode connectionMode : values()) {
                if (connectionMode.getName().equals(name)) {
                    return connectionMode;
                }
            }
            return UNDEFINED;
        }
    }

}