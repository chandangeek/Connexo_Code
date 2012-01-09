/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220.profile;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.iec1107.instromet.dl220.DL220Utils;

import java.io.IOException;


/**
 * Defines an Event Record configuration
 * 
 * @author gna
 * @since 10-mrt-2010
 *
 */
public class DL220EventRecordConfig {
	
	private static final String STR_TIME = "Zeit";
	private static final String STR_EVENT = "Er";	// Error or Event ...
	
	private String recordConfig;

	private int timeIndex = -1;
	private int eventIndex = -1;

	/**
	 * Constructor
	 * 
	 * @param recordConfig
	 * 			- the raw config string from the device
	 */
	public DL220EventRecordConfig(String recordConfig){
		this.recordConfig = recordConfig;
	}
	
	/**
	 * Parse the configuration string with the representing indexes
	 * 
	 * @throws IOException if the number of open- and closed brackets doesn't match
	 */
	protected void parse() throws IOException{
		int index = 0;
		int beginIndex = 0;
		int endIndex = 0;
		String str = "";
		do{
			beginIndex = this.recordConfig.indexOf("(", endIndex);
			endIndex = this.recordConfig.indexOf(")", beginIndex) + 1;
			str = ProtocolUtils.stripBrackets(this.recordConfig.substring(beginIndex, endIndex));
			if(STR_TIME.equalsIgnoreCase(str)){
				setTimeIndex(index);
			} else if ((str.indexOf(STR_EVENT) > -1) && this.eventIndex ==-1){
				setErrorIndex(index);
			}
			
			index++;
		} while(index < getNumberOfObjectsPerRecord());
	}
	
	/**
	 * @return the number of objects in a record
	 * 
	 * @throws IOException if the number of open- and closed brackets doesn't match
	 */
	public int getNumberOfObjectsPerRecord() throws IOException{
		return DL220Utils.getNumberOfObjects(this.recordConfig);
	}
	
	/**
	 * @param index
	 */
	private void setErrorIndex(int index) {
		this.eventIndex = index;
	}

	/**
	 * @param index
	 */
	private void setTimeIndex(int index) {
		this.timeIndex = index;
	}

	/**
	 * @return the timeIndex
	 * 
	 * @throws IOException if parsing the raw object configuration failed
	 */
	public int getTimeIndex() throws IOException {
		if(this.timeIndex == -1){
			parse();
		}
		return timeIndex;
	}

	/**
	 * @return the eventIndex
	 * 
	 * @throws IOException if parsing the raw object configuration failed
	 */
	public int getEventIndex() throws IOException {
		if(this.eventIndex == -1){
			parse();
		}
		return this.eventIndex;
	}
	

}
