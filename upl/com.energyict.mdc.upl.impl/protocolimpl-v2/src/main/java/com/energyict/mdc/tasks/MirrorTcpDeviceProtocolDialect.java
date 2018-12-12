package com.energyict.mdc.tasks;

import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.Thesaurus;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.DeviceProtocolDialectTranslationKeys;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_ROUND_TRIP_CORRECTION;
import static com.energyict.dlms.common.DlmsProtocolProperties.RETRIES;
import static com.energyict.dlms.common.DlmsProtocolProperties.ROUND_TRIP_CORRECTION;
import static com.energyict.dlms.common.DlmsProtocolProperties.TIMEOUT;

/**
 * Models a {@link DeviceProtocolDialect} for a TCP connection type to a Beacon 3100 device.
 * Note that, using this dialect, the protocol will read out the mirrored (cached) meter data from the Beacon DC.
 * No communication is done to the actual meter.
 *
 * @author: khe
 */
public class MirrorTcpDeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    public static final Duration DEFAULT_TCP_TIMEOUT = Duration.ofSeconds(30);

    public MirrorTcpDeviceProtocolDialect(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    public MirrorTcpDeviceProtocolDialect(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectTranslationKeys.BEACON_MIRROR_TCP_DLMS_PROTOCOL_DIALECT_NAME.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return getThesaurus().getFormat(DeviceProtocolDialectTranslationKeys.BEACON_MIRROR_TCP_DLMS_PROTOCOL_DIALECT_NAME).format();
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.timeoutPropertySpec(),
                this.retriesPropertySpec(),
                this.roundTripCorrectionPropertySpec()
        );
    }

    /**
     * The default number of retries is 0 for this dialect.
     * This dialect is exclusively used for TCP/IP communication with the Beacon device itself.
     * Since TCP is a confirmed service, we know for sure that our request will arrive at the Beacon device.
     * <p/>
     * In fact, if we were to send a retry for a certain request, the Beacon would reject it.
     * This is because this retry has the same frame counter as the original request!
     */
    protected PropertySpec retriesPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(RETRIES, false, PropertyTranslationKeys.V2_TASKS_RETRIES, getPropertySpecService()::bigDecimalSpec)
                .setDefaultValue(BigDecimal.ZERO)
                .finish();
    }

    protected PropertySpec timeoutPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(TIMEOUT, false, PropertyTranslationKeys.V2_TASKS_TIMEOUT, getPropertySpecService()::durationSpec)
                .setDefaultValue(DEFAULT_TCP_TIMEOUT)
                .finish();
    }

    protected PropertySpec roundTripCorrectionPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(ROUND_TRIP_CORRECTION, false, PropertyTranslationKeys.V2_TASKS_ROUNDTRIPCORRECTION, getPropertySpecService()::bigDecimalSpec)
                .setDefaultValue(DEFAULT_ROUND_TRIP_CORRECTION)
                .finish();
    }

}