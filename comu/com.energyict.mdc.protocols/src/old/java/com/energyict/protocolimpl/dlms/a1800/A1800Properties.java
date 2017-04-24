
package com.energyict.protocolimpl.dlms.a1800;

import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;

import com.energyict.dlms.ConnectionMode;
import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.DlmsSessionProperties;
import com.energyict.dlms.InvokeIdAndPriorityHandler;
import com.energyict.dlms.NonIncrementalInvokeIdAndPriorityHandler;
import com.energyict.dlms.aso.LocalSecurityProvider;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Properties management of A1800
 * <p/>
 * Created by heuckeg on 27.06.2014.
 */
public class A1800Properties extends DlmsProtocolProperties implements DlmsSessionProperties {

    private static final String PROPNAME_LOAD_PROFILE_OBIS_CODE = "LoadProfileObisCode";
    private static final String PROPNAME_SEND_PREFIX = "SendPrefix";
    private static final String PROPNAME_SN = "SerialNumber";
    private static final String PROPNAME_SERVER_UPPER_MAC_ADDRESS = "ServerUpperMacAddress";
    private static final String PROPNAME_SERVER_LOWER_MAC_ADDRESS = "ServerLowerMacAddress";
    private static final String PROPNAME_APPLY_TRANSFORMER_RATIOS = "ApplyTransformerRatios";

    public static final String READ_SERIAL_NUMBER = "ReadSerialNumber";

    InvokeIdAndPriorityHandler invokeIdAndPriorityHandler = null;

    public List<String> getOptionalKeys() {
        List<String> optional = new ArrayList<String>();
        optional.add(CLIENT_MAC_ADDRESS);
        optional.add(PROPNAME_SERVER_LOWER_MAC_ADDRESS);
        optional.add(PROPNAME_SERVER_UPPER_MAC_ADDRESS);
        optional.add(SECURITY_LEVEL);
        optional.add(READ_SERIAL_NUMBER);
        optional.add(PROPNAME_SEND_PREFIX);
        optional.add(PROPNAME_LOAD_PROFILE_OBIS_CODE);
        optional.add(PROPNAME_APPLY_TRANSFORMER_RATIOS);
        return optional;
    }

    public List<String> getRequiredKeys() {
        List<String> required = new ArrayList<String>();
        return required;
    }

    protected void doValidateProperties() throws MissingPropertyException, InvalidPropertyException {
        final String obisString = getStringValue(PROPNAME_LOAD_PROFILE_OBIS_CODE, "");
        if (obisString.length() > 0) {
            ObisCode obisCode = ObisCode.fromString(obisString);
            if (!obisCode.equals(A1800Profile.LOAD_PROFILE_EU_CUMULATIVE) &&
                    !obisCode.equals(A1800Profile.LOAD_PROFILE_PULSES) &&
                    !obisCode.equals(A1800Profile.LOAD_PROFILE_EU_NONCUMULATIVE) &&
                    !obisCode.equals(A1800Profile.PROFILE_INSTRUMENTATION_SET1) &&
                    !obisCode.equals(A1800Profile.PROFILE_INSTRUMENTATION_SET2)) {
                throw new InvalidPropertyException(String.format("Illegal argument for '%s':%s", PROPNAME_LOAD_PROFILE_OBIS_CODE, obisString));
            }
        }
    }

    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    public ConnectionMode getConnectionMode() {
        return ConnectionMode.HDLC;
    }

    public String getSecurityLevel() {
        return getStringValue(SECURITY_LEVEL, "1:0");
    }

    public int getClientMacAddress() {
        return getIntProperty(CLIENT_MAC_ADDRESS, "17");
    }

    public int getUpperHDLCAddress() {
        return getIntProperty(PROPNAME_SERVER_UPPER_MAC_ADDRESS, "1");
    }

    public int getLowerHDLCAddress() {
        return getIntProperty(PROPNAME_SERVER_LOWER_MAC_ADDRESS, "16");
    }

    public int getAddressingMode() {
        return 4;
    }

    public int getInformationFieldSize() {
        return 0x7EE;
    }

    public InvokeIdAndPriorityHandler getInvokeIdAndPriorityHandler() {
        if (invokeIdAndPriorityHandler == null) {
            invokeIdAndPriorityHandler = new NonIncrementalInvokeIdAndPriorityHandler();
        }
        return invokeIdAndPriorityHandler;
    }

    public SecurityProvider getSecurityProvider() {
        return new LocalSecurityProvider(this);
    }

    public String getPassword()
    {
        return null;
    }

    public String getDeviceId()
    {
        return null;
    }

    public String getNodeAddress()
    {
        return null;
    }

    @ProtocolProperty
    public String getSerialNumber() {
        return getStringValue(PROPNAME_SN, "");
    }

    public int getTimeout() {
        return getIntProperty(TIMEOUT, "10000");
    }

    @ProtocolProperty
    public final boolean isReadSerialNumber() {
        return getBooleanProperty(READ_SERIAL_NUMBER, "0");
    }

    @ProtocolProperty
    public final ObisCode getLoadProfileObiscode() {
        final String obisString = getStringValue(PROPNAME_LOAD_PROFILE_OBIS_CODE, "");
        if (obisString.length() == 0) {
            return A1800Profile.LOAD_PROFILE_PULSES;
        } else {
            return ObisCode.fromString(obisString);
        }
    }

    @ProtocolProperty
    public final boolean sendPrefix() {
        return getBooleanProperty(PROPNAME_SEND_PREFIX, "0");
    }

    @Override
    @ProtocolProperty
    public String getManufacturer() {
        return "ELS";
    }

    public boolean needToApplyTransformerRatios() {
        return getBooleanProperty(PROPNAME_APPLY_TRANSFORMER_RATIOS, "0");
    }
}
