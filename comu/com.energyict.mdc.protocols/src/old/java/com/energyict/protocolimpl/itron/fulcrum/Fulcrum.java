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

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.itron.fulcrum.basepages.BasePagesFactory;
import com.energyict.protocolimpl.itron.fulcrum.basepages.RegisterFactory;
import com.energyict.protocolimpl.itron.protocol.SchlumbergerProtocol;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Koen
 */
public class Fulcrum extends SchlumbergerProtocol {

    @Override
    public String getProtocolDescription() {
        return "Itron/Schlumberger Fulcrum";
    }

    private BasePagesFactory basePagesFactory=null;
    RegisterFactory registerFactory=null;
    private FulcrumProfile fulcrumProfile=null;

    @Inject
    public Fulcrum(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getFulcrumProfile().getProfileData(lastReading,includeEvents);
    }

    protected void doTheConnect() throws IOException {
        // absorb, addresses in the protocoldoc are absolute addresses...
    }

    protected void doTheDisConnect() throws IOException {
        // absorb, addresses in the protocoldoc are absolute addresses...




    }

    protected void doTheInit() {
        // specific initialization for the protocol
        setBasePagesFactory(new BasePagesFactory(this));
        setFulcrumProfile(new FulcrumProfile(this));
    }

    protected void doTheDoValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {

        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getProperty("Timeout","5000").trim()));
    }

    protected List<String> doTheDoGetOptionalKeys() {
        return Collections.emptyList();
    }

    public int getProfileInterval() throws IOException {
        return getBasePagesFactory().getMassMemoryBasePages().getRecordingIntervalLength()*60;
    }

    public int getNumberOfChannels() throws IOException {
        return getBasePagesFactory().getMassMemoryBasePages().getNumberOfChannels();
    }

    public Date getTime() throws IOException {
        return getBasePagesFactory().getRealTimeBasePage().getCalendar().getTime();
    }

    protected void hangup() throws IOException {
        getBasePagesFactory().writeBasePage(0x2111, new byte[]{(byte)0xFF});
    }

    protected void offLine() throws IOException {
        getBasePagesFactory().writeBasePage(0x2112, new byte[]{(byte)0xFF});
    }

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

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public String getFirmwareVersion() throws IOException {
        return getBasePagesFactory().getMeterIdentificationBasePages().toString2();
    }

    public String getSerialNumber() throws IOException {
        return "getSerialNumber() not implemented yet";
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

    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuilder strBuff = new StringBuilder();
        strBuff.append(getBasePagesFactory().getMeterIdentificationBasePages());
        ObisCodeMapper ocm = new ObisCodeMapper(this);
        strBuff.append(ocm.getRegisterInfo());
        return strBuff.toString();
    }

    // RegisterProtocol Interface implementation
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(this);
        return ocm.getRegisterValue(obisCode);
    }

    public FulcrumProfile getFulcrumProfile() {
        return fulcrumProfile;
    }

    public void setFulcrumProfile(FulcrumProfile fulcrumProfile) {
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