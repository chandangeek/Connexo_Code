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

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class Vectron extends SchlumbergerProtocol {

    private BasePagesFactory basePagesFactory = null;
    private RegisterFactory registerFactory = null;
    private VectronProfile vectronProfile = null;
    private boolean allowClockSet;

    public Vectron(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getFulcrumProfile().getProfileData(lastReading,includeEvents);
    }

    @Override
    protected void hangup() throws IOException {
    }

    @Override
    protected void offLine() throws IOException {
    }

    @Override
    protected void doTheDisConnect() throws IOException {
    }

    // The Quantuum meter uses only offset addresses in its protocoldoc. S, we need to set the base memory start address...
    @Override
    protected void doTheConnect() throws IOException {
        //getBasePagesFactory().setMemStartAddress(getCommandFactory().getIdentifyCommand().getMemStart());
    }

    @Override
    protected void doTheInit() {
        // specific initialization for the protocol
        setBasePagesFactory(new BasePagesFactory(this));
        setFulcrumProfile(new VectronProfile(this));
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        allowClockSet = Integer.parseInt(properties.getTypedProperty(ALLOW_CLOCK_SET, "0").trim()) == 1;
        setDelayAfterConnect(Integer.parseInt(properties.getTypedProperty(DELAY_AFTER_CONNECT, "2000").trim()));
    }

    @Override
    public int getProfileInterval() throws IOException {
        return getBasePagesFactory().getMassMemoryBasePages().getProfileInterval()*60;
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return getBasePagesFactory().getMassMemoryBasePages().getNrOfChannels();
    }

    @Override
    public Date getTime() throws IOException {
        return getBasePagesFactory().getRealTimeBasePage().getCalendar().getTime();
    }

    @Override
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
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:23:41 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return "firmware revision="+getBasePagesFactory().getFirmwareAndSoftwareRevision().getFwVersion()+
               ", software revision="+getBasePagesFactory().getFirmwareAndSoftwareRevision().getSwVersion()+
               ", options=0x"+Integer.toHexString(getBasePagesFactory().getFirmwareOptionsBasePage().getOptions())+
               ", front end firmware revision="+getBasePagesFactory().getFrontEndFirmwareVersionBasePage().getVersion();
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
        return String.valueOf(getBasePagesFactory().getMassMemoryBasePages()) +
                getBasePagesFactory().getFrontEndFirmwareVersionBasePage() +
                getBasePagesFactory().getSelfreadIndexBasePage() +
                getBasePagesFactory().getFirmwareOptionsBasePage() +
                getBasePagesFactory().getModelTypeBasePage() +
                getBasePagesFactory().getMeterKhBasePage() +
                getBasePagesFactory().getRegisterConfigurationBasePage() +
                getBasePagesFactory().getRegisterMultiplierBasePage() +
                getBasePagesFactory().getOperatingSetUpBasePage() +
                ocm.getRegisterInfo();
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

    private VectronProfile getFulcrumProfile() {
        return vectronProfile;
    }

    private void setFulcrumProfile(VectronProfile vectronProfile) {
        this.vectronProfile = vectronProfile;
    }

}