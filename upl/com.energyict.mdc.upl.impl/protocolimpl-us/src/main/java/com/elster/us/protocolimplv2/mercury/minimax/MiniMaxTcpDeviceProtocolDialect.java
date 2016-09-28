package com.elster.us.protocolimplv2.mercury.minimax;

import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.mdc.tasks.DeviceProtocolDialect;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Models a {@link DeviceProtocolDialect} for a TCP connection type
 *
 * @author: khe
 * @since: 16/10/12 (113:25)
 */
public class MiniMaxTcpDeviceProtocolDialect implements DeviceProtocolDialect {

    public static final String RETRIES = "Retries";
    public static final String TIMEOUT = "Timeout";
    public final static String ROUND_TRIP_CORRECTION = "RoundTripCorrection";

    public static final int DEFAULT_TCP_TIMEOUT = 30;

    public final static int DEFAULT_ROUND_TRIP_CORRECTION = 0;
    public static final PropertySpec<BigDecimal> RETRIES_PROPERTY = PropertySpecFactory.bigDecimalPropertySpec(RETRIES, new BigDecimal(3));

    @Override
    public String getDeviceProtocolDialectName() {
        return "TcpMercuryDialect";
    }

    @Override
    public boolean isRequiredProperty(String name) {
        for (PropertySpec propertySpec : getRequiredProperties()) {
            if (propertySpec.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDisplayName() {
        return "TCP/IP";
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

    protected PropertySpec retriesPropertySpec() {
        // TODO
        return RETRIES_PROPERTY;
    }

    protected PropertySpec timeoutPropertySpec() {
        return PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(TIMEOUT, new TimeDuration(DEFAULT_TCP_TIMEOUT));
    }

    protected PropertySpec roundTripCorrectionPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(ROUND_TRIP_CORRECTION, new BigDecimal(DEFAULT_ROUND_TRIP_CORRECTION));
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        switch (name) {
            case RETRIES:
                return this.retriesPropertySpec();
            case TIMEOUT:
                return this.timeoutPropertySpec();
            case ROUND_TRIP_CORRECTION:
                return this.roundTripCorrectionPropertySpec();
            default:
                return null;
        }
    }
}
