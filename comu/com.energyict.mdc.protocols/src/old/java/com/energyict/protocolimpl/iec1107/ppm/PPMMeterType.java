/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.ppm;

/**
 * There are 2 types of PPM meters: - Issue 1 - Issue 2 <p>
 * This class implements a very simple strategy pattern. <p>
 * Issue 1 is the oldest version - no encryption - max 48 integration periods in the Profile <p>
 * Issue 2 newer version - encryption - can have extra integration periods for time changes <p>
 * @author fbo
 */

public class PPMMeterType {

	/** The historical data register contains data for 4 days */
	public final static int NR_HISTORICAL_DATA = 4;
	public static final int NR_TOU_REGISTERS = 8;
	public static final int NR_MD_TOU_REGISTERS = 4;

	static final PPMMeterType ISSUE1 = new PPMMeterType.Issue1("Issue1");
	static final PPMMeterType ISSUE2 = new PPMMeterType.Issue2("Issue2");

	private String name = "";

	/** Creates a new instance of PPMMeterType */
	private PPMMeterType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static class Issue1 extends PPMMeterType {
		Issue1(String name) {
			super(name);
		}
	}

	public static class Issue2 extends PPMMeterType {
		Issue2(String name) {
			super(name);
		}
	}

}