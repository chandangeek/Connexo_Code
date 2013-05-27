package com.energyict.protocolimpl.dlms.idis;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 28/09/11
 * Time: 17:07
 */
public class IDISMBus extends IDIS {

    private int gasMeterSlot = -1;
    private MBusProfileDataReader mBusProfileDataReader = null;
    private IDISMessageHandler messageHandler = null;

    private static final ObisCode OBISCODE_MBUS_LOAD_PROFILE = ObisCode.fromString("0.0.24.3.0.255");
    private static final ObisCode MBUS_CLIENT_OBISCODE = ObisCode.fromString("0.1.24.1.0.255");
    private static final int MAX_MBUS_CHANNELS = 4;

    @Override
    public void connect() throws IOException {
        super.connect();

        // search for the channel of the Mbus Device
        String serial;
        List<String> receivedSerialNumbers = new ArrayList<String>();
        ObisCode obisCode = MBUS_CLIENT_OBISCODE;
        String expectedSerialNumber = new String(getCalledAPTitle());
        for (int i = 1; i <= MAX_MBUS_CHANNELS; i++) {
            try {
                obisCode = ProtocolTools.setObisCodeField(obisCode, 1, (byte) i);
                long serialNumberValue = getCosemObjectFactory().getMbusClient(obisCode, MbusClientAttributes.VERSION10).getIdentificationNumber().getValue();
                if (serialNumberValue != 0) {
                    serial = ProtocolTools.getHexStringFromInt((int) serialNumberValue, 4, "");
                    receivedSerialNumbers.add(serial);
                    if (serial.equals(expectedSerialNumber)) {
                    setGasSlotId(i);
                        break;
                }
                }
            } catch (IOException e) {
                // fetch next
            }
        }

        if (getGasSlotId() == -1) {
            StringBuilder sb = new StringBuilder();
            for (String receivedSerialNumber : receivedSerialNumbers) {
                sb.append("'").append(receivedSerialNumber).append("'");
                if (receivedSerialNumbers.indexOf(receivedSerialNumber) == (receivedSerialNumbers.size() - 1)) {    //Last element
                    sb.append(".");
                } else if (receivedSerialNumbers.indexOf(receivedSerialNumber) == (receivedSerialNumbers.size() - 2)) {    //Second last element
                    sb.append(" or ");
                } else {
                    sb.append(", ");
        }
    }
            if (receivedSerialNumbers.isEmpty()) {
                throw new IOException("No MBus device found with serialNumber '" + expectedSerialNumber + "' on the E-meter. No MBus devices are connected.");
            }
            throw new IOException("No MBus device found with serialNumber '" + expectedSerialNumber + "' on the E-meter. Expected " + sb.toString());
        }
    }

    public ObisCode getLoadProfileObisCode() {
        return ProtocolTools.setObisCodeField(OBISCODE_MBUS_LOAD_PROFILE, 1, (byte) getGasSlotId());
    }

    private MBusProfileDataReader getMBusProfileDataReader() {
        if (mBusProfileDataReader == null) {
            mBusProfileDataReader = new MBusProfileDataReader(this);
        }
        return mBusProfileDataReader;
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
        return getMBusProfileDataReader().getProfileData(from, to, includeEvents);
    }

    @Override
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        ProfileGeneric profileGeneric = getCosemObjectFactory().getProfileGeneric(getLoadProfileObisCode());
        return getMBusProfileDataReader().getChannelInfo(profileGeneric.getCaptureObjects()).size();
    }

    protected IDISMessageHandler getMessageHandler() {
        if (messageHandler == null) {
            messageHandler = new IDISMBusMessageHandler(this);
        }
        return messageHandler;
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCode originalObisCode = ObisCode.fromByteArray(obisCode.getLN());
        if (isMBusValueChannel(obisCode)) {  //Extended register
            obisCode = ProtocolTools.setObisCodeField(obisCode, 1, (byte) getGasSlotId());
            UniversalObject uo = getMeterConfig().findObject(obisCode);
            if (uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
                ExtendedRegister register = getCosemObjectFactory().getExtendedRegister(obisCode);
                AbstractDataType value = register.getAttrbAbstractDataType(2);
                if (value instanceof Unsigned32) {
                    return new RegisterValue(originalObisCode, new Quantity(value.getUnsigned32().intValue(), register.getScalerUnit().getEisUnit()));
                } else if (value instanceof OctetString) {
                    return new RegisterValue(originalObisCode, ((OctetString) value).stringValue());
                } else {
                    throw new NoSuchRegisterException();
                }
            } else if (uo.getClassID() == DLMSClassId.DATA.getClassId()) {
                final Data register = getCosemObjectFactory().getData(obisCode);
                OctetString octetString = register.getValueAttr().getOctetString();
                if (octetString != null && octetString.stringValue() != null) {
                    return new RegisterValue(originalObisCode, octetString.stringValue());
                }
                throw new NoSuchRegisterException();
            }
        }
        return super.readRegister(obisCode);
    }

    private boolean isMBusValueChannel(ObisCode obisCode) {
        return ((obisCode.getA() == 0) && (obisCode.getB() > 0 && obisCode.getB() < 5) && (obisCode.getC() == 24) && (obisCode.getD() == 2) && (obisCode.getE() > 0 && obisCode.getE() < 5) && obisCode.getF() == 255);
    }

    public void setGasSlotId(int slotId) {
        this.gasMeterSlot = slotId;
    }

    /**
     * Indicates the B-field in the MBus obis codes.
     *
     * @return B-field value
     */
    public int getGasSlotId() {
        return gasMeterSlot;
    }
}