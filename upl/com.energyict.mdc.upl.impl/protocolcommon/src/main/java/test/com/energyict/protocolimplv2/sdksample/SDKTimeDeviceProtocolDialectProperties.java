package test.com.energyict.protocolimplv2.sdksample;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.Arrays;
import java.util.List;

/**
 * A set of properties related to the TimeSetting
 * <p/>
 * Copyrights EnergyICT
 * Date: 5/02/13
 * Time: 15:51
 */
public class SDKTimeDeviceProtocolDialectProperties extends AbstractDeviceProtocolDialect {

    public static final String CLOCK_OFFSET_TO_WRITE_PROPERTY_NAME = "ClockOffsetWhenReading";
    public static final String CLOCK_OFFSET_TO_READ_PROPERTY_NAME = "ClockOffsetWhenWriting";

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.SDK_SAMPLE_TIME_DEVICE_PROTOCOL_DIALECT_NAME.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return "SDK dialect for time testing";
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                    this.getClockOffsetToReadPropertySpec(),
                    this.getClockOffsetToWritePropertySpec());
    }

    private PropertySpec getClockOffsetToWritePropertySpec() {
        return this.durationPropertySpec(CLOCK_OFFSET_TO_WRITE_PROPERTY_NAME);
    }

    private PropertySpec getClockOffsetToReadPropertySpec() {
        return this.durationPropertySpec(CLOCK_OFFSET_TO_READ_PROPERTY_NAME);
    }

    private PropertySpec durationPropertySpec(String name) {
        return Services
                .propertySpecService()
                .durationSpec()
                .named(name, name)
                .describedAs("Description for " + name)
                .finish();
    }

}
