/*
 * Fulcrum.java
 *
 * Created on 8 september 2006, 9:29
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.itron.protocol.SchlumbergerProtocol;
import com.energyict.protocolimpl.itron.quantum.basepages.BasePagesFactory;
import com.energyict.protocolimpl.itron.quantum.basepages.RegisterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Koen
 */
public class Quantum extends SchlumbergerProtocol {

    private BasePagesFactory basePagesFactory = null;
    private RegisterFactory registerFactory = null;
    private QuantumProfile quantumProfile = null;
    private boolean allowClockSet;
    private int loadProfileUnitScale;

    public Quantum(PropertySpecService propertySpecService) {
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
        getBasePagesFactory().downloadOfflineFlag();
    }

    @Override
    protected void doTheDisConnect() throws IOException {
    }

    // The Quantuum meter uses only offset addresses in its protocoldoc. S, we need to set the base memory start address...
    @Override
    protected void doTheConnect() throws IOException {
        getBasePagesFactory().setMemStartAddress(getCommandFactory().getIdentifyCommand().getMemStart());
    }

    @Override
    protected void doTheInit() {
        // specific initialization for the protocol
        setBasePagesFactory(new BasePagesFactory(this));
        setFulcrumProfile(new QuantumProfile(this));
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(this.integerSpec("LoadProfileUnitScale", false));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        allowClockSet = Integer.parseInt(properties.getTypedProperty(ALLOW_CLOCK_SET, "0").trim()) == 1;
        setDelayAfterConnect(Integer.parseInt(properties.getTypedProperty(DELAY_AFTER_CONNECT, "2000").trim()));
        setLoadProfileUnitScale(Integer.parseInt(properties.getTypedProperty("LoadProfileUnitScale", "3").trim()));
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
        return "revision nr "+getBasePagesFactory().getFirmwareRevisionBasePage().getFirmwareRevision();
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
        return ocm.getRegisterInfo() +
                getBasePagesFactory().getMassMemoryBasePages() +
                getBasePagesFactory().getProgramTableBasePage(false) +
                getBasePagesFactory().getProgramTableBasePage(true) +
                getBasePagesFactory().getInstantaneousRegMultipliers() +
                getBasePagesFactory().getMultipliersBasePage() +
                getBasePagesFactory().getPointerTimeDateRegisterReadingBasePage() +
                getBasePagesFactory().getVoltageAndCurrentBasePage() +
                getBasePagesFactory().getGeneralSetUpBasePage() +
                getBasePagesFactory().getMassMemoryBasePages();
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

    private QuantumProfile getFulcrumProfile() {
        return quantumProfile;
    }

    private void setFulcrumProfile(QuantumProfile quantumProfile) {
        this.quantumProfile = quantumProfile;
    }

    int getLoadProfileUnitScale() {
        return loadProfileUnitScale;
    }

    private void setLoadProfileUnitScale(int loadProfileUnitScale) {
        this.loadProfileUnitScale = loadProfileUnitScale;
    }

}