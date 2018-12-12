/*
 * MICommand.java
 *
 * Created on 11 augustus 2005, 11:28
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
/**
 *
 * @author koen
 */
public class MICommand  extends AbstractCommand {
    
    private static final CommandIdentification commandIdentification = new CommandIdentification("MI");

    private String modelNumber;
    private String serialNumber;
    
    /** Creates a new instance of MICommand */
    public MICommand(CommandFactory commandFactory) {
        super(commandFactory);
    }
    
    public String toString() {
        return "MICommand: "+getModelNumber()+", "+getSerialNumber();
    }
    
    protected void parse(String strData) throws IOException {
        BufferedReader br = new BufferedReader(new StringReader(strData));
        setModelNumber(br.readLine());
        setSerialNumber(br.readLine().trim());
    }
    
    protected CommandIdentification getCommandIdentification() {
        return commandIdentification;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public boolean is8ChannelMeter() {
        return getModelNumber().charAt(4) == 'L';
    }
    
    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override
    protected String getCommandName() {
        return "MI";
    }
}
