/*
 * Fulcrum.java
 *
 * Created on 8 september 2006, 9:29
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.datastar;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.itron.datastar.basepages.BasePagesFactory;
import com.energyict.protocolimpl.itron.protocol.SchlumbergerProtocol;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * @version  2.0
 * @author   Koenraad Vanderschaeve
 * <P>
 * <B>Description :</B><BR>
 * Class that implements Itron Datastar logger.
 * <BR>
 * <B>@beginchanges</B><BR>
KV|04072007|Add additional multipliers
 * @endchanges
 */

public class Datastar extends SchlumbergerProtocol {

    @Override
    public String getProtocolDescription() {
        return "Itron/Schlumberger Datastar";
    }

    private BasePagesFactory basePagesFactory=null;

    private DatastarProfile datastarProfile=null;
    boolean allowClockSet;

    @Inject
    public Datastar(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getFulcrumProfile().getProfileData(lastReading,includeEvents);
    }

    protected void hangup() throws IOException {
        getBasePagesFactory().writeBasePage(0x0052, new byte[]{(byte)0xFF});
    }

    protected void doTheDisConnect() throws IOException {

    }

    // The Quantuum meter uses only offset addresses in its protocoldoc. S, we need to set the base memory start address...
    protected void doTheConnect() throws IOException {
        getBasePagesFactory().setMemStartAddress(getCommandFactory().getIdentifyCommand().getMemStart());
    }

    protected void doTheInit() {
        // specific initialization for the protocol
        setBasePagesFactory(new BasePagesFactory(this));
        setFulcrumProfile(new DatastarProfile(this));
    }

    protected void doTheDoValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        allowClockSet = Integer.parseInt(properties.getProperty("AllowClockSet","0").trim()) == 1;
        setDelayAfterConnect(Integer.parseInt(properties.getProperty("DelayAfterConnect","2000").trim()));
    }

    protected List<String> doTheDoGetOptionalKeys() {
        return Collections.singletonList("AllowClockSet");
    }

    public int getProfileInterval() throws IOException {
        return getBasePagesFactory().getOperatingSetUpBasePage().getProfileInterval()*60;
    }

    public int getNumberOfChannels() throws IOException {
        return getBasePagesFactory().getOperatingSetUpBasePage().getNrOfChannels();
    }

    public Date getTime() throws IOException {
        return getBasePagesFactory().getRealTimeBasePage().getCalendar().getTime();
    }

    public void setTime() throws IOException {
        if (allowClockSet) {
            //getBasePagesFactory().writeBasePage(0x0063, new byte[]{(byte)0xFF}); // WARMST
            getBasePagesFactory().setRealTimeBasePage();
            //getBasePagesFactory().writeBasePage(0x0061, new byte[]{(byte)0xFF}); // RUN
        }
        else {
            throw new UnsupportedException("setTime() is supported on the Datastar meter but you have to make sure that the firmware version you have allows a clocksync without clearing the load profile. Therefor, the 'AllowClockSet' (set to 1 to enable) property adds an extra level of security to the timeset functionality.");
        }
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public String getFirmwareVersion() throws IOException {
        return "firmware revision="+getBasePagesFactory().getFirmwareAndSoftwareRevision().getFwVersion();
               //", software revision="+getBasePagesFactory().getFirmwareAndSoftwareRevision().getSwVersion()+
               //", options=0x"+Integer.toHexString(getBasePagesFactory().getFirmwareOptionsBasePage().getOptions())+
               //", front end firmware revision="+getBasePagesFactory().getFrontEndFirmwareVersionBasePage().getVersion();
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



    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuilder strBuff = new StringBuilder();
        ObisCodeMapper ocm = new ObisCodeMapper(this);

        // tables
        strBuff.append(getBasePagesFactory().getMassMemoryBasePages());
        strBuff.append(getBasePagesFactory().getCurrentMassMemoryRecordBasePage());
        strBuff.append(getBasePagesFactory().getKYZDividersBasePage());
        //strBuff.append(getBasePagesFactory().getSelfreadIndexBasePage());
        //strBuff.append(getBasePagesFactory().getModelTypeBasePage());
        //strBuff.append(getBasePagesFactory().getMeterKhBasePage());
        //strBuff.append(getBasePagesFactory().getRegisterConfigurationBasePage());
        //strBuff.append(getBasePagesFactory().getRegisterMultiplierBasePage());
        strBuff.append(getBasePagesFactory().getOperatingSetUpBasePage());
        strBuff.append(getBasePagesFactory().getDataBuffersBasePage());
        strBuff.append(getBasePagesFactory().getPulseMultiplierAndDisplayUnits());
        // registers
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

    public DatastarProfile getFulcrumProfile() {
        return datastarProfile;
    }

    public void setFulcrumProfile(DatastarProfile datastarProfile) {
        this.datastarProfile = datastarProfile;
    }

} // public class Fulcrum extends SchlumbergerProtocol
