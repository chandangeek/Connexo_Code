package test.com.energyict.protocolimplV2.nta.elster;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import test.com.energyict.protocolimplV2.nta.abstractnta.AbstractNtaMbusDevice;
import test.com.energyict.protocolimplV2.nta.abstractnta.AbstractNtaProtocol;

/**
 * @author: sva
 * @since: 2/11/12 (11:26)
 */
public class MbusDevice extends AbstractNtaMbusDevice {

    private DeviceMessageSupport messageProtocol;

    public MbusDevice() {
        super();
    }

    public MbusDevice(final AbstractNtaProtocol meterProtocol, final String serialNumber, final int physicalAddress) {
        super(meterProtocol, serialNumber, physicalAddress);
    }

    @Override
    public DeviceMessageSupport getMessageProtocol() {
        if (messageProtocol == null) {
            // messageProtocol = Dsmr23MbusMessaging(); ToDo
        }
        return messageProtocol;
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }
}
