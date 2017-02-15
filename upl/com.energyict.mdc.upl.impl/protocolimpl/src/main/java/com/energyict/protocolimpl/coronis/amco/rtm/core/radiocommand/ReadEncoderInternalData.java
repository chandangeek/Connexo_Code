package com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.core.parameter.EncoderModel;
import com.energyict.protocolimpl.coronis.amco.rtm.core.parameter.EncoderUnit;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 8-apr-2011
 * Time: 10:22:22
 */
public class ReadEncoderInternalData extends AbstractRadioCommand {

    protected ReadEncoderInternalData(PropertySpecService propertySpecService, RTM rtm, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
    }

    private EncoderModel encoderModelOnPortA;
    private EncoderModel encoderModelOnPortB;
    private int lengthA;
    private int lengthB;

    private int identifierA;
    private int identifierB;
    private String meterValueA;
    private String meterValueB;
    private String serialNumberA;
    private String serialNumberB;
    private EncoderUnit unitA;
    private EncoderUnit unitB;
    private int encodedWheelDigitsA;
    private int encodedWheelDigitsB;
    private int digitsBeforeDecimalPointA;
    private int digitsBeforeDecimalPointB;
    private int optionA;
    private int optionB;
    private int manufacturerAdapterCodeA;
    private int manufacturerAdapterCodeB;

    public int getDigitsBeforeDecimalPointA() {
        return digitsBeforeDecimalPointA;
    }

    public int getDigitsBeforeDecimalPointB() {
        return digitsBeforeDecimalPointB;
    }

    public int getEncodedWheelDigitsA() {
        return encodedWheelDigitsA;
    }

    public int getEncodedWheelDigitsB() {
        return encodedWheelDigitsB;
    }

    public EncoderModel getEncoderModelOnPortA() {
        return encoderModelOnPortA;
    }

    public EncoderModel getEncoderModelOnPortB() {
        return encoderModelOnPortB;
    }

    public int getIdentifierA() {
        return identifierA;
    }

    public int getIdentifierB() {
        return identifierB;
    }

    public int getLengthA() {
        return lengthA;
    }

    public int getLengthB() {
        return lengthB;
    }

    public int getManufacturerAdapterCodeA() {
        return manufacturerAdapterCodeA;
    }

    public int getManufacturerAdapterCodeB() {
        return manufacturerAdapterCodeB;
    }

    public String getMeterValueA() {
        return meterValueA;
    }

    public String getMeterValueB() {
        return meterValueB;
    }

    public int getOptionA() {
        return optionA;
    }

    public int getOptionB() {
        return optionB;
    }

    public String getSerialNumberA() {
        return serialNumberA;
    }

    public String getSerialNumberB() {
        return serialNumberB;
    }

    public EncoderUnit getUnitA() {
        return unitA;
    }

    public EncoderUnit getUnitB() {
        return unitB;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        encoderModelOnPortA = new EncoderModel(getPropertySpecService(), getRTM(), getNlsService());
        encoderModelOnPortA.parse(ProtocolTools.getSubArray(data, offset, offset + 2));
        offset += 2;

        encoderModelOnPortB = new EncoderModel(getPropertySpecService(), getRTM(), getNlsService());
        encoderModelOnPortB.parse(ProtocolTools.getSubArray(data, offset, offset + 2));
        offset += 2;

        lengthA = data[offset++] & 0xFF;
        lengthB = data[offset++] & 0xFF;

        if (lengthA > 0) {
            identifierA = data[offset++] & 0xFF;
            meterValueA = new String(ProtocolTools.getSubArray(data, offset, offset + 6));
            offset += 6;
            serialNumberA = new String(ProtocolTools.getSubArray(data, offset, offset + 10));
            offset += 10;
            unitA = new EncoderUnit(getPropertySpecService(), getRTM(), getNlsService());
            unitA.parse(ProtocolTools.getSubArray(data, offset, offset + 2));
            offset += 2;
            encodedWheelDigitsA = data[offset++] & 0xFF;
            digitsBeforeDecimalPointA = data[offset++] & 0xFF;
            optionA = ProtocolTools.getUnsignedIntFromBytes(data, offset, 2);
            offset += 2;
            manufacturerAdapterCodeA = ProtocolTools.getUnsignedIntFromBytes(data, offset, 2);
            offset += 2;

            offset += 2;      //Skip the error code
            offset += 2;      //Skip the checksum
            offset++;         //Skip the carriage return
        }

        if (lengthB > 0) {
            identifierB = data[offset++] & 0xFF;
            meterValueB = new String(ProtocolTools.getSubArray(data, offset, offset + 6));
            offset += 6;
            serialNumberB = new String(ProtocolTools.getSubArray(data, offset, offset + 10));
            offset += 10;
            unitB = new EncoderUnit(getPropertySpecService(), getRTM(), getNlsService());
            unitB.parse(ProtocolTools.getSubArray(data, offset, offset + 2));
            offset += 2;
            encodedWheelDigitsB = data[offset++] & 0xFF;
            digitsBeforeDecimalPointB = data[offset++] & 0xFF;
            optionB = ProtocolTools.getUnsignedIntFromBytes(data, offset, 2);
            offset += 2;
            manufacturerAdapterCodeB = ProtocolTools.getUnsignedIntFromBytes(data, offset, 2);
            offset += 2;

            offset += 2;      //Skip the error code
            offset += 2;      //Skip the checksum
            offset++;         //Skip the carriage return
        }
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ReadEncoderInternalData;
    }
}
