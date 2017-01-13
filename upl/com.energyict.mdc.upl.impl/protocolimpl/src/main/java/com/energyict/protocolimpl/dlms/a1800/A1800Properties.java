
package com.energyict.protocolimpl.dlms.a1800;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;

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
import com.energyict.protocolimpl.dlms.common.ObisCodePropertySpec;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Properties management of A1800
 * <p/>
 * Created by heuckeg on 27.06.2014.
 */
class A1800Properties extends DlmsProtocolProperties implements DlmsSessionProperties {

    private static final String PROPNAME_LOAD_PROFILE_OBIS_CODE = "LoadProfileObisCode";
    private static final String PROPNAME_SEND_PREFIX = "SendPrefix";
    private static final String PROPNAME_SN = "SerialNumber";
    private static final String PROPNAME_SERVER_UPPER_MAC_ADDRESS = "ServerUpperMacAddress";
    private static final String PROPNAME_SERVER_LOWER_MAC_ADDRESS = "ServerLowerMacAddress";
    private static final String PROPNAME_APPLY_TRANSFORMER_RATIOS = "ApplyTransformerRatios";

    private static final String READ_SERIAL_NUMBER = "ReadSerialNumber";

    private final PropertySpecService propertySpecService;
    private InvokeIdAndPriorityHandler invokeIdAndPriorityHandler = null;

    A1800Properties(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.integerSpec(SECURITY_LEVEL),
                this.integerSpec(CLIENT_MAC_ADDRESS),
                this.integerSpec(PROPNAME_SERVER_LOWER_MAC_ADDRESS),
                this.integerSpec(PROPNAME_SERVER_UPPER_MAC_ADDRESS),
                this.integerSpec(READ_SERIAL_NUMBER),
                this.integerSpec(PROPNAME_SEND_PREFIX),
                new ObisCodePropertySpec(PROPNAME_LOAD_PROFILE_OBIS_CODE, false),
                this.integerSpec(PROPNAME_APPLY_TRANSFORMER_RATIOS));
    }

    private <T> PropertySpec spec(String name, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, optionsSupplier).finish();
    }

    private PropertySpec stringSpec(String name) {
        return this.spec(name, this.propertySpecService::stringSpec);
    }

    private PropertySpec integerSpec(String name) {
        return this.spec(name, this.propertySpecService::integerSpec);
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
        return getIntProperty(PK_TIMEOUT, "10000");
    }

    @ProtocolProperty
    public final boolean isReadSerialNumber() {
        return getBooleanProperty(READ_SERIAL_NUMBER, "0");
    }

    @ProtocolProperty
    public final ObisCode getLoadProfileObiscode() {
        final String obisString = getStringValue(PROPNAME_LOAD_PROFILE_OBIS_CODE, "");
        if (obisString.isEmpty()) {
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