/*
 * SchlumbergerProtocol.java
 *
 * Created on 8 september 2006, 10:11
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.protocol;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.itron.protocol.schlumberger.CommandFactory;
import com.energyict.protocolimpl.itron.protocol.schlumberger.IdentifyCommand;
import com.energyict.protocolimpl.itron.protocol.schlumberger.SchlumbergerConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static com.energyict.mdc.upl.MeterProtocol.Property.SECURITYLEVEL;
import static com.energyict.mdc.upl.MeterProtocol.Property.TIMEOUT;

/**
 *
 * @author Koen
 */
public abstract class SchlumbergerProtocol extends AbstractProtocol implements ProtocolLink {

    private static final String UNIT_ID = "UnitId";
    private static final String UNIT_TYPE = "UnitType";
    protected static final String DELAY_AFTER_CONNECT = "DelayAfterConnect";
    private static final String BLOCK_SIZE = "BlockSize";
    private static final String DAISY_CHAIN = "DaisyChain";
    protected static final String ALLOW_CLOCK_SET = "AllowClockSet";
    private static final String FORCED_DELAY = PROP_FORCED_DELAY;
    private static final String SECURITY_LEVEL = SECURITYLEVEL.getName();
    private static final String NODE_ADDRESS = "NodeAddress";
    private static final String UNIT_ID_MASTER = "UnitIdMaster";
    private static final String SECURITY_LEVEL_MASTER = "SecurityLevelMaster";
    private static final String PASSWORD_MASTER = "PasswordMaster";

    public SchlumbergerProtocol(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    protected abstract void doTheInit();
    protected abstract void doTheConnect() throws IOException;
    protected abstract void doTheDisConnect() throws IOException;
    protected abstract void hangup() throws IOException;
    protected abstract void offLine() throws IOException;

    private SchlumbergerConnection schlumbergerConnection=null;
    private CommandFactory commandFactory=null;
    private int delayAfterConnect;
    private String unitType;
    private String unitId;
    private int blockSize;
    private boolean allowClockSet;
    private int daisyChain;
    private String unitIdMaster;
    private int securityLevelMaster;
    private String passwordMaster;

    @Override
    protected void doConnect() throws IOException {
        getSchlumbergerConnection().delayAndFlush(getDelayAfterConnect());
        if (getDaisyChain() == 2) {
            doDaisyChainConnect();
        } else {
            // is it allowed to send an extra I and S? why shouldn't it?
            //if ((!isDaisyChain()) || (isDaisyChain() && ((getInfoTypeNodeAddress()==null) || (getInfoTypeNodeAddress().compareTo("")==0)))) {
            getCommandFactory().enqCommand();
            getCommandFactory().getIdentifyCommand(getUnitId(), getUnitType());
            if (getInfoTypeSecurityLevel() >= 1) {
                getCommandFactory().securityCommand(getInfoTypePassword());
            }
            //}

            if (isDaisyChain()) {
                if ((getInfoTypeNodeAddress() != null) && (getInfoTypeNodeAddress().compareTo("") != 0)) {
                    hangup();
                    getSchlumbergerConnection().sendEnqMultidrop(20, Integer.parseInt(getInfoTypeNodeAddress()));
                    getCommandFactory().getIdentifyCommand(getUnitId(), getUnitType());
                    if (getInfoTypeSecurityLevel() >= 1) {
                        getCommandFactory().securityCommand(getInfoTypePassword());
                    }
                }
            }
        }

        doTheConnect();
    }

    /**
     * Do a daisy chained connect<br></br>
     * <p>In a daisy chained environment only one remote unit is programmed to answer the phone line (= the master).
     * When the slave should be reached, we should download the offline flag (while we are connected & authenticated to the master);
     * The master unit will send a hardwired signal to the second unit an then hang up the pone. The second unit (=slave) shall pick up the phone
     * before the first unit hangs up, after which we can log on and authenticate to the second unit.</p>
     * @throws IOException
     */
    private void doDaisyChainConnect() throws IOException {
        // 1. Load the custom properties of the master
        String unitIDOfMaster = getUnitIdMaster();
        int securityLevelOfMaster = getSecurityLevelMaster();
        String passwordOfMaster = getPasswordMaster();

        // 1. Log on to the master unit
        getLogger().log(Level.FINE, "Daisy chaining - Log on to the master");
        getCommandFactory().enqCommand();
        IdentifyCommand masterIdentify = getCommandFactory().getIdentifyCommand();// We do not specify the UnitID and Type, but use the wildcard
        if (!masterIdentify.getUnitId().equals(unitIDOfMaster)) {
            throw new IOException("Could not log on to the master - expected unitID "+ unitIDOfMaster +", but received " + masterIdentify.getUnitId()+".");
        }

        // 2. Authenticate the master
        getLogger().log(Level.FINE, "Daisy chaining - Authenticating the master");
        if (securityLevelOfMaster >= 1) {
            getCommandFactory().securityCommand(passwordOfMaster);
        }

        // 3. Instruct a handover to the slave device, done by downloading 0xFF to the offline flag
        getLogger().log(Level.FINE, "Daisy chaining - Instruct handover to the slave");
        this.offLine();

        // 4. Clear all data until the slave is connected
        getLogger().log(Level.FINE, "Daisy chaining - Handover to the slave in progress");
        getSchlumbergerConnection().sendMultipleEnqs(1, 0);    // First flush the incoming data for 1 sec and then launch an ENQ command

        // 5. Log on to the slave unit
        getLogger().log(Level.FINE, "Daisy chaining - Log on to the slave");
        getCommandFactory().enqCommand();
        IdentifyCommand slaveIdentify = getCommandFactory().getIdentifyCommand();// We do not specify the UnitID and Type, but use the wildcard
        if (!slaveIdentify.getUnitId().equals(getUnitId())) {
            throw new IOException("Could not log on to the slave - expected unitID " + getUnitId() + ", but received " + slaveIdentify.getUnitId() + ".");
        }

        // 6. Authenticate the slave
        getLogger().log(Level.INFO, "Daisy chaining - Authenticating the slave");
        if (getSecurityLevel() >= 1) {
            getCommandFactory().securityCommand(getStrPassword());
        }
    }

    @Override
    protected void doDisconnect() throws IOException {
        if (isDaisyChain() && getDaisyChain() != 2) {
            getSchlumbergerConnection().sendEnqMultidrop(2);
        } else {
            hangup();
        }
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.add(this.stringSpec(UNIT_TYPE, false));
        propertySpecs.add(this.stringSpec(UNIT_ID, false));
        propertySpecs.add(this.stringSpec(NODE_ADDRESS, false));
        propertySpecs.add(this.integerSpec(DELAY_AFTER_CONNECT, false));
        propertySpecs.add(this.integerSpec(BLOCK_SIZE, false));
        propertySpecs.add(this.integerSpec(DAISY_CHAIN, false));
        propertySpecs.add(this.integerSpec(ALLOW_CLOCK_SET, false));
        propertySpecs.add(this.stringSpec(UNIT_ID_MASTER, false));
        propertySpecs.add(this.integerSpec(SECURITY_LEVEL_MASTER, false));
        propertySpecs.add(this.stringSpec(PASSWORD_MASTER, false));
        return propertySpecs;
    }

    @Override
    public void setProperties(TypedProperties properties) throws PropertyValidationException {
        super.setProperties(properties);
        setForcedDelay(Integer.parseInt(properties.getTypedProperty(FORCED_DELAY,"0").trim()));
        setInfoTypeSecurityLevel(Integer.parseInt(properties.getTypedProperty(SECURITY_LEVEL, "1").trim()));
        setUnitType(properties.getTypedProperty(UNIT_TYPE));
        setUnitId(properties.getTypedProperty(UNIT_ID));
        setInfoTypeNodeAddress(properties.getTypedProperty(NODE_ADDRESS));
        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getTypedProperty(TIMEOUT.getName(), "7000").trim()));
        setDelayAfterConnect(Integer.parseInt(properties.getTypedProperty(DELAY_AFTER_CONNECT, "0").trim()));
        setBlockSize(Integer.parseInt(properties.getTypedProperty(BLOCK_SIZE, "128").trim()));
        setDaisyChain(Integer.parseInt(properties.getTypedProperty(DAISY_CHAIN, "0").trim()));
        setAllowClockSet(Integer.parseInt(properties.getTypedProperty(ALLOW_CLOCK_SET,"0").trim()) == 1);

