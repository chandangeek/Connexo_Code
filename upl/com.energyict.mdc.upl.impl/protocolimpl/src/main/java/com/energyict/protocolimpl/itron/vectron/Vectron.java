/*
 * Fulcrum.java
 *
 * Created on 8 september 2006, 9:29
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.vectron;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.itron.protocol.SchlumbergerProtocol;
import com.energyict.protocolimpl.itron.vectron.basepages.BasePagesFactory;
import com.energyict.protocolimpl.itron.vectron.basepages.RegisterFactory;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Koen
 */
public class Vectron extends SchlumbergerProtocol {

    public static final String WAIT_UNTIL_TIME_VALID = "waitUntilTimeValid";
    public static final String WAITING_TIME = "waitingTime";
    private BasePagesFactory basePagesFactory = null;
    RegisterFactory registerFactory = null;
    private VectronProfile vectronProfile = null;
    boolean allowClockSet;
    boolean waitUntilTimeValid;
    private int waitingTime = 5;

    public Vectron(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }


    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getFulcrumProfile().getProfileData(lastReading, includeEvents);
    }

    protected void hangup() throws IOException {
        //getBasePagesFactory().writeBasePage(0x2111, new byte[]{(byte)0xFF});
    }

    protected void offLine() throws IOException {
        //getBasePagesFactory().writeBasePage(0x2112, new byte[]{(byte)0xFF});
    }

    protected void doTheDisConnect() throws IOException {

    }

    // The Quantuum meter uses only offset addresses in its protocoldoc. S, we need to set the base memory start address...
    protected void doTheConnect() throws IOException {
        //getBasePagesFactory().setMemStartAddress(getCommandFactory().getIdentifyCommand().getMemStart());
    }

    protected void doTheInit() {
        // specific initialization for the protocol
        setBasePagesFactory(new BasePagesFactory(this));
        setFulcrumProfile(new VectronProfile(this));
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        ArrayList<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(this.stringSpec(WAIT_UNTIL_TIME_VALID, PropertyTranslationKeys.WAIT_UNTIL_TIME_VALID, false));
        propertySpecs.add(this.stringSpec(WAITING_TIME, PropertyTranslationKeys.WAITING_TIME, false));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        allowClockSet = Integer.parseInt(properties.getTypedProperty(ALLOW_CLOCK_SET, "0").trim()) == 1;
        setDelayAfterConnect(Integer.parseInt(properties.getTypedProperty(DELAY_AFTER_CONNECT, "2000").trim()));
        waitUntilTimeValid = Integer.parseInt(properties.getTypedProperty(WAIT_UNTIL_TIME_VALID, "1")) == 1;
        waitingTime = Integer.parseInt(properties.getTypedProperty(WAITING_TIME, "5").trim());
    }

    public int getProfileInterval() throws IOException {
        return getBasePagesFactory().getMassMemoryBasePages().getProfileInterval() * 60;
    }

    public int getNumberOfChannels() throws IOException {
        return getBasePagesFactory().getMassMemoryBasePages().getNrOfChannels();
    }

    public Date getTime() throws IOException {
        return getBasePagesFactory().getRealTimeBasePage().getCalendar().getTime();
    }

    public void setTime() throws IOException {
//        if (allowClockSet) {
//            getBasePagesFactory().writeBasePage(0x2113, new byte[]{(byte)0xFF});
//            getBasePagesFactory().writeBasePage(0x2118, new byte[]{0});
//            getBasePagesFactory().setRealTimeBasePage();
//            getBasePagesFactory().writeBasePage(0x2116, new byte[]{(byte)0xFF});
//            getBasePagesFactory().writeBasePage(0x2113, new byte[]{0});
//        }
//        else throw new UnsupportedException("setTime() is not supported on the Fulcrum meter because is clears all the memory. However, when 'AllowClockSet' property is set to 1, a setTime() can be forced but all memory will be cleared!");
    }

    @Override
    public String getProtocolDescription() {
        return "Itron/Schlumberger Vectron";
    }

    public String getProtocolVersion() {
        return "$Date: 2017-02-02 16:23:41 +0200 (Th, 2 Feb 2017)$";
    }

    public String getFirmwareVersion() throws IOException {
        return "firmware revision=" + getBasePagesFactory().getFirmwareAndSoftwareRevision().getFwVersion() +
                ", software revision=" + getBasePagesFactory().getFirmwareAndSoftwareRevision().getSwVersion() +
                ", options=0x" + Integer.toHexString(getBasePagesFactory().getFirmwareOptionsBasePage().getOptions()) +
                ", front end firmware revision=" + getBasePagesFactory().getFrontEndFirmwareVersionBasePage().getVersion();
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
        StringBuffer strBuff = new StringBuffer();
        ObisCodeMapper ocm = new ObisCodeMapper(this);

        // tables
        strBuff.append(getBasePagesFactory().getMassMemoryBasePages());
        strBuff.append(getBasePagesFactory().getFrontEndFirmwareVersionBasePage());
        strBuff.append(getBasePagesFactory().getSelfreadIndexBasePage());
        strBuff.append(getBasePagesFactory().getFirmwareOptionsBasePage());
        strBuff.append(getBasePagesFactory().getModelTypeBasePage());
        strBuff.append(getBasePagesFactory().getMeterKhBasePage());
        strBuff.append(getBasePagesFactory().getRegisterConfigurationBasePage());
        strBuff.append(getBasePagesFactory().getRegisterMultiplierBasePage());
        strBuff.append(getBasePagesFactory().getOperatingSetUpBasePage());
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

    public VectronProfile getFulcrumProfile() {
        return vectronProfile;
    }

    public void setFulcrumProfile(VectronProfile vectronProfile) {
        this.vectronProfile = vectronProfile;
    }

    public boolean waitUntilTimeValid() {
        return waitUntilTimeValid;
    }

    public long getWaitingTime() {
        return waitingTime;
    }
} // public class Fulcrum extends SchlumbergerProtocol
