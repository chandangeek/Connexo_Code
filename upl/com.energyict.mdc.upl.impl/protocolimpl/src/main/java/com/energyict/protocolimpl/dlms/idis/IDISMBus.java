package com.energyict.protocolimpl.dlms.idis;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;

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
        ObisCode obisCode = MBUS_CLIENT_OBISCODE;
        for (int i = 1; i <= MAX_MBUS_CHANNELS; i++) {
            try {
                obisCode = ProtocolTools.setObisCodeField(obisCode, 1, (byte) i);
                serial = String.valueOf(getCosemObjectFactory().getData(obisCode).getAttrbAbstractDataType(6).getInteger32().longValue());
                if (serial.contains(new String(getSystemIdentifier()))) {
                    setGasSlotId(i);
                }
            } catch (IOException e) {
                // fetch next
            }
        }

        if (getGasSlotId() == -1) {
            throw new IOException("No MBus device found with serialNumber " + getInfoTypeSerialNumber() + " on the E-meter.");
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

    protected IDISMessageHandler getMessageHandler() {
        if (messageHandler == null) {
            messageHandler = new IDISMBusMessageHandler(this);
        }
        return messageHandler;
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        if (isMBusValueChannel(obisCode)) {  //Extended register
            UniversalObject uo = getMeterConfig().findObject(obisCode);
            if (uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
                ExtendedRegister register = getCosemObjectFactory().getExtendedRegister(obisCode);
                AbstractDataType value = register.getAttrbAbstractDataType(2);
                if (value instanceof Unsigned32) {
                    return new RegisterValue(obisCode, new Quantity(value.getUnsigned32().intValue(), register.getScalerUnit().getUnit()));
                } else if (value instanceof OctetString) {
                    return new RegisterValue(obisCode, ((OctetString) value).stringValue());
                } else {
                    throw new NoSuchRegisterException();
                }
            } else if (uo.getClassID() == DLMSClassId.DATA.getClassId()) {
                final Data register = getCosemObjectFactory().getData(obisCode);
                OctetString octetString = register.getValueAttr().getOctetString();
                if (octetString != null && octetString.stringValue() != null) {
                    return new RegisterValue(obisCode, octetString.stringValue());
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