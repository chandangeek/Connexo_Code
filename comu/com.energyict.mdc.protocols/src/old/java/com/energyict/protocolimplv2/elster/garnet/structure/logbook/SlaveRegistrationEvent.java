package com.energyict.protocolimplv2.elster.garnet.structure.logbook;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Address;
import com.energyict.protocolimplv2.elster.garnet.structure.field.PaddingData;
import com.energyict.protocolimplv2.elster.garnet.structure.field.RegistrationFunction;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class SlaveRegistrationEvent extends AbstractField<SlaveRegistrationEvent> implements  LogBookEvent{

    public static final int LENGTH = 16;
    private static final boolean BIG_ENDIAN = true;

    private Address slaveId;
    private RegistrationFunction registrationFunction;
    private PaddingData paddingData;

    public SlaveRegistrationEvent() {
        this.slaveId = new Address(BIG_ENDIAN);
        this.registrationFunction = new RegistrationFunction();
        this.paddingData = new PaddingData(13);
    }

    public SlaveRegistrationEvent(RegistrationFunction registrationFunction, Address slaveId) {
        this.registrationFunction = registrationFunction;
        this.slaveId = slaveId;
        this.paddingData = new PaddingData(13);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                slaveId.getBytes(),
                registrationFunction.getBytes(),
                paddingData.getBytes()
        );
    }

    @Override
    public SlaveRegistrationEvent parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        this.slaveId.parse(rawData, ptr);
        ptr += slaveId.getLength();

        this.registrationFunction.parse(rawData, ptr);
        ptr += registrationFunction.getLength();

        this.paddingData.parse(rawData, ptr);
        ptr += paddingData.getLength();

        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    @Override
    public String getEventDescription() {
        return registrationFunction.getFunctionId() == 0
                ? "Unregister of concentrator with ID " + slaveId.getAddress()
                : "Register of concentrator with ID " + slaveId.getAddress();
    }

    public RegistrationFunction getRegistrationFunction() {
        return registrationFunction;
    }

    public Address getSlaveId() {
        return slaveId;
    }

    public PaddingData getPaddingData() {
        return paddingData;
    }
}