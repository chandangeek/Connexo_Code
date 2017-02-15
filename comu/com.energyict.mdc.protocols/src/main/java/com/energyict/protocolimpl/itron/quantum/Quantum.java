/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.protocolimpl.itron.protocol.SchlumbergerProtocol;
import com.energyict.protocolimpl.itron.quantum.basepages.BasePagesFactory;
import com.energyict.protocolimpl.itron.quantum.basepages.RegisterFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Koen
 */
public class Quantum extends SchlumbergerProtocol {

    @Override
    public String getProtocolDescription() {
        return "Itron/Schlumberger Quantum";
    }

    private BasePagesFactory basePagesFactory=null;
    RegisterFactory registerFactory=null;
    private QuantumProfile quantumProfile=null;
    boolean allowClockSet;
    private int loadProfileUnitScale;

    @Inject
    public Quantum(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getFulcrumProfile().getProfileData(lastReading,includeEvents);
    }

    protected void hangup() throws IOException {
        //getBasePagesFactory().writeBasePage(0x2111, new byte[]{(byte)0xFF});
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
        setFulcrumProfile(new QuantumProfile(this));
    }

    protected void doTheDoValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        allowClockSet = Integer.parseInt(properties.getProperty("AllowClockSet","0").trim()) == 1;
        setDelayAfterConnect(Integer.parseInt(properties.getProperty("DelayAfterConnect","2000").trim()));
        setLoadProfileUnitScale(Integer.parseInt(properties.getProperty("LoadProfileUnitScale","3").trim()));
    }

    protected List<String> doTheDoGetOptionalKeys() {
        return Arrays.asList("AllowClockSet", "LoadProfileUnitScale");
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

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public String getFirmwareVersion() throws IOException {
        return "revision nr "+getBasePagesFactory().getFirmwareRevisionBasePage().getFirmwareRevision();
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
        ObisCodeMapper ocm = new ObisCodeMapper(this);

        // registers
        strBuff.append(ocm.getRegisterInfo());

        // tables
        strBuff.append(getBasePagesFactory().getMassMemoryBasePages());
        strBuff.append(getBasePagesFactory().getProgramTableBasePage(false));
        strBuff.append(getBasePagesFactory().getProgramTableBasePage(true));
        strBuff.append(getBasePagesFactory().getInstantaneousRegMultipliers());

        strBuff.append(getBasePagesFactory().getMultipliersBasePage());

        strBuff.append(getBasePagesFactory().getPointerTimeDateRegisterReadingBasePage());
        strBuff.append(getBasePagesFactory().getVoltageAndCurrentBasePage());

        strBuff.append(getBasePagesFactory().getGeneralSetUpBasePage());
        strBuff.append(getBasePagesFactory().getMassMemoryBasePages());

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

    public QuantumProfile getFulcrumProfile() {
        return quantumProfile;
    }

    public void setFulcrumProfile(QuantumProfile quantumProfile) {
        this.quantumProfile = quantumProfile;
    }

    public int getLoadProfileUnitScale() {
        return loadProfileUnitScale;
    }

    public void setLoadProfileUnitScale(int loadProfileUnitScale) {
        this.loadProfileUnitScale = loadProfileUnitScale;
    }

} // public class Fulcrum extends SchlumbergerProtocol
