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

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.itron.protocol.schlumberger.CommandFactory;
import com.energyict.protocolimpl.itron.protocol.schlumberger.SchlumbergerConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Koen
 */
public abstract class SchlumbergerProtocol extends AbstractProtocol implements ProtocolLink {

    protected abstract void doTheInit();
    protected abstract void doTheDoValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException;
    protected abstract List<String> doTheDoGetOptionalKeys();
    protected abstract void doTheConnect() throws IOException;
    protected abstract void doTheDisConnect() throws IOException;
    protected abstract void hangup() throws IOException;

    private SchlumbergerConnection schlumbergerConnection=null;
    private CommandFactory commandFactory=null;
    private int delayAfterConnect;
    private String unitType;
    private String unitId;
    private int blockSize;
    private boolean allowClockSet;
    private boolean daisyChain;

    public SchlumbergerProtocol(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    protected void doConnect() throws IOException {

        getSchlumbergerConnection().delayAndFlush(getDelayAfterConnect());

        // is it allowd to send an extra I and S? why shouldn't it?
        //if ((!isDaisyChain()) || (isDaisyChain() && ((getInfoTypeNodeAddress()==null) || (getInfoTypeNodeAddress().compareTo("")==0)))) {
            getCommandFactory().enqCommand();
            getCommandFactory().getIdentifyCommand(getUnitId(), getUnitType());
            if (getInfoTypeSecurityLevel()>=1) {
                getCommandFactory().securityCommand(getInfoTypePassword());
            }
        //}

        if (isDaisyChain()) {
            if ((getInfoTypeNodeAddress()!=null) && (getInfoTypeNodeAddress().compareTo("")!=0)) {
                hangup();
                getSchlumbergerConnection().sendEnqMultidrop(20,Integer.parseInt(getInfoTypeNodeAddress()));
                getCommandFactory().getIdentifyCommand(getUnitId(), getUnitType());
                if (getInfoTypeSecurityLevel()>=1) {
                    getCommandFactory().securityCommand(getInfoTypePassword());
                }
            }
        }

        doTheConnect();
    }

    protected void doDisConnect() throws IOException {
        if (isDaisyChain()) {
            getSchlumbergerConnection().sendEnqMultidrop(2);
        }
        else {
            hangup();
        }
    }

    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay","0").trim()));
        setInfoTypeSecurityLevel(Integer.parseInt(properties.getProperty("SecurityLevel","1").trim()));
        setUnitType(properties.getProperty("UnitType"));
        setUnitId(properties.getProperty("UnitId"));
        setInfoTypeNodeAddress(properties.getProperty("NodeAddress"));
        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getProperty("Timeout","7000").trim()));
        setDelayAfterConnect(Integer.parseInt(properties.getProperty("DelayAfterConnect","0").trim()));
        setBlockSize(Integer.parseInt(properties.getProperty("BlockSize","128").trim()));
        setDaisyChain(Integer.parseInt(properties.getProperty("DaisyChain","0").trim()) == 1);
        setAllowClockSet(Integer.parseInt(properties.getProperty("AllowClockSet","0").trim()) == 1);
        doTheDoValidateProperties(properties);
    }

    protected List<String> doGetOptionalKeys() {
        List<String> list = new ArrayList<>();
        list.add("UnitType");
        list.add("UnitId");
        list.add("DelayAfterConnect");
        list.add("BlockSize");
        list.add("DaisyChain");
        list.add("AllowClockSet");
        list.addAll(doTheDoGetOptionalKeys());
        return list;
    }

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
        return daisyChain;
    }

    public void setDaisyChain(boolean daisyChain) {
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



}
