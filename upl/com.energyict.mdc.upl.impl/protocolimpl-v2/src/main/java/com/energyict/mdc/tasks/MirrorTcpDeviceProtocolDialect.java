package com.energyict.mdc.tasks;

import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.energyict.dlms.common.DlmsProtocolProperties.*;

/**
 * Models a {@link DeviceProtocolDialect} for a TCP connection type to a Beacon 3100 device.
 * Note that, using this dialect, the protocol will read out the mirrored (cached) meter data from the Beacon DC.
 * No communication is done to the actual meter.
 *
 * @author: khe
 */
public class MirrorTcpDeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    public static final int DEFAULT_TCP_TIMEOUT = 30;
    public static final String BEACON_DC_MIRROR_TCP_DLMS = "Beacon DC Mirror TCP DLMS";

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.BEACON_MIRROR_TCP_DLMS_PROTOCOL_DIALECT_NAME.getName();
    }

    @Override
    public String getDisplayName() {
        return BEACON_DC_MIRROR_TCP_DLMS;
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
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
        return PropertySpecFactory.bigDecimalPropertySpec(RETRIES, BigDecimal.ZERO);
    }

    protected PropertySpec timeoutPropertySpec() {
        return PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(TIMEOUT, new TimeDuration(DEFAULT_TCP_TIMEOUT));
    }

    protected PropertySpec roundTripCorrectionPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(ROUND_TRIP_CORRECTION, DEFAULT_ROUND_TRIP_CORRECTION);
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        switch (name) {
            case DlmsProtocolProperties.RETRIES:
                return this.retriesPropertySpec();
            case DlmsProtocolProperties.TIMEOUT:
                return this.timeoutPropertySpec();
            case DlmsProtocolProperties.ROUND_TRIP_CORRECTION:
                return this.roundTripCorrectionPropertySpec();
            default:
                return null;
        }
    }
}
