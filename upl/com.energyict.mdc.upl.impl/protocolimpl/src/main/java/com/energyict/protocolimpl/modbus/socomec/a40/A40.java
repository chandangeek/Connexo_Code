/*
 * A40.java
 *
 * Created on 19 september 2005, 16:02
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.socomec.a40;

import com.energyict.mdc.upl.SerialNumberSupport;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.base.ProfileLimiter;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.connection.ModbusConnection;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Koen
 */
public class A40 extends Modbus implements SerialNumberSupport {

    private MultiplierFactory multiplierFactory=null;
    private String socomecType;
    private SocomecProfile profile;
    private static final String PR_LIMIT_MAX_NR_OF_DAYS = "LimitMaxNrOfDays";
    private int limitMaxNrOfDays;

    public A40(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    @Override
    protected void doTheConnect() throws IOException {
    }

    @Override
    protected void doTheDisConnect() throws IOException {
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return getProfileWithLimiter(new ProfileLimiter(from, new Date(), getLimitMaxNrOfDays()));
    }

    private ProfileData getProfileWithLimiter(ProfileLimiter limiter) throws IOException {
        Date lastReading = limiter.getFromDate();
        if (getProfile().isSupported()) {
    		ProfileData profileData = new ProfileData();
    		profileData.setChannelInfos(getProfile().getChannelInfos());
    		profileData.setIntervalDatas(getProfile().getIntervalDatas(lastReading));
    		profileData.sort();
    		return profileData;
    	} else {
    		throw new UnsupportedException("ProfileData is not supported by the meter.");
    	}
    }

    @Override
    public int getProfileInterval() throws IOException {
    	return getProfile().getProfileInterval();
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(this.stringSpec("SocomecType", PropertyTranslationKeys.ENERDIS_SOCOMEC_TYPE, false));
        propertySpecs.add(this.integerSpec(PR_LIMIT_MAX_NR_OF_DAYS, PropertyTranslationKeys.ENERDIS_LIMIT_MAX_NR_OF_DAYS, false));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        setInfoTypeInterframeTimeout(properties.getTypedProperty(PK_INTERFRAME_TIMEOUT, 50));
        setSocomecType(properties.getTypedProperty("SocomecType"));
        this.limitMaxNrOfDays = properties.getTypedProperty(PR_LIMIT_MAX_NR_OF_DAYS, 0);
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return "unknown";
    }

    @Override
    public String getSerialNumber() {
        try {
            return getRegisterFactory().findRegister(RegisterFactory.SERIAL_NUMBER).value().toString();
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries() + 1);
        }
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2016-06-03 12:47:33 +0300 (Fri, 03 Jun 2016)$";
    }

    @Override
    public String getProtocolDescription() {
        return "Socomec Diris A40 Modbus";
    }

    @Override
    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }

    @Override
    public Date getTime() throws IOException {
        return new Date();
    }

    @Override
    public BigDecimal getRegisterMultiplier(int address) throws IOException {
        return getMultiplierFactory().getMultiplier(address);
    }

    MultiplierFactory getMultiplierFactory() {
        if (multiplierFactory == null) {
			multiplierFactory = new MultiplierFactory(this);
		}
        return multiplierFactory;
    }

    public String getSocomecType() {
        return socomecType;
    }

    /**
     * Setter for the SocomecType (A20/A40)
     *
     * @param socomecType - the type
     */
    private void setSocomecType(String socomecType) {
        this.socomecType = socomecType;
    }

    /**
     * Setter for the {@link ModbusConnection}
     *
     * @param modbusConnection - the used modbusConnection
     */
    protected void setModbusConnection(ModbusConnection modbusConnection){
    	this.modbusConnection = modbusConnection;
    }

    /**
     * Setter for the {@link Logger}
     *
     * @param logger - the desired logger
     */
    protected void setLogger(Logger logger){
    	setAbstractLogger(logger);
    }

    /**
     * @return the current SocomecProfile
     */
    protected SocomecProfile getProfile(){
    	if(this.profile == null){
    		this.profile = new SocomecProfile(this);
    	}
    	return this.profile;
    }

    /**
     * Read the raw registers from the MobBus device
     *
     * @param address - startAddress
     * @param length - the required data length
     * @return the registers from the device
     * @throws IOException if we couldn't read the data
     */
    int[] readRawValue(int address, int length)  throws IOException {
    	HoldingRegister r = new HoldingRegister(address, length);
        r.setRegisterFactory(getRegisterFactory());
        return r.getReadHoldingRegistersRequest().getRegisters();
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return getProfile().getNumberOfChannels();
    }

    private int getLimitMaxNrOfDays() {
        return limitMaxNrOfDays;
    }
}
