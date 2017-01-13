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
public class SDKFirmwareTaskProtocolDialectProperties extends AbstractDeviceProtocolDialect {

    public static final String activeMeterFirmwareVersion = "activeMeterFirmwareVersion";
    public static final String passiveMeterFirmwareVersion = "passiveMeterFirmwareVersion";
    public static final String activeCommunicationFirmwareVersion = "activeCommunicationFirmwareVersion";
    public static final String passiveCommunicationFirmwareVersion = "passiveCommunicationFirmwareVersion";

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.SDK_SAMPLE_FIRMWARE.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return "SDK dialect for firmware testing";
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                MdwToUplPropertySpecAdapter.adapt(PropertySpecFactory.stringPropertySpec(activeMeterFirmwareVersion)),
                MdwToUplPropertySpecAdapter.adapt(PropertySpecFactory.stringPropertySpec(passiveMeterFirmwareVersion)),
                MdwToUplPropertySpecAdapter.adapt(PropertySpecFactory.stringPropertySpec(activeCommunicationFirmwareVersion)),
                MdwToUplPropertySpecAdapter.adapt(PropertySpecFactory.stringPropertySpec(passiveCommunicationFirmwareVersion)));
    }
}