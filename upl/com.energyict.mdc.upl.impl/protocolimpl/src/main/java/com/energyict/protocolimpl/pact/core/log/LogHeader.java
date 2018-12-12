/*
 * LogHeader.java
 *
 * Created on 30 maart 2004, 11:12
 */

package com.energyict.protocolimpl.pact.core.log;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class LogHeader {
    
	private int logId;
	private int flags;
	private int eventCounter;
	private int logSize;
    
    /** Creates a new instance of LogHeader */
    public LogHeader(byte[] data) {
        parse(data);
    }
    
    private void parse(byte[] data) {
        try {
           setLogId(ProtocolUtils.byte2int(data[1]));
           setFlags(ProtocolUtils.byte2int(data[2]));
           setEventCounter(ProtocolUtils.getIntLE(data,3,3));
           setLogSize(ProtocolUtils.getIntLE(data,6,2));
        }
        catch (IOException e) {
            e.printStackTrace(); // should never happen!   
        }
    }
    
    /** Getter for property logId.
     * @return Value of property logId.
     *
     */
    public int getLogId() {
        return logId;
    }
    
    /** Setter for property logId.
     * @param logId New value of property logId.
     *
     */
    public void setLogId(int logId) {
        this.logId = logId;
    }
    
    /** Getter for property flags.
     * @return Value of property flags.
     *
     */
    public int getFlags() {
        return flags;
    }
    
    /** Setter for property flags.
     * @param flags New value of property flags.
     *
     */
    public void setFlags(int flags) {
        this.flags = flags;
    }
    
    /** Getter for property eventCounter.
     * @return Value of property eventCounter.
     *
     */
    public int getEventCounter() {
        return eventCounter;
    }
    
    /** Setter for property eventCounter.
     * @param eventCounter New value of property eventCounter.
     *
     */
    public void setEventCounter(int eventCounter) {
        this.eventCounter = eventCounter;
    }
    
    /** Getter for property logSize.
     * @return Value of property logSize.
     *
     */
    public int getLogSize() {
        return logSize;
    }
    
    /** Setter for property logSize.
     * @param logSize New value of property logSize.
     *
     */
    public void setLogSize(int logSize) {
        this.logSize = logSize;
    }
    
}
