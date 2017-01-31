/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * ABBA1350MessageType.java
 * 
 * Created on 21-nov-2008, 15:12:36 by jme
 * 
 */
package com.energyict.protocolimpl.iec1107.abba1350;

/**
 * @author jme
 *
 */
public class ABBA1350MessageType {
	private final int length;
	private final int classnr;
	private final String tagName;
	private final String displayName;
	
	public ABBA1350MessageType(String tagName, int classnr, int length, String displayName) {
		this.tagName = tagName;
		this.classnr = classnr;
		this.length = length;
		this.displayName = displayName;
	}

	public int getLength() {
		return length;
	}
	public int getClassnr() {
		return classnr;
	}
	public String getTagName() {
		return tagName;
	}
	public String getDisplayName() {
		return displayName;
	}

}
