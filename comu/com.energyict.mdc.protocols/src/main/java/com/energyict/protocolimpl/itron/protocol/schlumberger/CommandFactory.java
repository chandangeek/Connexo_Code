/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * CommandFactory.java
 *
 * Created on 7 september 2006, 17:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.protocol.schlumberger;

import com.energyict.protocolimpl.itron.protocol.SchlumbergerProtocol;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class CommandFactory {

    private SchlumbergerProtocol schlumbergerProtocol;

    IdentifyCommand identifyCommand=null;

    /** Creates a new instance of CommandFactory */
    public CommandFactory(SchlumbergerProtocol schlumbergerProtocol) {
        this.setSchlumbergerProtocol(schlumbergerProtocol);
    }

    public SchlumbergerProtocol getSchlumbergerProtocol() {
        return schlumbergerProtocol;
    }

    public void setSchlumbergerProtocol(SchlumbergerProtocol schlumbergerProtocol) {
        this.schlumbergerProtocol = schlumbergerProtocol;
    }

    public IdentifyCommand getIdentifyCommand() throws IOException {
        return getIdentifyCommand(null, null);
    }

    public IdentifyCommand getIdentifyCommand(String unitId, String unitType) throws IOException {
        //if (identifyCommand==null) {
            identifyCommand = new IdentifyCommand(getSchlumbergerProtocol());
            identifyCommand.setUnitId(unitId);
            identifyCommand.setUnitType(unitType);
            identifyCommand.invoke();
        //}
        return identifyCommand;
    }

    public void securityCommand(String securityCode) throws IOException {
        SecurityCommand securityCommand = new SecurityCommand(getSchlumbergerProtocol());
        securityCommand.setSecurityCode(securityCode);
        securityCommand.invoke();
    }

    public void downloadCommand(int firstAddress, int lastAddress, byte[] data) throws IOException {
        DownloadCommand downloadCommand = new DownloadCommand(getSchlumbergerProtocol());
        downloadCommand.setFirstAddress(firstAddress);
        downloadCommand.setLastAddress(lastAddress);
        downloadCommand.invoke();
        downloadCommand.setData(data);
        downloadCommand.invoke();
    }

    public UploadCommand getUploadCommand(int firstAddress, int lastAddress) throws IOException {
        UploadCommand uploadCommand = new UploadCommand(getSchlumbergerProtocol());
        uploadCommand.setFirstAddress(firstAddress);
        uploadCommand.setLastAddress(lastAddress);
        uploadCommand.invoke();
        return uploadCommand;
    }

    public void enqCommand() throws IOException {
        ENQCommand enq = new ENQCommand(getSchlumbergerProtocol());
        enq.invoke();
    }

} // public class CommandFactory
