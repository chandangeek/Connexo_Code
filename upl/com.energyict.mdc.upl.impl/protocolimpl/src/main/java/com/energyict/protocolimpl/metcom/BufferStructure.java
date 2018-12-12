/*
 * BufferStructure.java
 *
 * Created on 13 december 2004, 18:48
 */

package com.energyict.protocolimpl.metcom;

import com.energyict.protocolimpl.utils.ProtocolUtils;

/**
 *
 * @author  Koen
 */
public class BufferStructure {
    
    int nrOfChannels;
    int nrOfDecades;
    int profileInterval;
    
    
    /** Creates a new instance of BufferStructure */
    public BufferStructure(byte[] data) {
       setNrOfChannels(ProtocolUtils.parseIntFromStr(data,0,2));
       setNrOfDecades(ProtocolUtils.parseIntFromStr(data,2,2));
       setProfileInterval(ProtocolUtils.parseIntFromStr(data,4,2));
    }
    
    public BufferStructure(int nrOfChannels,int nrOfDecades,int profileInterval) {
       setNrOfChannels(nrOfChannels);
       setNrOfDecades(nrOfDecades);
       setProfileInterval(profileInterval);
    }
    
    public String toString() {
        return "nrOfChannels="+getNrOfChannels()+", nrOfDecades="+getNrOfDecades()+", profileInterval="+getProfileInterval();
    }
    /**
     * Getter for property nrOfChannels.
     * @return Value of property nrOfChannels.
     */
    public int getNrOfChannels() {
        return nrOfChannels;
    }
    
    /**
     * Setter for property nrOfChannels.
     * @param nrOfChannels New value of property nrOfChannels.
     */
    public void setNrOfChannels(int nrOfChannels) {
        this.nrOfChannels = nrOfChannels;
    }
    
    /**
     * Getter for property nrOfDecades.
     * @return Value of property nrOfDecades.
     */
    public int getNrOfDecades() {
        return nrOfDecades;
    }
    
    /**
     * Setter for property nrOfDecades.
     * @param nrOfDecades New value of property nrOfDecades.
     */
    public void setNrOfDecades(int nrOfDecades) {
        this.nrOfDecades = nrOfDecades;
    }
    
    /**
     * Getter for property profileInterval.
     * @return Value of property profileInterval.
     */
    public int getProfileInterval() {
        return profileInterval;
    }
    
    /**
     * Setter for property profileInterval.
     * @param profileInterval New value of property profileInterval.
     */
    public void setProfileInterval(int profileInterval) {
        this.profileInterval = profileInterval;
    }
    
}