        setUnitIdMaster(properties.getTypedProperty(UNIT_ID_MASTER));
        setSecurityLevelMaster(Integer.parseInt(properties.getTypedProperty(SECURITY_LEVEL_MASTER, "1")));
        setPasswordMaster(properties.getTypedProperty(PASSWORD_MASTER, ""));
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        setSchlumbergerConnection(new SchlumbergerConnection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, 0));
        setCommandFactory(new CommandFactory(this));
        doTheInit();
        return getSchlumbergerConnection();
    }

    public SchlumbergerConnection getSchlumbergerConnection() {
        return schlumbergerConnection;
    }

    private void setSchlumbergerConnection(SchlumbergerConnection schlumbergerConnection) {
        this.schlumbergerConnection = schlumbergerConnection;
    }

    @Override
    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    private void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    public String getUnitType() {
        return unitType;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }

    public int getDelayAfterConnect() {
        return delayAfterConnect;
    }

    public void setDelayAfterConnect(int delayAfterConnect) {
        this.delayAfterConnect = delayAfterConnect;
    }

    public int getBlockSize() {
        return blockSize;
    }

    private void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public boolean isDaisyChain() {
        return daisyChain != 0;
    }

    public int getDaisyChain() {
        return daisyChain;
    }

    public void setDaisyChain(int daisyChain) {
        this.daisyChain = daisyChain;
    }

    public boolean isAllowClockSet() {
        return allowClockSet;
    }

    public void setAllowClockSet(boolean allowClockSet) {
        this.allowClockSet = allowClockSet;
    }

    public String getUnitId() {
        return unitId;
    }

    private void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public String getUnitIdMaster() {
        return unitIdMaster;
    }

    public void setUnitIdMaster(String unitIdMaster) {
        this.unitIdMaster = unitIdMaster;
    }

    public int getSecurityLevelMaster() {
        return securityLevelMaster;
    }

    public void setSecurityLevelMaster(int securityLevelMaster) {
        this.securityLevelMaster = securityLevelMaster;
    }

    public String getPasswordMaster() {
        return passwordMaster;
    }

    public void setPasswordMaster(String passwordMaster) {
        this.passwordMaster = passwordMaster;
    }
}