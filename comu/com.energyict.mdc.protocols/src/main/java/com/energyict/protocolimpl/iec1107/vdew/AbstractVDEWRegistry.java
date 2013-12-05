/*
 * AbstractRegistry.java
 *
 * Created on 17 juni 2003, 8:37
 */

package com.energyict.protocolimpl.iec1107.vdew;

import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author  Koen
 * changes:
 * KV 17022004 extended with MeterExceptionInfo
 */
public abstract class AbstractVDEWRegistry {

    abstract protected void initRegisters();

    protected Map registers = new HashMap();
    protected ProtocolLink protocolLink=null;
    private MeterExceptionInfo meterExceptionInfo=null;
    public int registerSet;
    protected String dateFormat;

    private void addDefaultRegisters() {
        registers.put("DEFAULT_REGISTER", new VDEWRegister("",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED,FlagIEC1107Connection.READ1));
    }

    protected void initLocals() {
        addDefaultRegisters();
        Iterator iterator = registers.values().iterator();
        while(iterator.hasNext()) {
            VDEWRegister register = (VDEWRegister)iterator.next();
            register.setAbstractVDEWRegistry(this);
        }
    }
    protected ProtocolLink getProtocolLink() {
        return protocolLink;
    }
    protected Map getRegisters() {
        return registers;
    }



    public void setRegister(String name,String value) throws IOException {
        try {
            VDEWRegister register = findRegister(name);
            if (register.isWriteable()) register.writeRegister(value);
            else throw new IOException("AbstractVDEWRegistry, setRegister, register not writeable");

        }
        catch(FlagIEC1107ConnectionException e) {
            throw new IOException("AbstractVDEWRegistry, setRegister, "+e.getMessage());
        }
    }
    public void setRegister(String name,Object object) throws IOException {
        try {
            VDEWRegister register = findRegister(name);
            if (register.isWriteable()) register.writeRegister(object);
            else throw new IOException("AbstractVDEWRegistry, setRegister, register not writeable");

        }
        catch(FlagIEC1107ConnectionException e) {
            throw new IOException("AbstractVDEWRegistry, setRegister, "+e.getMessage());
        }
    }

    public Object getRegister(String name) throws IOException {
        VDEWRegister register = findRegister(name);
        return getRegister(name,register.isCached());
    }


    public Object getRegister(String name,boolean cached) throws IOException {
        try {
            VDEWRegister register = findRegister(name);
            return (register.parse(register.readRegister(cached)));
        }
        catch(FlagIEC1107ConnectionException e) {
            throw new IOException("AbstractVDEWRegistry, getRegister, "+e.getMessage());
        }
    }

    public byte[] getRegisterRawData(String name) throws IOException {
        try {
            VDEWRegister register = findRegister(name);
            return (register.readRegister(register.isCached()));
        }
        catch(FlagIEC1107ConnectionException e) {
            throw new IOException("AbstractVDEWRegistry, getRegisterRawData, "+e.getMessage());
        }
    }

    // search the map for the register info
    private VDEWRegister findRegister(String name) throws IOException {
        VDEWRegister register = (VDEWRegister)getRegisters().get(name);
        if (register == null) {
            Iterator iterator = getRegisters().values().iterator();
            while(iterator.hasNext()) {
                register = (VDEWRegister)iterator.next();
                if (name.compareTo(register.getObjectID()) == 0) {
                    return register;
                }
            }

            // If register does not exist, get the default one and set attributes
            // using the extended attributes following the registername separated by a space
            register = (VDEWRegister)getRegisters().get("DEFAULT_REGISTER");
            register.setObjectID(parseObjectId(name));
            register.setType(parseType(name));
            register.setReadCommand(parseReadCommand(name));
            register.setCached(parseCached(name));
            return register;
        }
        else {
            return register;
        }
    }

    private boolean parseCached(String name) {
        String attribs = getExtraAttributes(name);
        boolean cached = VDEWRegister.NOT_CACHED; // default
        if (attribs.indexOf("CACHED") != -1)
           cached = VDEWRegister.CACHED;
        if (attribs.indexOf("NOTCACHED") != -1)
           cached = VDEWRegister.NOT_CACHED;
        return cached;
    }

