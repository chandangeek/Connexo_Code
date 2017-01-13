package test.com.energyict.protocolimplv2.sdksample;

import com.energyict.cpo.MdwToUplPropertySpecAdapter;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/05/13
 * Time: 9:59
 */
public class SDKCalendarTaskProtocolDialectProperties extends AbstractDeviceProtocolDialect {

    public static final String activeCalendarName = "activeCalendarName";
    public static final String passiveCalendarName = "passiveCalendarName";

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.SDK_SAMPLE_CALENDAR.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return "SDK dialect for calendar testing";
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                MdwToUplPropertySpecAdapter.adapt(PropertySpecFactory.stringPropertySpec(activeCalendarName)),
                MdwToUplPropertySpecAdapter.adapt(PropertySpecFactory.stringPropertySpec(passiveCalendarName))
        );
    }
}