/*
 * RGLInfo.java
 *
 * Created on 17 mei 2005, 16:16
 */

package com.energyict.protocolimpl.emon.ez7.core.command;

import com.energyict.mdc.upl.io.NestedIOException;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocolimpl.emon.ez7.core.EZ7CommandFactory;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.List;
/**
 *
 * @author  Koen
 */
public class RGLInfo extends AbstractCommand {

    private static final int DEBUG=0;
    private static final String COMMAND="RGL";

    int groupNumber;
    int locationNumber;
    String DeviceId;
    String SerialNumber;
    String idrIdCode;
    int callInTypes;

    private static final int NR_OF_COLUMNS=8;
    private static final int NR_OF_ROWS=2;

    int[][] vals = new int[NR_OF_ROWS][NR_OF_COLUMNS];

    /** Creates a new instance of RGLInfo */
    public RGLInfo(EZ7CommandFactory ez7CommandFactory) {
        super(ez7CommandFactory);
    }

    public String toString() {
        return "RGLInfo: "+
               "groupNumber="+getGroupNumber()+", "+
               "locationNumber="+getLocationNumber()+", "+
               "DeviceId="+getDeviceId()+", "+
               "SerialNumber="+getSerialNumber()+", "+
               "idrIdCode="+getIdrIdCode()+", "+
               "callInTypes="+getCallInTypes();

    }

    public void build() throws ConnectionException, NestedIOException {
        // retrieve profileStatus
        byte[] data = ez7CommandFactory.getEz7().getEz7Connection().sendCommand(COMMAND);
        parse(data);
    }

    private void parse(byte[] data) {
        if (DEBUG>=1) {
            System.out.println(new String(data));
        }
        CommandParser cp = new CommandParser(data);

        List values = cp.getValues("LINE-1");
        for (int col=0;col<NR_OF_COLUMNS;col++) {
            vals[0][col] = Integer.parseInt((String) values.get(col), 16);
        }

        setGroupNumber(Integer.parseInt((String)values.get(0),16));
        setLocationNumber(Integer.parseInt((String) values.get(1), 16));
        setDeviceId(extractIdentificationNumber(values));

        values = cp.getValues("LINE-2");
        for (int col=0;col<NR_OF_COLUMNS;col++) {
            vals[1][col] = Integer.parseInt((String) values.get(col), 16);
        }

        byte[] idrIdCode = new byte[2];
        idrIdCode[0] = (byte)(Integer.parseInt((String)values.get(0),16)/0x100);
        idrIdCode[1] = (byte)(Integer.parseInt((String)values.get(0),16)%0x100);
        setIdrIdCode(new String(idrIdCode));
        setCallInTypes(Integer.parseInt((String)values.get(1),16));
        setSerialNumber(extractIdentificationNumber(values));
    }

    protected String extractIdentificationNumber(List values) {
        if (values.get(2).equals("0000") &&
                values.get(3).equals("0000")) {
            // Numerical identification - e.g.: 0000 0000 0000 2300 1234 5678 is for "230012345678"
            return (String) values.get(5) + (String) values.get(6) + (String) values.get(7);
        } else {
            // alpha-numerical identification represented as HEX - e.g.: 4241 3030 3031 3233 3334 3536 is for "BA0001233456"
            String hexString = (String) values.get(2) +(String) values.get(3) +(String) values.get(4) +(String) values.get(5) + (String) values.get(6) + (String) values.get(7);
            byte[] bytes = ProtocolTools.getBytesFromHexString(hexString, "");
            return new String(bytes);
        }
    }

    /**
     * Getter for property groupNumber.
     * @return Value of property groupNumber.
     */
    public int getGroupNumber() {
        return groupNumber;
    }

    /**
     * Setter for property groupNumber.
     * @param groupNumber New value of property groupNumber.
     */
    public void setGroupNumber(int groupNumber) {
        this.groupNumber = groupNumber;
    }

    /**
     * Getter for property locationNumber.
     * @return Value of property locationNumber.
     */
    public int getLocationNumber() {
        return locationNumber;
    }

    /**
     * Setter for property locationNumber.
     * @param locationNumber New value of property locationNumber.
     */
    public void setLocationNumber(int locationNumber) {
        this.locationNumber = locationNumber;
    }

    /**
     * Getter for property DeviceId.
     * @return Value of property DeviceId.
     */
    public java.lang.String getDeviceId() {
        return DeviceId;
    }

    /**
     * Setter for property DeviceId.
     * @param DeviceId New value of property DeviceId.
     */
    public void setDeviceId(java.lang.String DeviceId) {
        this.DeviceId = DeviceId;
    }

    /**
     * Getter for property SerialNumber.
     * @return Value of property SerialNumber.
     */
    public java.lang.String getSerialNumber() {
        return SerialNumber;
    }

    /**
     * Setter for property SerialNumber.
     * @param SerialNumber New value of property SerialNumber.
     */
    public void setSerialNumber(java.lang.String SerialNumber) {
        this.SerialNumber = SerialNumber;
    }



    /**
     * Getter for property callInTypes.
     * @return Value of property callInTypes.
     */
    public int getCallInTypes() {
        return callInTypes;
    }

    /**
     * Setter for property callInTypes.
     * @param callInTypes New value of property callInTypes.
     */
    public void setCallInTypes(int callInTypes) {
        this.callInTypes = callInTypes;
    }

    /**
     * Getter for property idrIdCode.
     * @return Value of property idrIdCode.
     */
    public java.lang.String getIdrIdCode() {
        return idrIdCode;
    }

    /**
     * Setter for property idrIdCode.
     * @param idrIdCode New value of property idrIdCode.
     */
    public void setIdrIdCode(java.lang.String idrIdCode) {
        this.idrIdCode = idrIdCode;
    }

    public int getValue(int col, int row) {
        try {
            return vals[row][col];
        }
        catch(ArrayIndexOutOfBoundsException e) {
            return -1;
        }
    }
}
