/*
 * VDEWRegister.java
 *
 */

package com.energyict.protocolimpl.iec1107.iskraemeco.mt83.vdew;

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.iskraemeco.mt83.MT83;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 *
 * @author  Koen
 * changes:
 * KV 17022004 extended with MeterExceptionInfo
 */
public class VDEWRegister extends VDEWRegisterDataParse {


    static public final boolean CACHED=true;
    static public final boolean NOT_CACHED=false;

    static public final boolean WRITEABLE=true;
    static public final boolean NOT_WRITEABLE=false;

    private static final int DEBUG = 1;

    private String objectId;
    private int type;
    private int offset;
    private int length;
    private Unit unit;
    private byte[] regdata=null;

    private boolean writeable;
    private boolean cached;
    //private boolean datareadout;
    private boolean usePassword;
    private byte[] readCommand=null,writeCommand=null;

    private AbstractVDEWRegistry abstractVDEWRegistry=null;

    /** Creates a new instance of VDEWRegister */
    /*
     *  String objectId if string contains spaces, then it is a compount register.
     *  Compount registers are a concatenation of some registers to being parsed.
     *  E.g. time (hhmmss) and date (yymmdd) are two different registers. The strings
     *  of time and date are concatenated together (yymmddhhmmss) and then parsed as
     *  a Date object.
     */
    public VDEWRegister(String objectId, int type, int offset, int length, Unit unit, boolean writeable, boolean cached) {
        this(objectId,type,offset,length,unit,writeable,cached,FlagIEC1107Connection.READ5);
    }

    public VDEWRegister(String objectId, int type, int offset, int length, Unit unit, boolean writeable, boolean cached, boolean usePassword) {
        this(objectId,type,offset,length,unit,writeable,cached,FlagIEC1107Connection.READ5,FlagIEC1107Connection.WRITE5,usePassword);
    }

    public VDEWRegister(String objectId, int type, int offset, int length, Unit unit, boolean writeable, boolean cached, byte[] readCommand) {
        this(objectId,type,offset,length,unit,writeable,cached,readCommand,FlagIEC1107Connection.WRITE5);
    }

    /** Creates a new instance of VDEWRegister */
    public VDEWRegister(String objectId, int type, int offset, int length, Unit unit, boolean writeable, boolean cached, byte[] readCommand, byte[] writeCommand) {
        this(objectId,type,offset,length,unit,writeable,cached,readCommand,writeCommand,true);
    }

    public VDEWRegister(String objectId, int type, int offset, int length, Unit unit, boolean writeable, boolean cached, byte[] readCommand, byte[] writeCommand, boolean usePassword) {
        this.objectId = objectId;
        this.type = type;
        this.offset = offset;
        this.length = length;
        this.unit = unit;
        this.writeable = writeable;
        this.cached = cached;
        this.regdata = null;
        this.readCommand = readCommand;
        this.writeCommand = writeCommand;
        this.usePassword=usePassword;
    }


    protected void setAbstractVDEWRegistry(AbstractVDEWRegistry abstractVDEWRegistry) {
        this.abstractVDEWRegistry = abstractVDEWRegistry;
    }

    protected ProtocolLink getProtocolLink() {
        return abstractVDEWRegistry.getProtocolLink();
    }

    protected byte[] getReadCommand() {
        return readCommand;
    }

    protected byte[] getWriteCommand() {
        return writeCommand;
    }

    protected Unit getUnit() {
        return unit;
    }
    protected int getType() {
        return type;
    }
    protected int getOffset() {
        return offset;
    }
    protected int getLength() {
        return length;
    }
    protected boolean isWriteable() {
        return writeable;
    }
    protected boolean isCached() {
        return cached;
    }

    protected FlagIEC1107Connection getFlagIEC1107Connection() {
        return getProtocolLink().getFlagIEC1107Connection();
    }
    protected String getObjectID() {
        return objectId;
    }
    protected void setObjectID(String objectId) {
        this.objectId = objectId;
    }
    protected void setType(int type) {
        this.type = type;
    }
    protected void setReadCommand(byte[] readCommand) {
        this.readCommand = readCommand;
    }
    protected void setCached(boolean cached) {
        this.cached = cached;
    }


    protected void writeRegister(String value) throws FlagIEC1107ConnectionException,IOException {
        dowriteRawRegister(value);
    }
    protected void writeRegister(Object object) throws FlagIEC1107ConnectionException,IOException {
        dowriteRawRegister(buildData(object));
    }

