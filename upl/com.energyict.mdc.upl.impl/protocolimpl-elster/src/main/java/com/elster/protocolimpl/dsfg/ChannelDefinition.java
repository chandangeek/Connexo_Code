package com.elster.protocolimpl.dsfg;

/**
 * class to hold channel info
 * 
 * @author gh
 * @since 19-apr-2010
 * 
 */

public class ChannelDefinition {

	/** address letter of archive */
	private String archiveAddress;
	/** index of channel */
	private int channelNo;
	/** type of channel, eg. Counter, Interval value, Analoge value */
	private String channelType;
	/** number of digits of counter */
	private int channelOv;
	/** column in archive */
	private String boxAddress;

	/**
	 * @param archiveAddress
	 *            is address letter of archive
	 * @param channelInfoString
	 *            is of form "CHN"nnn"["<address letter>[",C"[ov]]"]"<br>
	 *            samples:<br>
	 *            CHN001[f] - interval data from archive channel<br>
	 *            "ca<archive address letter>f" will be stored in channel 1<br>
	 *            CHN2[g;C ] interval data from archive channel<br>
	 *            "ca<archive address letter>g" will be stored as advances in<br>
	 *            channel 2, overflow at 999 999 999 (default)<br>
	 *            CHN03[h;C8] interval data from archive channel<br>
	 *            "ca<archive address letter>h" will be stored as advances in<br>
	 *            channel 3, overflow at 99 999 999<br>
	 * @throws Exception
	 */
	public ChannelDefinition(String archiveAddress, String channelInfoString)
			throws Exception {
		this.archiveAddress = archiveAddress;
		channelOv = 9;
		channelType = "I";

		String data = channelInfoString.substring(3);

		/* Split channel number from rest */
		String[] parts = data.split("\\[");
		if (parts.length != 2) {
			throw new Exception("Error in channel definition (" + data + ")");
		}

		try {
			channelNo = Integer.parseInt(parts[0]);
		} catch (Exception e) {
			throw new Exception(
					"Error in channel definition: wrong channel number("
							+ parts[0] + ")");
		}

		/* get content between brackets */
		int end = parts[1].indexOf("]");
		String ai = parts[1].substring(0, end);

		/* check data between brackets */
		parts = ai.split(";");

		/* address is first part... */
		boxAddress = parts[0].toLowerCase();
		if ((boxAddress.length() != 1)
				|| ("fghijklmnopqrstuvwxy".indexOf(boxAddress) < 0)) {
			throw new Exception(
					"Error in channel definition: wrong address letter("
							+ boxAddress + ")");
		}

		/* if second part exists, check */
		if (parts.length > 1) {
			ai = parts[1];
			if (ai.toUpperCase().charAt(0) == 'C') {
				channelType = "C";
				if (ai.length() > 1) {
					try {
						channelOv = Integer.parseInt(ai.substring(1));
					} catch (Exception e) {
						throw new Exception(
								"Error in channel definition: wrong overflow value("
										+ ai.substring(1) + ")");
					}
				}
			}
		}
	}

	/**
	 * Getter for channel no of definition
	 * 
	 * @return channel no
	 */
	public int getChannelNo() {
		return channelNo;
	}

	/**
	 * Getter for channel type (Counter or not)
	 * 
	 * @return "C" if it's a channel for advances, else "I" 
	 */
	public String getChannelType() {
		return channelType;
	}

	/** 
	 * Getter for channel overflow
	 * 
	 * @return overflow value (no. of digits)
	 */
	public int getChannelOv() {
		return channelOv;
	}

	/**
	 * Getter for address letter of box
	 * 
	 * @return address letter
	 */
	public String getChannelAddress() {
		return boxAddress;
	}

	/**
	 * Gets a valid dsfg address for the type of the value
	 * 
	 * @return dsfg address
	 */
	public String getValueTypeAddress() {
		return "ca" + archiveAddress + boxAddress + "a";
	}

	/**
	 * Gets a valid dsfg address for the unit of the value
	 * 
	 * @return dsfg address
	 */
	public String getValueUnitAddress() {
		return "ca" + archiveAddress + boxAddress + "f";
		
	}

	/**
	 * Gets a valid dsfg address for the profile data of the value
	 * 
	 * @return dsfg address
	 */
	public String getValueProfileData() {
		return "ca" + archiveAddress + boxAddress + "d";
		
	}
}
