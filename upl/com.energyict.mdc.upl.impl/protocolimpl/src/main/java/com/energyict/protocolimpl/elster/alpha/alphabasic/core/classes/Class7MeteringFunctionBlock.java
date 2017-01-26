/*
 * Class6MeteringFunctionBlock.java
 *
 * Created on 12 juli 2005, 16:00
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes;

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;


/**
 *
 * @author Koen
 */
public class Class7MeteringFunctionBlock extends AbstractClass {

	private ClassIdentification classIdentification = new ClassIdentification(7,288,true);

	private long xmtrsn;
	private BigDecimal xkh;
	private int xpr1;
	private BigDecimal xke1;
	private int xkhdiv;

	@Override
	public String toString() {
		return "Class7MeteringFunctionBlock: XMTRSN="+this.xmtrsn+", XKH="+this.xkh+", XPR1="+this.xpr1+", XKE1="+this.xke1+", XKHDIV="+this.xkhdiv;
	}

	/** Creates a new instance of Class6MeteringFunctionBlock */
	public Class7MeteringFunctionBlock(ClassFactory classFactory) {
		super(classFactory);
	}

	@Override
	protected void parse(byte[] data) throws IOException {
		this.xmtrsn = ParseUtils.getBCD2Long(data,0, 5); //new String(ProtocolUtils.getSubArray2(data, 0, 5));
		if (getClassIdentification().getLength() > 5) {
			this.xkh = BigDecimal.valueOf(ParseUtils.getBCD2Long(data, 5, 3),3);
			this.xpr1 = ProtocolUtils.getBCD2Int(data,8, 1);
			this.xke1 = BigDecimal.valueOf(ParseUtils.getBCD2Long(data, 9, 5),6);
			this.xkhdiv = ProtocolUtils.getInt(data,14, 1);
		}
	}

	@Override
	protected ClassIdentification getClassIdentification() {
		return this.classIdentification;
	}

	public void discoverSerialNumber() {
		getClassIdentification().setLength(5);
		getClassIdentification().setVerify(false);
	}

	public long getXMTRSN() {
		return this.xmtrsn;
	}

	public BigDecimal getXKH() {
		return this.xkh;
	}

	public int getXPR1() {
		return this.xpr1;
	}

	public BigDecimal getXKE1() {
		return this.xke1;
	}

	public int getXKHDIV() {
		return this.xkhdiv;
	}

}
