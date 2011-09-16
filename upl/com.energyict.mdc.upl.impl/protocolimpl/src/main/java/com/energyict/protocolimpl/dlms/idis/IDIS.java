package com.energyict.protocolimpl.dlms.idis;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.Register;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.dlms.AbstractDLMSProtocol;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 5/09/11
 * Time: 9:37
 */
public class IDIS extends AbstractDLMSProtocol {

    private static ObisCode FIRMWARE_VERSION = ObisCode.fromString("1.0.0.2.0.255");

    @Override
    public Date getTime() throws IOException {
        return getCosemObjectFactory().getClock().getDateTime();
    }

    @Override
    public void setTime() throws IOException {
        final Calendar newTimeToSet = Calendar.getInstance(getTimeZone());
        getCosemObjectFactory().getClock().setTimeAttr(new DateTime(newTimeToSet));
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {


        final UniversalObject uo = getMeterConfig().findObject(obisCode);
        if (uo.getClassID() == DLMSClassId.REGISTER.getClassId()) {
            final Register register = getCosemObjectFactory().getRegister(obisCode);
            return new RegisterValue(obisCode, register.getQuantityValue());
        } else if (uo.getClassID() == DLMSClassId.DEMAND_REGISTER.getClassId()) {
            final DemandRegister register = getCosemObjectFactory().getDemandRegister(obisCode);
            return new RegisterValue(obisCode, register.getQuantityValue());
        } else if (uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
            final ExtendedRegister register = getCosemObjectFactory().getExtendedRegister(obisCode);
            return new RegisterValue(obisCode, register.getQuantityValue());
        } else if (uo.getClassID() == DLMSClassId.DISCONNECT_CONTROL.getClassId()) {
            final Disconnector register = getCosemObjectFactory().getDisconnector(obisCode);
            return new RegisterValue(obisCode, "" + register.getState());
        } else {
            throw new NoSuchRegisterException();
        }
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo("");
    }

    @Override
    protected List doGetOptionalKeys() {
        List<String> optional = new ArrayList<String>();
        optional.add(DlmsProtocolProperties.CLIENT_MAC_ADDRESS);
        optional.add(PROPNAME_SERVER_LOWER_MAC_ADDRESS);
        optional.add(PROPNAME_SERVER_UPPER_MAC_ADDRESS);
        optional.add(DlmsProtocolProperties.CONNECTION);
        optional.add(DlmsProtocolProperties.TIMEOUT);
        optional.add(DlmsProtocolProperties.ADDRESSING_MODE);
        optional.add(DlmsProtocolProperties.RETRIES);
        return optional;
    }

    @Override
    public String getProtocolVersion() {
        return "$Date$";
    }

    @Override
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        Data data = getCosemObjectFactory().getData(FIRMWARE_VERSION);
        return data.getString();
    }

    public boolean isRequestTimeZone() {
        return false;
    }

    public int getRoundTripCorrection() {
        return 0;
    }

    @Override
    public int getProfileInterval() throws UnsupportedException, IOException {
        return 900;     //TODO
    }

    public int getReference() {
        return ProtocolLink.LN_REFERENCE;
    }

    public StoredValues getStoredValues() {
        return null;
    }

    @Override
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return 0;       //TODO
    }
}