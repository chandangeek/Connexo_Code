/*
 * Class10Status2.java
 *
 * Created on 20 juli 2005, 13:39
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes;

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;


/**
 *
 * @author koen
 */
public class Class10Status2 extends AbstractClass {

	private ClassIdentification classIdentification = new ClassIdentification(10,24,false);

	private int KH;
	private int PR;
	private int PULDEF;
	private long MTRSN;
	private long KEADJ;
	private long KDADJ;
	private int ENEWCON;
	private int ENEWACT;

	@Override
	public String toString() {
		return "Class10Status2: KH="+this.KH+", "+
		"PR="+this.PR+", "+
		"PULDEF=0x"+Integer.toHexString(this.PULDEF)+", "+
		"MTRSN="+this.MTRSN+", "+
		"KEADJ="+this.KEADJ+", "+
		"KDADJ="+this.KDADJ+", "+
		"ENEWCON="+this.ENEWCON+", "+
		"ENEWACT=0x"+Integer.toHexString(this.ENEWACT);
	}

	/** Creates a new instance of Class10Status2 */
	public Class10Status2(ClassFactory classFactory) {
		super(classFactory);
	}

	@Override
	protected void parse(byte[] data) throws IOException {
		this.KH = ProtocolUtils.getBCD2Int(data,0, 3);
		this.PR = ProtocolUtils.getBCD2Int(data,3, 1);
		this.PULDEF = ProtocolUtils.getInt(data,4, 1);
		this.MTRSN = ParseUtils.getBCD2Long(data,5, 5);
		this.KEADJ = ParseUtils.getBCD2Long(data,10, 5);
		this.KDADJ = ParseUtils.getBCD2Long(data,15, 5);
		this.ENEWCON = ProtocolUtils.getBCD2Int(data,20, 3);
		this.ENEWACT = ProtocolUtils.getInt(data,23,1);
	}

	@Override
	protected ClassIdentification getClassIdentification() {
		return this.classIdentification;
	}

}
