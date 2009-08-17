/**
 * AS220MessageType.java
 * 
 * Created on 21-nov-2008, 15:12:36 by jme
 * 
 */
package com.energyict.protocolimpl.iec1107.as220;

/**
 * @author jme
 *
 */
public class AS220MessageType {
	private final int length;
	private final int classnr;
	private final String tagName;
	private final String displayName;

	public AS220MessageType(String tagName, int classnr, int length, String displayName) {
		this.tagName = tagName;
		this.classnr = classnr;
		this.length = length;
		this.displayName = displayName;
	}

	public int getLength() {
		return this.length;
	}
	public int getClassnr() {
		return this.classnr;
	}
	public String getTagName() {
		return this.tagName;
	}
	public String getDisplayName() {
		return this.displayName;
	}

}
