/*
 * surveyInfo.java
 *
 * Created on 11 maart 2004, 9:58
 */

package com.energyict.protocolimpl.pact.core.meterreading;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
/**
 *
 * @author  Koen
 */
public class SurveyInfo extends MeterReadingsBlockImpl { 
    
	private int blocks;
	private int days;
	private int parms;
	private int hist;
	private int nrOfChannels;
	private int profileInterval;
    
    /** Creates a new instance of surveyInfo */
    public SurveyInfo(byte[] data) {
        super(data);
    }
    
    public String print() {
        return "BLOCKS="+getBlocks()+", DAYS="+getDays()+", PARMS="+getParms()+", HIST="+getHist();   
        
    }
    
    protected void parse() throws IOException {
        setBlocks(ProtocolUtils.byte2int(getData()[1]));
        setProfileInterval((24*60*60)/((getBlocks()-1)*4));
        setDays(ProtocolUtils.byte2int(getData()[2])+ProtocolUtils.byte2int(getData()[4])*256);
        setParms(ProtocolUtils.byte2int(getData()[3]));
        if (getParms() == 255) {
			setNrOfChannels(0);
		} else {
			setNrOfChannels(getParms()+1);
		}
        setHist(ProtocolUtils.byte2int(getData()[5]));
    }
    
    /** Getter for property blocks.
     * @return Value of property blocks.
     *
     */
    public int getBlocks() {
        return blocks;
    }
    
    /** Setter for property blocks.
     * @param blocks New value of property blocks.
     *
     */
    public void setBlocks(int blocks) {
        this.blocks = blocks;
    }
    
    /** Getter for property days.
     * @return Value of property days.
     *
     */
    public int getDays() {
        return days;
    }
    
    /** Setter for property days.
     * @param days New value of property days.
     *
     */
    public void setDays(int days) {
        this.days = days;
    }
    
    /** Getter for property parms.
     * @return Value of property parms.
     *
     */
    public int getParms() {
        return parms;
    }
    
    /** Setter for property parms.
     * @param parms New value of property parms.
     *
     */
    public void setParms(int parms) {
        this.parms = parms;
    }
    
    /** Getter for property hist.
     * @return Value of property hist.
     *
     */
    public int getHist() {
        return hist;
    }
    
    /** Setter for property hist.
     * @param hist New value of property hist.
     *
     */
    public void setHist(int hist) {
        this.hist = hist;
    }
    
    /** Getter for property nrOfChannels.
     * @return Value of property nrOfChannels.
     *
     */
    public int getNrOfChannels() {
        return nrOfChannels;
    }
    
    /** Setter for property nrOfChannels.
     * @param nrOfChannels New value of property nrOfChannels.
     *
     */
    public void setNrOfChannels(int nrOfChannels) {
        this.nrOfChannels = nrOfChannels;
    }
    
    /** Getter for property profileInterval.
     * @return Value of property profileInterval.
     *
     */
    public int getProfileInterval() {
        return profileInterval;
    }
    
    /** Setter for property profileInterval.
     * @param profileInterval New value of property profileInterval.
     *
     */
    public void setProfileInterval(int profileInterval) {
        this.profileInterval = profileInterval;
    }
    
}
