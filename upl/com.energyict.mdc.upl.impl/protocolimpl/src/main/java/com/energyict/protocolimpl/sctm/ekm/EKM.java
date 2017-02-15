/*
 * EKM.java
 *
 * Created on 30 september 2004, 16:04
 */

package com.energyict.protocolimpl.sctm.ekm;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.customerconfig.EDPRegisterConfig;
import com.energyict.protocolimpl.customerconfig.RegisterConfig;
import com.energyict.protocolimpl.metcom.Metcom2;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.sctm.base.GenericRegisters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    private static final String BILLING_TIME_STAMP_ID = "BillingTimeStampID";
    private static final String BILLINGPOINT_TIMESTAMP_ID_DEFAULT = "40*";
    private RegisterConfig regs = new EDPRegisterConfig(); // we should use an infotype property to determine the registerset
    private GenericRegisters genericRegisters;
    private String billingTimeStampId = BILLINGPOINT_TIMESTAMP_ID_DEFAULT;

    public EKM(PropertySpecService propertySpecService) {
        super(propertySpecService);
        genericRegisters = new GenericRegisters(this);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(this.stringSpec(BILLING_TIME_STAMP_ID));
        return propertySpecs;
    }

    private PropertySpec stringSpec(String name) {
        return UPLPropertySpecFactory.specBuilder(name, false, PropertyTranslationKeys.SCTM_BILLING_TIMESTAMP_ID, this.getPropertySpecService()::stringSpec).finish();
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        billingTimeStampId = properties.getTypedProperty(BILLING_TIME_STAMP_ID);
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        if (genericRegisters.isManufacturerSpecific(obisCode)) {
            return genericRegisters.getRegisterInfo(obisCode);
		} else {
            return ObisCodeMapper.getRegisterInfo(obisCode);
		}
    }

    @Override
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

    private String getBillingTimeStampId() {
        if (billingTimeStampId == null) {
            billingTimeStampId = BILLINGPOINT_TIMESTAMP_ID_DEFAULT;
        }
        return billingTimeStampId;
    }

    @Override
    public String getRegistersInfo(int extendedLogging) throws IOException {
        return regs.getRegisterInfo()+"\n"+genericRegisters.getRegisterInfo();
    }

}