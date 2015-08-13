/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220.profile;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.iec1107.instromet.dl220.DL220RecordConfig;
import com.energyict.protocolimpl.iec1107.instromet.dl220.DL220Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines the indexes of the different objects in a {@link DL220IntervalRecord}
 * 
 * TODO check the addresses instead of the namings
 * 
 * @author gna
 * @since 8-mrt-2010
 *
 */
public class DL220IntervalRecordConfig implements DL220RecordConfig {

	private static final String STR_TIME = "Zeit";
	private static final String STR_VALUE = "V";
	private static final String STR_STATUS = "StSy";
	private static final String STR_EVENT = "Er";	// Error or Event ...
	
	private String recordConfig;

	private int timeIndex = -1;
	private int statusIndex = -1;
	private int eventIndex = -1;
	/** Contains a list of the indexes of the values*/
	private List<Integer> valueIndexes;
	
	/**
	 * Default constructor
	 * 
	 * @param recordConfig
	 * 				- the raw record configuration
	 */
	public DL220IntervalRecordConfig(String recordConfig){
		this.recordConfig = recordConfig;
	}
	
	/**
	 * Parse the configuration string with the representing indexes
	 * @throws IOException 
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
			} else if (str.indexOf(STR_VALUE) > -1){
				addConfigValue(index);
			} else if ((str.indexOf(STR_STATUS) > -1) && this.statusIndex == -1){
				setStatusIndex(index);
			} else if ((str.indexOf(STR_EVENT) > -1) && this.eventIndex ==-1){
				setErrorIndex(index);
			}
			
			index++;
		} while(index < getNumberOfObjectsPerRecord());
	}
	
	/**
	 * Add an index to the {@link #valueIndexes}
	 * @param index
	 */
	private void addConfigValue(int index){
		if(this.valueIndexes == null){
			this.valueIndexes = new ArrayList<Integer>();
		}
		valueIndexes.add(index);
	}
	
	/**
	 * @return the number of channels 
	 */
	public int getNumberOfChannels() throws IOException{
		if(this.valueIndexes == null){
			parse();
		}
		return this.valueIndexes.size();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getTimeIndex() throws IOException {
		if(this.timeIndex == -1){
			parse();
		}
		return timeIndex;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getValueIndex(int index) throws IOException {
		if(this.valueIndexes == null){
			parse();
		}
		return this.valueIndexes.get(index);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getStatusIndex() throws IOException{
		if(this.statusIndex == -1){
			parse();
		}
		return this.statusIndex;
	}
	

	/**
	 * {@inheritDoc}
	 */
	public int getEventIndex() throws IOException{
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
	 * @param errorIndex
	 * 				- the errorIndex to set
	 */
	protected void setErrorIndex(int errorIndex) {
		this.eventIndex = errorIndex;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getNumberOfObjectsPerRecord() throws IOException {
		return DL220Utils.getNumberOfObjects(this.recordConfig);
	}
}