    private int parseType(String name) {
        String attribs = getExtraAttributes(name);
        int type = VDEWRegisterDataParse.VDEW_QUANTITY; // default
        if (attribs.indexOf("QUANTITY") != -1)
            type = VDEWRegisterDataParse.VDEW_QUANTITY;
        if (attribs.indexOf("STRING") != -1)
            type = VDEWRegisterDataParse.VDEW_STRING;
        if (attribs.indexOf("INTEGER") != -1)
            type = VDEWRegisterDataParse.VDEW_INTEGER;
        if (attribs.indexOf("DATE_VALUE_PAIR") != -1)
            type = VDEWRegisterDataParse.VDEW_DATE_VALUE_PAIR;
        return type;
    }
    private String parseObjectId(String name) {
        int index;
        if ((index=name.indexOf(" ")) != -1) {
            return name.substring(0,index);
        }
        else return name;
    }
    private byte[] parseReadCommand(String name) {
        String attribs = getExtraAttributes(name);
        byte[] readCommand = FlagIEC1107Connection.READ1; // default
        if (attribs.indexOf("R1") != -1)
            readCommand = FlagIEC1107Connection.READ1;
        if (attribs.indexOf("R5") != -1)
            readCommand = FlagIEC1107Connection.READ5;
        if(attribs.indexOf("R2") != -1)
        	readCommand = FlagIEC1107Connection.READ2;
        return readCommand;
    }
    private String getExtraAttributes(String name) {
        int index;
        if ((index=name.indexOf(" ")) != -1) {
            return name.substring(index,name.length()).toUpperCase();
        }
        else return "";
    }

    /** Getter for property meterExceptionInfo.
     * @return Value of property meterExceptionInfo.
     *
     */
    public MeterExceptionInfo getMeterExceptionInfo() {
        return meterExceptionInfo;
    }

    public void validateData(byte[] data) throws IOException {
        String str = new String(data);
        validateData(str);
    }

    public void validateData(String str) throws IOException {
        // Pure VDEW
        if (str.indexOf("(ERROR)") != -1) {
            if (getMeterExceptionInfo() != null) {
               str=ProtocolUtils.stripBrackets(str);
               throw new VDEWException("AbstractVDEWRegister, validateData, error received ("+str+") = "+getMeterExceptionInfo().getExceptionInfo(str));
            }
            else throw new VDEWException("AbstractVDEWRegister, validateData, error received ("+str+")");
        }
        // Pure VDEW
        else if ((str.indexOf("ERROR") != -1) && ((str.indexOf("ERROR")+"ERROR".length()) == str.length()))  {
            if (getMeterExceptionInfo() != null) {
               str=ProtocolUtils.stripBrackets(str);
               throw new VDEWException("AbstractVDEWRegister, validateData, error received ("+str+") = "+getMeterExceptionInfo().getExceptionInfo(str));
            }
            else throw new VDEWException("AbstractVDEWRegister, validateData, error received ("+str+")");
        }
        // Ferranti protocol
        else if ((str.indexOf("#") == 0) || (str.indexOf("(#") == 0)) {
            if (getMeterExceptionInfo() != null) {
               str=ProtocolUtils.stripBrackets(str);
               throw new FlagIEC1107ConnectionException("AbstractVDEWRegister, validateData, error received ("+str+") = "+getMeterExceptionInfo().getExceptionInfo(str));
            }
            else throw new FlagIEC1107ConnectionException("AbstractVDEWRegister, validateData, error received ("+str+")");
        }
        // A1500 protocol
        else if (str.indexOf("ERROR") != -1) {
            if (getMeterExceptionInfo() != null)
               throw new FlagIEC1107ConnectionException("AbstractVDEWRegister, validateData, error received ("+str+") = "+getMeterExceptionInfo().getExceptionInfo(str));
            else
               throw new FlagIEC1107ConnectionException("AbstractVDEWRegister, validateData, error received ("+str+")");
        }
        // Iskra EMECO protocol
        else if ((str.indexOf("ER") == 0) || (str.indexOf("(ER") == 0)) {
            if (getMeterExceptionInfo() != null) {
               String exceptionId = str.substring(str.indexOf("ER"),str.indexOf("ER")+4);
               throw new FlagIEC1107ConnectionException("AbstractVDEWRegister, validateData, error received ("+str+") = "+getMeterExceptionInfo().getExceptionInfo(exceptionId));
            }
            else throw new FlagIEC1107ConnectionException("AbstractVDEWRegister, validateData, error received ("+str+")");
        }

    }

    /**
     * Getter for property registerSet.
     * @return Value of property registerSet.
     */
    public int getRegisterSet() {
        return registerSet;
    }

    /**
     * Setter for property registerSet.
     * @param registerSet New value of property registerSet.
     */
    public void setRegisterSet(int registerSet) {
        this.registerSet = registerSet;
    }

    /** Creates a new instance of AbstractRegistry */
    public AbstractVDEWRegistry(MeterExceptionInfo meterExceptionInfo, ProtocolLink protocolLink) {
        this(meterExceptionInfo,protocolLink,-1, "yy/mm/dd");
    }

    public AbstractVDEWRegistry(MeterExceptionInfo meterExceptionInfo, ProtocolLink protocolLink, int registerSet) {
        this(meterExceptionInfo, protocolLink, registerSet, "yy/mm/dd");
    }

    public AbstractVDEWRegistry(MeterExceptionInfo meterExceptionInfo, ProtocolLink protocolLink, int registerSet, String dateFormat) {
        this.registerSet=registerSet;
        this.protocolLink = protocolLink;
        this.meterExceptionInfo=meterExceptionInfo;
        this.dateFormat = dateFormat;
        initRegisters();
        initLocals();
    }
}
