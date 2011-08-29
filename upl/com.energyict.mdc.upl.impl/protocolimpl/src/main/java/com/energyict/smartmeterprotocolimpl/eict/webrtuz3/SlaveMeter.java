package com.energyict.smartmeterprotocolimpl.eict.webrtuz3;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.smartmeterprotocolimpl.common.SimpleMeter;

import java.io.IOException;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 3-mrt-2011
 * Time: 16:32:09
 */
public abstract class SlaveMeter implements SimpleMeter, MessageProtocol{

    public abstract MessageProtocol getMessageProtocol();

    private final WebRTUZ3 meterProtocol;
    private final String serialNumber;
    private final int physicalAddress;

    /**
     * Default constructor for EIServer instantiations
     */
    public SlaveMeter(){
        this(null, null, -1);
    }

    public SlaveMeter(WebRTUZ3 meterProtocol, String serialNumber, int physicalAddress) {
        this.meterProtocol = meterProtocol;
        this.serialNumber = serialNumber;
        this.physicalAddress = physicalAddress;
    }

    /**
     * Return the DeviceTimeZone
     *
     * @return the DeviceTimeZone
     */
    public TimeZone getTimeZone() {
        return this.meterProtocol.getTimeZone();
    }

    /**
     * Getter for the used Logger
     *
     * @return the Logger
     */
    public Logger getLogger() {
        return this.meterProtocol.getLogger();
    }

    /**
     * The serialNumber of the meter
     *
     * @return the serialNumber of the meter
     */
    public String getSerialNumber() {
        return this.serialNumber;
    }

    /**
     * Get the physical address of the Meter. Mostly this will be an index of the meterList
     *
     * @return the physical Address of the Meter.
     */
    public int getPhysicalAddress() {
        return this.physicalAddress;
    }

    /**
     * Provides the full list of outstanding messages to the protocol.
     * If for any reason certain messages have to be grouped before they are sent to a device, then this is the place to do it.
     * At a later timestamp the framework will query each {@link com.energyict.protocol.MessageEntry} (see {@link #queryMessage(com.energyict.protocol.MessageEntry)}) to actually
     * perform the message.
     *
     * @param messageEntries a list of {@link com.energyict.protocol.MessageEntry}s
     * @throws java.io.IOException if a logical error occurs
     */
    public void applyMessages(final List messageEntries) throws IOException {
        // nothing to do
    }

    /**
     * Indicates that each message has to be executed by the protocol.
     *
     * @param messageEntry a definition of which message needs to be sent
     * @return a state of the message which was just sent
     * @throws java.io.IOException if a logical error occurs
     */
    public MessageResult queryMessage(final MessageEntry messageEntry) throws IOException {
        return this.meterProtocol.queryMessage(messageEntry);
    }

    public String writeMessage(final Message msg) {
        return getMessageProtocol().writeMessage(msg);
    }

    public String writeTag(final MessageTag tag) {
        return getMessageProtocol().writeTag(tag);
    }

    public String writeValue(final MessageValue value) {
        return getMessageProtocol().writeValue(value);
    }

    public List getMessageCategories() {
        return getMessageProtocol().getMessageCategories();
    }

    public CosemObjectFactory getCosemObjectFactory(){
        return this.meterProtocol.getDlmsSession().getCosemObjectFactory();
    }

    public DLMSMeterConfig getMeterConfig(){
        return this.meterProtocol.getDlmsSession().getMeterConfig();
    }
}
