package test.com.energyict.protocolimplV2.nta.abstractnta;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedData;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.mdc.shadow.messages.DeviceMessageShadow;
import com.energyict.mdw.core.Pluggable;
import com.energyict.smartmeterprotocolimpl.common.SimpleMeter;
import test.com.energyict.protocolimplV2.nta.elster.AM100;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * @author: sva
 * @since: 2/11/12 (9:11)
 */
public abstract class AbstractNtaMbusDevice implements Pluggable, SimpleMeter, DeviceMessageSupport {

    private final AbstractNtaProtocol meterProtocol;

    private final String serialNumber;
    private final int physicalAddress;

    /**
     * Get the used MessageProtocol
     *
     * @return the DeviceMessageSupport message protocol
     */

    public abstract DeviceMessageSupport getMessageProtocol();

    public AbstractNtaMbusDevice() {
        this.meterProtocol = new AM100();
        this.serialNumber = "CurrentlyUnKnown";
        this.physicalAddress = -1;
    }

    public AbstractNtaMbusDevice(final AbstractNtaProtocol meterProtocol, final String serialNumber, final int physicalAddress) {
        this.meterProtocol = meterProtocol;
        this.serialNumber = serialNumber;
        this.physicalAddress = physicalAddress;
    }

    @Override
    public TimeZone getTimeZone() {
        return this.meterProtocol.getTimeZone();
    }

    @Override
    public Logger getLogger() {
        return this.meterProtocol.getLogger();
    }

    @Override
    public String getSerialNumber() {
        return this.serialNumber;
    }

    @Override
    public int getPhysicalAddress() {
        return this.physicalAddress;
    }

    public AbstractNtaProtocol getMeterProtocol() {
        return meterProtocol;
    }

    @Override
    public void addProperties(TypedProperties properties) {
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return new ArrayList<PropertySpec>();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return new ArrayList<PropertySpec>();
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getMessageProtocol().getSupportedMessages();
    }

    @Override
    public CollectedMessage executePendingMessages(List<DeviceMessageShadow> pendingMessages) {
        return getMessageProtocol().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedData updateSentMessages(List<DeviceMessageShadow> sentMessages) {
        return getMessageProtocol().updateSentMessages(sentMessages);
    }
}
