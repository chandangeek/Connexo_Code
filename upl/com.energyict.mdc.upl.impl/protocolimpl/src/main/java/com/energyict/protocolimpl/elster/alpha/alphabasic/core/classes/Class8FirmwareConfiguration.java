/*
 * Class8FirmwareConfiguration.java
 *
 * Created on 12 juli 2005, 13:53
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class Class8FirmwareConfiguration extends AbstractClass {

	ClassIdentification classIdentification = new ClassIdentification(8,8,false);

	private int sspec;
	private int group;
	private int revno;
	private int pcode;

	// pprim primary metering constants flag (pcode:15)
	//       1 = primary metering (use class00 and class02 constants)
	//       0 = use predefined meter constants in class07.
	// ptou tou enable flag (pcode:14)
	//       1 = perform tou (checks class04 and 05 and power outage timekeeping)
	//       0 = disable tou checks and power fail (demand metering).
	// pbat battery test flag (pcode:13)
	//       1 = conduct battery test
	//       0 = do not conduct battery test.
	// spare unused (pcode:12); don't care.
	// pfut future configuration switch selector (pcode:11)
	//       1 = future switch enabled (checks class19-23)
	//       0 = future switch disabled.
	// palt alternate quantity definition flag (pcode:10)
	//       1 = alternate input is VAh, 0 = alternate input is VARh.
	// ptoue tou energy definition flag (pcode:9)
	//       1 = tou energy is driven by the alternate input
	//       0 =tou energy is driven by the kWh input.
	// ptoud tou demand definition flag (pcode:8)
	//       1 = tou demand is driven by the alternate input
	//       0 = tou demand is driven by the kWh input.

	static private final int PALT=0x04;
	// pseries device family code (pcode:0-7)
	/*
0 = EMF-2400,
1 = EMF-2500,
2 = EMF-2600,
3 = EMF-2160,
4 = EMF-2460,
5 = EMF-3410,
6 = A1T, A1R, A1K Alpha,
7 = EMF-3110,
8 = A1D Alpha,
9 = "L" Alpha: A1T with Load profile (single channel) option board,
A = "A" Alpha: A1R, A1K with Advanced (multi-quadrant) option board, 18 Alpha Meter Abridged Data Dictionary
B = "AL" Alpha: A1R, A1K with Advanced (multi-quadrant), Load profile (multi-channel) option board.
	 */
	static private final int MAX_METERTYPES=11;
	static private final String[] METERTYPES={"EMF-2400","EMF-2500","EMF-2600","EMF-2160","EMF-2460","EMF-3410","A1T, A1R, A1K Alpha","EMF-3110","A1D Alpha","A1T-L","A1R-A, A1K-A","A1R-AL, A1K-AL"};

	static public final int D_TYPE=0; // A1D
	static public final int K_TYPE=1; // A1K
	static public final int R_TYPE=2; // A1R
	static public final int OTHER_TYPE=3;

	private int xuomhi; // bit 15 .. 8 VAh import, VAh export, Qh import, Qh export, kvarh import, kvarh export, kWh import, kWhexport

	@Override
	public String toString() {
		return "Class8FirmwareConfiguration: SSPEC="+this.sspec+", GROUP=0x"+Integer.toHexString(this.group)+", REVNO="+this.revno+", XUOMHI=0x"+Integer.toHexString(this.xuomhi)+", PCODE="+Integer.toHexString(getPCODE());
	}

	public String getFirmwareVersion() {
		return "SSPEC="+this.sspec+", GROUP=0x"+Integer.toHexString(this.group)+", REVNO="+this.revno+", XUOMHI=0x"+Integer.toHexString(this.xuomhi);
	}

	/** Creates a new instance of Class8FirmwareConfiguration */
	public Class8FirmwareConfiguration(ClassFactory classFactory) {
		super(classFactory);
	}

	@Override
	protected void parse(byte[] data) throws IOException {
		this.sspec = ProtocolUtils.getBCD2Int(data,0, 3);
		this.group = ProtocolUtils.getBCD2Int(data,3, 1);
		this.revno = ProtocolUtils.getInt(data,4, 1);
		setPCODE(ProtocolUtils.getInt(data,5, 2));
		this.xuomhi = ProtocolUtils.getInt(data,7, 1);
	}

	@Override
	protected ClassIdentification getClassIdentification() {
		return this.classIdentification;
	}


	public int getSSPEC() {
		return this.sspec;
	}

	public int getGROUP() {
		return this.group;
	}

	public int getREVNO() {
		return this.revno;
	}

	public int getXUOMHI() {
		return this.xuomhi;
	}

	public String getRegisterFirmware() {
		return ProtocolUtils.buildStringDecimal(getSSPEC(), 6)+" "+ProtocolUtils.buildStringDecimal(getGROUP(),2);
	}
	public String getMeterTypeString() {
		return METERTYPES[getPSERIES()];
	}

	public int getMeterType() {
		if (isDType()) {
			return D_TYPE;
		}
		if (isRType()) {
			return R_TYPE;
		}
		if (isKType()) {
			return K_TYPE;
		} else {
			return OTHER_TYPE;
		}
	}

	public boolean isDType() {
		return getMeterTypeString().indexOf("D")>=0;
	}
	public boolean isKType() {
		return getMeterTypeString().indexOf("K")>=0;
	}
	public boolean isRType() {
		return getMeterTypeString().indexOf("R")>=0;
	}

	public int getPSERIES() {
		return getPCODE()&0xFF;
	}

	public int getPCODEFlags() {
		return getPCODE() >> 8;
	}

	public int getPCODE() {
		return this.pcode;
	}

	/*
	 *   Starting bit = 0 .. 7
	 */
	public boolean isPCODEFlag(int bit) {
		return (getPCODEFlags()&(0x01<<bit)) == (0x01<<bit);
	}

	public boolean isVAAlternateInput() {
		return isPCODEFlag(PALT);
	}
	public boolean isVarAlternateInput() {
		return !isPCODEFlag(PALT);
	}

	public void setPCODE(int PCODE) {
		this.pcode = PCODE;
	}

}
