/*
 * VersionRead.java
 *
 * Created on 25 oktober 2004, 17:12
 */

package com.energyict.protocolimpl.iec1107.enermete70x;

/**
 *
 * @author  Koen
 */
public class VersionRead extends AbstractDataReadingCommand {
    
    String version=null;
    
    /** Creates a new instance of VersionRead */
    public VersionRead(DataReadingCommandFactory drcf) {
        super(drcf);
    }
    
    public void parse(byte[] data, java.util.TimeZone timeZone) throws java.io.IOException {
        version = new String(data);
    }
    
    /**
     * Getter for property version.
     * @return Value of property version.
     */
    public java.lang.String getVersion() throws java.io.IOException {
        if (version == null)
            retrieve("VER");
        return version;
    }    

}
