/*
 * Fulcrum.java
 *
 * Created on 8 september 2006, 9:29
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

/*
 * leap day issue 1/4/2008
 */

package com.energyict.protocolimpl.itron.fulcrum;

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.itron.fulcrum.basepages.BasePagesFactory;
import com.energyict.protocolimpl.itron.fulcrum.basepages.RegisterFactory;
import com.energyict.protocolimpl.itron.protocol.SchlumbergerProtocol;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class Fulcrum extends SchlumbergerProtocol {

    private BasePagesFactory basePagesFactory = null;
    private RegisterFactory registerFactory = null;
    private FulcrumProfile fulcrumProfile = null;

    public Fulcrum(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getFulcrumProfile().getProfileData(lastReading,includeEvents);
    }

    @Override
    protected void doTheConnect() throws IOException {
        // absorb, addresses in the protocoldoc are absolute addresses...
    }

    @Override
    protected void doTheDisConnect() throws IOException {
        // absorb, addresses in the protocoldoc are absolute addresses...
    }

    @Override
    protected void doTheInit() {
        // specific initialization for the protocol
        setBasePagesFactory(new BasePagesFactory(this));
        setFulcrumProfile(new FulcrumProfile(this));
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getTypedProperty(PROP_TIMEOUT, "5000").trim()));
    }

    @Override
    public int getProfileInterval() throws IOException {
        return getBasePagesFactory().getMassMemoryBasePages().getRecordingIntervalLength()*60;
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return getBasePagesFactory().getMassMemoryBasePages().getNumberOfChannels();
    }

    @Override
    public Date getTime() throws IOException {
        return getBasePagesFactory().getRealTimeBasePage().getCalendar().getTime();
    }

    @Override
    protected void hangup() throws IOException {
        getBasePagesFactory().writeBasePage(0x2111, new byte[]{(byte)0xFF});
    }

    @Override
    protected void offLine() throws IOException {
        getBasePagesFactory().writeBasePage(0x2112, new byte[]{(byte)0xFF});
    }

    @Override
    public void setTime() throws IOException {
        if (isAllowClockSet()) {
            getBasePagesFactory().writeBasePage(0x2113, new byte[]{(byte)0xFF}); // STOP METERING FLAG
            getBasePagesFactory().writeBasePage(0x2118, new byte[]{0}); // CLOCK OPTION RUN FLAG
            getBasePagesFactory().setRealTimeBasePage();
            getBasePagesFactory().writeBasePage(0x2116, new byte[]{(byte)0xFF}); // CLOCK OPTION RECONFIGURE FLAG
            getBasePagesFactory().writeBasePage(0x2113, new byte[]{0}); // STOP METERING FLAG
        }
        else {
            throw new UnsupportedException("setTime() is not supported on the Fulcrum meter because is clears all the memory. However, when 'AllowClockSet' property is set to 1, a setTime() can be forced but all memory will be cleared!");
        }
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:26:46 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return getBasePagesFactory().getMeterIdentificationBasePages().toString2();
    }

    public BasePagesFactory getBasePagesFactory() {
        return basePagesFactory;
    }

    public void setBasePagesFactory(BasePagesFactory basePagesFactory) {
        this.basePagesFactory = basePagesFactory;
    }

    public RegisterFactory getRegisterFactory() throws IOException {
        if (registerFactory == null) {
            registerFactory = new RegisterFactory(this);
            registerFactory.init();
        }
        return registerFactory;
    }

    @Override
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(this);
        return String.valueOf(getBasePagesFactory().getMeterIdentificationBasePages()) + ocm.getRegisterInfo();
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(this);
        return ocm.getRegisterValue(obisCode);
    }

    private FulcrumProfile getFulcrumProfile() {
        return fulcrumProfile;
    }

    private void setFulcrumProfile(FulcrumProfile fulcrumProfile) {
        this.fulcrumProfile = fulcrumProfile;
    }

    protected void validateDeviceId() throws IOException {
        if ((getInfoTypeDeviceID() == null) || ("".compareTo(getInfoTypeDeviceID().trim())==0)) {
            return;
        }
        if (getBasePagesFactory().getMeterIdentificationBasePages().getUnitId().trim().compareTo(getInfoTypeDeviceID().trim()) != 0) {
            String msg =
                    "DeviceId mismatch! meter DeviceId=" + getBasePagesFactory().getMeterIdentificationBasePages().getUnitId().trim() +
                    ", configured DeviceId=" + getInfoTypeDeviceID().trim();
            throw new IOException(msg);
        }

    }

}