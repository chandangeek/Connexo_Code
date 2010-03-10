/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220;

import com.energyict.protocol.ProtocolUtils;

/**
 * Defines the indexes of the different objects in a {@link DL220IntervalRecord}
 * 
 * @author gna
 * @since 8-mrt-2010
 *
 */
public class DL220IntervalRecordConfig {

	private static final String STR_TIME = "Zeit";
	private static final String STR_VALUE = "V1";
	private static final String STR_STATUS = "StSy";
	private static final String STR_EVENT = "Er";	// Error or Event ...
	
	private String recordConfig;

	private int timeIndex = -1;
	private int valueIndex = -1;
	private int statusIndex = -1;
	private int eventIndex = -1;
	
	
	public DL220IntervalRecordConfig(String recordConfig){
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
			} else if ((str.indexOf(STR_VALUE) > -1) && this.valueIndex == -1){
				setValueIndex(index);
			} else if ((str.indexOf(STR_STATUS) > -1) && this.statusIndex == -1){
				setStatusIndex(index);
			} else if ((str.indexOf(STR_EVENT) > -1) && this.eventIndex ==-1){
				setErrorIndex(index);
			}
			
			index++;
		} while(index < 9);
	}
	
	/**
	 * @return the timeIndex
	 */
	public int getTimeIndex() {
		if(this.timeIndex == -1){
			parse();
		}
		return timeIndex;
	}

	/**
	 * @return the valueIndex
	 */
	public int getValueIndex() {
		if(this.valueIndex == -1){
			parse();
		}
		return valueIndex;
	}
	
	/**
	 * @return the statusIndex
	 */
	public int getStatusIndex(){
		if(this.statusIndex == -1){
			parse();
		}
		return this.statusIndex;
	}
	

	/**
	 * @return the errorIndex
	 */
	public int getEventIndex() {
		if(this.eventIndex == -1){
			parse();
		}
		return this.eventIndex;
	}
	
	
	/**
	 * @param statusIndex 
	 * 				- the statusIndex to set
	 */
	protected void setStatusIndex(int statusIndex){
		this.statusIndex = statusIndex;
	}
	
	/**
	 * @param timeIndex 
	 * 				- the timeIndex to set
	 */
	protected void setTimeIndex(int timeIndex) {
		this.timeIndex = timeIndex;
	}

	/**
	 * @param valueIndex 
	 * 				- the valueIndex to set
	 */
	protected void setValueIndex(int valueIndex) {
		this.valueIndex = valueIndex;
	}
	
	/**
	 * @param errorIndex
	 * 				- the errorIndex to set
	 */
	protected void setErrorIndex(int errorIndex) {
		this.eventIndex = errorIndex;
	}
}
