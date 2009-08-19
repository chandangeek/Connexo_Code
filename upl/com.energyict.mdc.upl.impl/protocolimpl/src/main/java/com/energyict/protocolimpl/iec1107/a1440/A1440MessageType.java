
package com.energyict.protocolimpl.iec1107.a1440;

/**
 * @author jme
 * @since 18-aug-2009
 */
public class A1440MessageType {
	private final int length;
	private final int classnr;
	private final String tagName;
	private final String displayName;

	public A1440MessageType(String tagName, int classnr, int length, String displayName) {
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
