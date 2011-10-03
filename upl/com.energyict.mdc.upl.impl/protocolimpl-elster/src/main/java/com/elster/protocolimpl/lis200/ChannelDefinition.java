package com.elster.protocolimpl.lis200;

/**
 * class to hold channel info
 * 
 * @author gh
 * @since 19-apr-2010
 * 
 */

public class ChannelDefinition {

	/** index of channel*/
	private int channelNo;
	/** type of channel, eg. Counter, Interval value, Analoge value */
	private String channelType;
	/** number of digits of counter */
	private int channelOv;
	/** column in archive */
	private int archiveCol;
	
	/**
	 * 
	 * @param channelInfoString is of form "CHN"nnn['[''C'[ov]']']
	 * samples:
	 * CHN001    is a regular channel at index 1 (No Advances!)
	 * CHN2[C]   channel at index 2 with advances (counter), overflow at 999 999 999 (default)
	 * CHN03[C8] channel at index 3, with advances, overflow at 99 999 999
	 * @param archiveColumn - index of column in archive line 
	 */
	public ChannelDefinition(String channelInfoString, int archiveColumn)
	{
	    archiveCol = archiveColumn;
		channelOv = 9;
	  	channelType = "I";
	  	
		String data = channelInfoString.substring(3);
	  	
	  	String[] parts = data.split("\\[");
	  	
	  	channelNo = Integer.parseInt(parts[0]);
	  	
        if (parts.length > 1) {
        	int end = parts[1].indexOf("]");
        	String ai = parts[1].substring(0, end);
        	if (ai.toUpperCase().charAt(0) == 'C') {
                channelType = "C";
                if (ai.length() > 1) {
                	channelOv = Integer.parseInt(ai.substring(1));
                }
        	}
        }	
	}

	public int getChannelNo() {
		return channelNo;
	}

	public String getChannelType() {
		return channelType;
	}

	public int getChannelOv() {
		return channelOv;
	}
	
	public int getArchiveColumn() {
		return archiveCol;
	}	
}
