/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220;

import com.energyict.protocol.ProtocolUtils;


/**
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

	public DL220EventRecordConfig(String recordConfig){
		this.recordConfig = recordConfig;
	}
	
	/**
	 * Parse the configuration string with the representing indexes
	 */
	protected void parse(){
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
		} while(index < 9);
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
	 * @return
	 */
	public int getTimeIndex() {
		if(this.timeIndex == -1){
			parse();
		}
		return timeIndex;
	}

	/**
	 * @return
	 */
	public int getEventIndex() {
		if(this.eventIndex == -1){
			parse();
		}
		return this.eventIndex;
	}
	

}
