
package com.energyict.protocolimpl.iec1107.as220;

/**
 * @author jme
 * @since 18-aug-2009
 */
public class AS220MessageType {

	private final int length;
	private final int classnr;
	private final String tagName;
	private final String displayName;

	/**
	 * Constructor for a AS220MessageType object
	 * @param tagName The name of the message, this is used as tag in the message XML
	 * @param classnr The class number in the device, where the message content should be written
	 * @param length The length of data expected in the class
	 * @param displayName A user readable name for the message, shown in the EiServer messages menu
	 */
	public AS220MessageType(String tagName, int classnr, int length, String displayName) {
		this.tagName = tagName;
		this.classnr = classnr;
		this.length = length;
		this.displayName = displayName;
	}

	/**
	 * The length of data expected in the meter class
	 * @return The length of data
	 */
	public int getLength() {
		return this.length;
	}

	/**
	 * The class number in the device, where the message content should be written
	 * @return The class number
	 */
	public int getClassnr() {
		return this.classnr;
	}

	/**
	 * The name of the message, this is used as tag in the message XML
	 * @return The name of the message
	 */
	public String getTagName() {
		return this.tagName;
	}

	/**
	 * A user readable name for the message, shown in the EiServer messages menu
	 * @return Message display name
	 */
	public String getDisplayName() {
		return this.displayName;
	}

}