    private void dowriteRawRegister(String value) throws FlagIEC1107ConnectionException,IOException {
        String data = null;

        if (isUsePassword() && ! getWriteCommand().equals(FlagIEC1107Connection.WRITE1)) {
            data = getObjectID() + "(" + value + ")(" + getProtocolLink().getPassword() + ")";
        } else {
            data = getObjectID() + "(" + value + ")";
        }

//        if (getWriteCommand().equals(FlagIEC1107Connection.WRITE5)) {
//            if (isUsePassword()) {
//                data = getObjectID() + "(" + value + ")(" + getProtocolLink().getPassword() + ")";
//            } else {
//                data = getObjectID() + "(" + value + ")";
//            }
//        } else if (getWriteCommand().equals(FlagIEC1107Connection.WRITE1)) {
//            data = getObjectID() + "(" + value + ")";
//        } else if (getWriteCommand().equals(FlagIEC1107Connection.WRITE2)) {
//            if (isUsePassword()) {
//                data = getObjectID() + "(" + value + ")(" + getProtocolLink().getPassword() + ")";
//            } else {
//                data = getObjectID() + "(" + value + ")";
//            }
//        }
        String retval = getProtocolLink().getFlagIEC1107Connection().sendRawCommandFrameAndReturn(getWriteCommand(), data.getBytes());

        if (retval != null) {
            abstractVDEWRegistry.validateData(retval);
        }
        resetRegdata();
    }

    protected void resetRegdata() {
        regdata = null;
    }

    // read register in the meter if not cached
    protected byte[] readRegister(boolean cached) throws FlagIEC1107ConnectionException,IOException {
        if (cached && (getProtocolLink().getDataReadout() != null)) {
            try {
                regdata = doReadDataReadoutRawRegister();
            } catch (IOException e) {
                regdata = doReadRawRegister();
            }
        }
        else {
            if ((!cached) || (regdata == null)) {
                regdata = doReadRawRegister();
            }
        }
        return regdata;
    }

    // read register in the meter
    private byte[] doReadDataReadoutRawRegister() throws FlagIEC1107ConnectionException,IOException {
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        StringTokenizer st = new StringTokenizer(getObjectID()," ");
        while(st.countTokens() > 0) {
            String token = st.nextToken();
            byte[] data = getData(token);
            abstractVDEWRegistry.validateData(data);
            ba.write(data);
        }
        regdata = ba.toByteArray();
        return regdata;
    }

    private byte[] getData(String token) throws IOException {
        String strdump = new String(getProtocolLink().getDataReadout());
        StringBuffer strbuffer = new StringBuffer();
        int state=0;
        int index = strdump.indexOf(token);

        if (index == -1) throw new IOException("VDEWRegister, getData, register "+getObjectID()+" does not exist in datareadout!");
        for (int i = index; i < strdump.length() ; i++) {
            if (state == 0) {
                if (strdump.charAt(i) == '(') state = 1;
            }
            else if (state == 1) {
                if (strdump.charAt(i) == ')') break;
                strbuffer.append(strdump.charAt(i));
            }
        } // for (int i = index; i < strdump.length() ; i++)
        return strbuffer.toString().getBytes();
    }

    // read register in the meter
    private byte[] doReadRawRegister() throws IOException {
        try {
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            StringTokenizer st = new StringTokenizer(getObjectID()," ");
            while(st.countTokens() > 0) {
                String token = st.nextToken()+"()";
                MT83.sendDebug("doReadRawRegister()" + token, DEBUG);
                getProtocolLink().getFlagIEC1107Connection().sendRawCommandFrame(getReadCommand(),token.getBytes());
                byte[] data = null;
                if (getType() == VDEW_DATE_VALUE_PAIR) {
                    data = getProtocolLink().getFlagIEC1107Connection().receiveRawData();
                    abstractVDEWRegistry.validateData(data);
                }
                else {
                    data = getProtocolLink().getFlagIEC1107Connection().receiveRawData();
                    abstractVDEWRegistry.validateData(data);
                    data = getProtocolLink().getFlagIEC1107Connection().parseDataBetweenBrackets(data);
                    abstractVDEWRegistry.validateData(data);
                }
                ba.write(data);
            }
            regdata = ba.toByteArray();
            return regdata;
        }
        catch(FlagIEC1107ConnectionException e) {
            throw new IOException("VDEWRegister, doReadRawRegister, FlagIEC1107ConnectionException, "+e.getMessage());
        }
    } // private byte[] doReadRawRegister()



    /**
     * Getter for property usePassword.
     * @return Value of property usePassword.
     */
    public boolean isUsePassword() {
        return usePassword;
    }

    /**
     * Setter for property usePassword.
     * @param usePassword New value of property usePassword.
     */
    public void setUsePassword(boolean usePassword) {
        this.usePassword = usePassword;
    }

 // private void validateData(byte[] data) throws IOException

} // public class VDEWRegister
