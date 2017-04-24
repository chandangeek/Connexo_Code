/*
 * EKM.java
 *
 * Created on 30 september 2004, 16:04
 */

package com.energyict.protocolimpl.sctm.ekm;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterProtocol;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.customerconfig.EDPRegisterConfig;
import com.energyict.protocolimpl.customerconfig.RegisterConfig;
import com.energyict.protocolimpl.metcom.Metcom2;
import com.energyict.protocolimpl.sctm.base.GenericRegisters;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author  Koen
 * @beginchanges
KV|30092004|Initial version
KV|15022005|Changed RegisterConfig to allow B field obiscodes != 1
KV|15022005|bugfix  RegisterConfig
KV|07032005|changes for setTime and use of 8 character SCTM ID
KV|17032005|Minor bugfixes and improved registerreading
KV|23032005|Changed header to be compatible with protocol version tool
KV|23092005|Changed intervalstate bits behaviour (EDP)
KV|06042006|Add IntervalStatusBehaviour custom property to correct power fail status
 * @endchanges
 */
//com.energyict.protocolimpl.sctm.enermete70x.EKM
public class EKM extends Metcom2 implements RegisterProtocol {

    @Override
    public String getProtocolDescription() {
        return "L&G EKM SCTM";
    }

    private static final String BILLING_TIME_STAMP_ID = "BillingTimeStampID";
    private static final String BILLINGPOINT_TIMESTAMP_ID_DEFAULT = "40*";
    private RegisterConfig regs = new EDPRegisterConfig(); // we should use an infotype property to determine the registerset
    private GenericRegisters genericRegisters;
    private String billingTimeStampId = BILLINGPOINT_TIMESTAMP_ID_DEFAULT;

    @Inject
    public EKM(PropertySpecService propertySpecService) {
        super(propertySpecService);
        genericRegisters = new GenericRegisters(this);
    }

    @Override
    protected void validateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        billingTimeStampId = properties.getProperty(BILLING_TIME_STAMP_ID);
        super.validateProperties(properties);
    }

    public List<String> getOptionalKeys() {
        return Arrays.asList(
                "Timeout",
                "Retries",
                "HalfDuplex",
                "ExtendedLogging",
                "RemovePowerOutageIntervals",
                "LogBookReadCommand",
                "ForcedDelay",
                "IntervalStatusBehaviour",
                "AutoBillingPointNrOfDigits",
                "TimeSetMethod",
                "Software7E1",
                BILLING_TIME_STAMP_ID);
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    /*******************************************************************************************
    R e g i s t e r P r o t o c o l  i n t e r f a c e
    *******************************************************************************************/
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        if (genericRegisters.isManufacturerSpecific(obisCode)) {
            return genericRegisters.getRegisterInfo(obisCode);
		} else {
            return ObisCodeMapper.getRegisterInfo(obisCode);
		}
    }
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        if (genericRegisters.isManufacturerSpecific(obisCode)) {
            return genericRegisters.readRegisterValue(obisCode);
        }
        else {
            ObisCodeMapper ocm = new ObisCodeMapper(getDumpData(obisCode.getB()-1),getTimeZone(),regs,getAutoBillingPointNrOfDigits());
            ocm.setBillingTimeStampId(getBillingTimeStampId());
            return ocm.getRegisterValue(obisCode);
        }
    }

    public String getBillingTimeStampId() {
        if (billingTimeStampId == null) {
            billingTimeStampId = BILLINGPOINT_TIMESTAMP_ID_DEFAULT;
        }
        return billingTimeStampId;
    }

    public String getRegistersInfo(int extendedLogging) throws IOException {
        return regs.getRegisterInfo()+"\n"+genericRegisters.getRegisterInfo();
    }

}
