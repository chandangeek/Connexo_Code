/*
 * Version.java
 *
 * Created on 17 mei 2005, 16:05
 */

package com.energyict.protocolimpl.emon.ez7.core.command;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.common.NestedIOException;
import com.energyict.protocolimpl.emon.ez7.core.EZ7CommandFactory;

import java.io.IOException;
import java.util.List;
/**
 *
 * @author  Koen
 */
public class Version extends AbstractCommand {

    private static final int DEBUG=0;
    private static final String COMMAND="RV";

    String completeVersionString;
    String versionString;

    /** Creates a new instance of Version */
    public Version(EZ7CommandFactory ez7CommandFactory) {
        super(ez7CommandFactory);
    }

    public String toString() {
        return "Version: "+ getCompleteVersionString();
    }

    public void build() throws IOException {
        // retrieve profileStatus
        byte[] data = ez7CommandFactory.getEz7().getEz7Connection().sendCommand(COMMAND);
        parse(data);
    }

    private void parse(byte[] data) throws NestedIOException {

        List values;

        if (DEBUG>=1)
           System.out.println(new String(data));
        String dataStr = new String(data);
        setCompleteVersionString(dataStr.trim().replaceAll("\r\n", ", "));

        CommandParser commandParser = new CommandParser(data);
        values = commandParser.getValues("VER:");
        if (values != null && !values.isEmpty()) {
            setVersionString(((String) values.get(0)).trim());
        }
    }

    public String getVersionString() {
        return versionString;
    }

    public void setVersionString(String versionString) {
        this.versionString = versionString;
    }

    /**
     * Getter for property version.
     * @return Value of property version.
     */
    public java.lang.String getCompleteVersionString() {
        return completeVersionString;
    }

    /**
     * Setter for property version.
     * @param completeVersionString New value of property version.
     */
    public void setCompleteVersionString(java.lang.String completeVersionString) {
        this.completeVersionString = completeVersionString;
    }

}
