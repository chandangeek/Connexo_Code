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

import com.energyict.mdc.upl.UnsupportedException;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.itron.datastar.basepages.BasePagesFactory;
import com.energyict.protocolimpl.itron.protocol.SchlumbergerProtocol;

import java.io.IOException;
import java.util.ArrayList;
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

    private BasePagesFactory basePagesFactory=null;

    private DatastarProfile datastarProfile=null;
    boolean allowClockSet;

    public Datastar() {
    }


    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getFulcrumProfile().getProfileData(lastReading,includeEvents);
    }

    protected void hangup() throws IOException {
        getBasePagesFactory().writeBasePage(0x0052, new byte[]{(byte)0xFF});
    }

    protected void offLine() throws IOException {
        getBasePagesFactory().writeBasePage(0x0053, new byte[]{(byte)0xFF});
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

    protected List doTheDoGetOptionalKeys() {
        List list = new ArrayList();
        list.add("AllowClockSet");
        return list;
    }

    public int getProfileInterval() throws UnsupportedException, IOException {
        return getBasePagesFactory().getOperatingSetUpBasePage().getProfileInterval()*60;
    }

    public int getNumberOfChannels() throws UnsupportedException, IOException {
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
        else throw new UnsupportedException("setTime() is supported on the Datastar meter but you have to make sure that the firmware version you have allows a clocksync without clearing the load profile. Therefor, the 'AllowClockSet' (set to 1 to enable) property adds an extra level of security to the timeset functionality.");
    }

    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:24:28 +0200 (Thu, 26 Nov 2015)$";
    }

    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return "firmware revision="+getBasePagesFactory().getFirmwareAndSoftwareRevision().getFwVersion();
               //", software revision="+getBasePagesFactory().getFirmwareAndSoftwareRevision().getSwVersion()+
               //", options=0x"+Integer.toHexString(getBasePagesFactory().getFirmwareOptionsBasePage().getOptions())+
               //", front end firmware revision="+getBasePagesFactory().getFrontEndFirmwareVersionBasePage().getVersion();
    }

    public BasePagesFactory getBasePagesFactory() {
        return basePagesFactory;
    }

    public void setBasePagesFactory(BasePagesFactory basePagesFactory) {
        this.basePagesFactory = basePagesFactory;
    }

    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuffer strBuff = new StringBuffer();
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
