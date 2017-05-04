/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.protocols.mdc.inbound.rtuplusserver.DiscoverResult;
import com.energyict.protocols.mdc.inbound.rtuplusserver.DiscoverTools;

import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.connection.ModbusConnection;

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
/**
 *
 * @author Koen
 */
public class A40 extends Modbus {

    @Override
    public String getProtocolDescription() {
        return "Socomec Diris A40 Modbus";
    }

    private MultiplierFactory multiplierFactory=null;
    private String socomecType;
    private SocomecProfile profile;

    @Inject
    public A40(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    protected void doTheConnect() throws IOException {
    }

    protected void doTheDisConnect() throws IOException {
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {

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

    public int getProfileInterval() throws IOException {
    	return getProfile().getProfileInterval();
    }

    protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setInfoTypeInterframeTimeout(Integer.parseInt(properties.getProperty("InterframeTimeout","50").trim()));
        setSocomecType(properties.getProperty("SocomecType"));
    }

    public String getFirmwareVersion() throws IOException {
        return "unknown";
    }

    protected List<String> doTheGetOptionalKeys() {
        return Collections.singletonList("SocomecType");
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }

    public Date getTime() throws IOException {
    	getRegisterFactory().findRegister(RegisterFactory.currentDateTime).getReadHoldingRegistersRequest().getRegisters();
        return new Date();
    }

    public DiscoverResult discover(DiscoverTools discoverTools) {
        // discovery is implemented in the GenericModbusDiscover protocol
        return null;
    }

    /**
     * @param address - the given address
     * @return the mulitplier for the given address
     */
    public BigDecimal getRegisterMultiplier(int address) throws IOException {
        return getMultiplierFactory().getMultiplier(address);
    }

    /**
     * Getter for the {@link MultiplierFactory}
     *
     * @return the MulitpliereFactory
     */
    public MultiplierFactory getMultiplierFactory() {
        if (multiplierFactory == null) {
			multiplierFactory = new MultiplierFactory(this);
		}
        return multiplierFactory;
    }

    /**
     * Getter for the SocomecType (A20/A40)
     *
     * @return
     */
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

    /**
     * @return the number of channels
     */
    public int getNumberOfChannels() throws IOException {
        return getProfile().getNumberOfChannels();
    }
}
