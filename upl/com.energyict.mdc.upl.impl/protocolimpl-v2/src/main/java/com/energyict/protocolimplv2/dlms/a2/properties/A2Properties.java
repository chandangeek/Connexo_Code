package com.energyict.protocolimplv2.dlms.a2.properties;

import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.mdc.protocol.security.AdvancedDeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.properties.HexString;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import java.math.BigDecimal;

public class A2Properties extends DlmsProperties {


    public A2Properties() {
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (securityProvider == null) {
            securityProvider = new A2SecurityProvider(getProperties(), getSecurityPropertySet().getAuthenticationDeviceAccessLevel(), DLMSConnectionException.REASON_ABORT_INVALID_FRAMECOUNTER);
        }
        return securityProvider;
    }

    @Override
    public byte[] getSystemIdentifier() {
        // Property CallingAPTitle is used as system title in the AARQ
        final HexString callingAPTitle = getProperties().getTypedProperty(A2ConfigurationSupport.CALLING_AP_TITLE_PROPERTY);
        if (callingAPTitle == null || callingAPTitle.getContent() == null || callingAPTitle.getContent().isEmpty()) {
            return super.getSystemIdentifier();
        } else {
            return ProtocolTools.getBytesFromHexString(callingAPTitle.getContent(), "");
        }
    }

    public long getLimitMaxNrOfDays() {
        return getProperties().getTypedProperty(
                A2ConfigurationSupport.LIMIT_MAX_NR_OF_DAYS_PROPERTY,
                BigDecimal.valueOf(0)   // Do not limit, but use as-is
        ).longValue();
    }

    public boolean isTimeIntervalOverClockSync(){
        return getProperties().<Boolean>getTypedProperty(A2ConfigurationSupport.TIME_INTERVAL_OVER_CLOCK_SYNC, true);
    }

    /**
     * The security policy of suite 1 and 2 are not backwards compatible.
     * It is now a byte where every bit is a flag:
     * 0 unused, shall be set to 0,
     * 1 unused, shall be set to 0,
     * 2 authenticated request,
     * 3 encrypted request,
     * 4 digitally signed request,
     * 5 authenticated response,
     * 6 encrypted response,
     * 7 digitally signed response
     */
    @Override
    protected int doGetDataTransportSecurityLevel() {
        if (getSecurityPropertySet() instanceof AdvancedDeviceProtocolSecurityPropertySet) {
            if (getSecuritySuite() <= 0) {
                //Suite 0 uses the old field, EncryptionDeviceAccessLevel. It is either 0, 1, 2 or 3.
                return super.doGetDataTransportSecurityLevel();
            } else {
                AdvancedDeviceProtocolSecurityPropertySet advancedSecurityPropertySet = (AdvancedDeviceProtocolSecurityPropertySet) getSecurityPropertySet();
                int result = 0;
                result |= advancedSecurityPropertySet.getRequestSecurityLevel() << 2;
                result |= advancedSecurityPropertySet.getResponseSecurityLevel() << 5;
                return result;
            }
        } else {
            return super.doGetDataTransportSecurityLevel();
        }
    }

}
