/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * AbstractDataReadingCommand.java
 *
 * Created on 25 oktober 2004
 */

package com.energyict.protocolimpl.iec1107.enermete70x;

import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.iec1107.IEC1107Connection;

import java.io.IOException;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 */
public abstract class AbstractDataReadingCommand {

    abstract public void parse(byte[] data, TimeZone timeZone) throws java.io.IOException;

    DataReadingCommandFactory dataReadingCommandFactory;

    /** Creates a new instance of AbstractDataReadingCommand */
    public AbstractDataReadingCommand(DataReadingCommandFactory dataReadingCommandFactory) {
        this.dataReadingCommandFactory=dataReadingCommandFactory;
    }

    public void retrieve(String command) throws IOException {
        retrieve(command,"");
    }

    public void retrieve(String command,String data) throws IOException {
        byte[] retVal = read(command,data);
        parse(retVal,getDataReadingCommandFactory().getEnermet().getTimeZone());
    }

    /*
     *  Must be overridden to implement the data builder...
     */
    protected byte[] buildData() {
        return null;
    }

    protected void write(String command, String data) throws ProtocolConnectionException,IOException {
        StringBuffer strbuff = new StringBuffer();
        strbuff.append(command);
        strbuff.append('(');
        strbuff.append(data);
        strbuff.append(')');
        String str = getDataReadingCommandFactory().getEnermet().getIec1107Connection().sendRawCommandFrameAndReturn(IEC1107Connection.WRITE1,strbuff.toString().getBytes());
        if (str != null)
             validateData(str);
    }

    private byte[] read(String Command,String data) throws ProtocolConnectionException,IOException {
        StringBuffer strbuff = new StringBuffer();
        strbuff.append(Command);
        strbuff.append('(');
        strbuff.append(data);
        strbuff.append(')');
        getDataReadingCommandFactory().getEnermet().getIec1107Connection().sendRawCommandFrame(IEC1107Connection.READ1,strbuff.toString().getBytes());
        byte[] ba = getDataReadingCommandFactory().getEnermet().getIec1107Connection().receiveRawData();
        return validateData(ba);
    }

    protected String buildLength(int value,int length) {
        String str=Integer.toHexString(value);
        StringBuffer strbuff = new StringBuffer();
        if (length >= str.length())
            for (int i=0;i<(length-str.length());i++)
                strbuff.append('0');
        strbuff.append(str);
        return strbuff.toString();
    }

    private void validateData(String str) throws ProtocolConnectionException {
        validateData(str.getBytes());
    }

    private byte[] validateData(byte[] data) throws ProtocolConnectionException {
        String str = new String(data);
        int errorCodeReturnIndex = str.indexOf("([");

        // check explicit if ([..]) starts at first position in returned data. in the register data ([4]) indicates
        // that a certain register is not existing
        if (errorCodeReturnIndex == 0) {
            throw new ProtocolConnectionException("AbstractLogicalAddress, validateData, "+getDataReadingCommandFactory().getMeterExceptionInfo().getExceptionInfo(str),str);
        }
        return data;
    }




    /**
     * Getter for property dataReadingCommandFactory.
     * @return Value of property dataReadingCommandFactory.
     */
    public com.energyict.protocolimpl.iec1107.enermete70x.DataReadingCommandFactory getDataReadingCommandFactory() {
        return dataReadingCommandFactory;
    }
}
