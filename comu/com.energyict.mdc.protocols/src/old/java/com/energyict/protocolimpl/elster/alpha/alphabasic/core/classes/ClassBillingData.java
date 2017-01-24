/*
 * ClassBillingData.java
 *
 * Created on 13 juli 2005, 14:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes;

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.elster.alpha.core.classes.ClassParseUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author Koen
 */
abstract public class ClassBillingData extends AbstractClass {

	@Override
	abstract protected ClassIdentification getClassIdentification();

	// rates 0..3, 4 rates = rate A, rate B, rate C, rate D
	// TOU blocks 0..1,

	private static final int MAX_TOU_BLOCKS=6;
	private static final int MAX_COIN_BLOCKS=2;
	private static final int MAX_RATES=4;

	/*
	 *  The KWH used in the attibutes below IS NOT Active energy. It is howezver energy (KWH) and power (KW) but the
	 *  phenomenon must be determined by the CLASS 14 TOU block configuration fields! The naming is taken from the CLASS 11
	 *  description in the Alpha meter communication protocol for meter reading May 22, 1997, revision 1.61
	 */
	BigDecimal[] KWHtotal = new BigDecimal[MAX_TOU_BLOCKS];
	BigDecimal[][] KWH = new BigDecimal[MAX_TOU_BLOCKS][MAX_RATES]; // total kwh (non TOU) or rate A kwh for TOU block 1
	BigDecimal[][] KW = new BigDecimal[MAX_TOU_BLOCKS][MAX_RATES]; // maximum demand (non TOU) or demand for rate A in TOU block 1
	BigDecimal[][] KWCUM = new BigDecimal[MAX_TOU_BLOCKS][MAX_RATES]; // cumulative demand (non TOU) or rate A cumulative demand for TOU block 1
	Date[][] TD = new Date[MAX_TOU_BLOCKS][MAX_RATES]; // date & time of the max demand
	// 3 BYTES SPARE
	BigDecimal[] PFAverage = new BigDecimal[MAX_COIN_BLOCKS]; // average power factor based on kVA and kWH defined by this block since last demand reset (except for configurations where KVA is not available in which the first 3 bytes should be ignored)    BigDecimal[][] PF = new BigDecimal[MAX_TOU_BLOCKS][MAX_RATES]; // power factor for rate A trigger interval
	BigDecimal[][] PF = new BigDecimal[MAX_COIN_BLOCKS][MAX_RATES]; // powerfactor for rate A trigger interval
	BigDecimal[][] AK = new BigDecimal[MAX_COIN_BLOCKS][MAX_RATES]; // demand at rate A trigger interval
	// 3 BYTES SPARE
	Date[][] TDTR = new Date[MAX_COIN_BLOCKS][MAX_RATES]; // trigger interval

	@Override
	public String toString() {
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("Class "+getClassIdentification().getId()+" (billing data): \n");
		for (int block=0; block<MAX_TOU_BLOCKS;block++) {
			for (int rate=0; rate<MAX_RATES;rate++) {
				strBuff.append("KWH["+block+"]["+rate+"]="+this.KWH[block][rate]+
						", KW["+block+"]["+rate+"]="+this.KW[block][rate]+
						", TD["+block+"]["+rate+"]="+this.TD[block][rate]+
						", KWCUM["+block+"]["+rate+"]="+this.KWCUM[block][rate]+"\n");
			} // for (int rate=0; rate<MAX_RATES;rate++)
			strBuff.append("KWHtotal["+block+"]="+this.KWHtotal[block]+"\n");
		}

		for (int block=0; block<MAX_COIN_BLOCKS;block++) {
			for (int rate=0; rate<MAX_RATES;rate++) {
				strBuff.append("PF["+block+"]["+rate+"]="+this.PF[block][rate]+
						", AK["+block+"]["+rate+"]="+this.AK[block][rate]+
						", TDTR["+block+"]["+rate+"]="+this.TDTR[block][rate]+"\n");
			} // for (int rate=0; rate<MAX_RATES;rate++)
			strBuff.append("PFAverage["+block+"]="+this.PFAverage[block]+"\n");
		}

		return strBuff.toString();
	}

	/** Creates a new instance of Class11BillingData */
	public ClassBillingData(ClassFactory classFactory) {
		super(classFactory);
	}

	@Override
	protected void parse(byte[] data) throws IOException {
		int offset = 0;
		for (int block=0; block<MAX_TOU_BLOCKS;block++) {
			this.KWHtotal[block] = getValue(data,offset,3,true);offset+=3;
			for (int rate=0; rate<MAX_RATES;rate++) {
				this.KWH[block][rate] = getValue(data,offset,3,true);offset+=3;
				this.KW[block][rate] = getValue(data,offset,3,false);offset+=3;
				this.KWCUM[block][rate] = getValue(data,offset,3,false);offset+=3;
				this.TD[block][rate] = ClassParseUtils.getDate5(data,offset, this.getClassFactory().getAlpha().getTimeZone());offset+=5;
			} // for (int rate=0; rate<MAX_RATES;rate++)
			offset+=3; // skip 3 bytes
		} // for (int block=0; block<MAX_TOU_BLOCKS;block++)

		for (int block=0; block<MAX_COIN_BLOCKS;block++) {
			this.PFAverage[block] = BigDecimal.valueOf(ParseUtils.getBCD2Long(data,offset,3));offset+=3;
			for (int rate=0; rate<MAX_RATES;rate++) {
				this.PF[block][rate] = BigDecimal.valueOf(ParseUtils.getBCD2Long(data,offset,3));offset+=3;
				this.AK[block][rate] = getValue(data,offset,3,false);offset+=3;
				offset+=3; // skip 3 bytes
				this.TDTR[block][rate] = ClassParseUtils.getDate5(data,offset, this.getClassFactory().getAlpha().getTimeZone());offset+=5;
			} // for (int rate=0; rate<MAX_RATES;rate++)
			offset+=3; // skip 3 bytes
		} // for (int block=0; block<MAX_TOU_BLOCKS;block++)


	} // protected void parse(byte[] data) throws IOException

	private BigDecimal getValue(byte[] data, int offset, int length, boolean energy) throws IOException {
		int decimalPoint = energy ? this.getClassFactory().getClass0ComputationalConfiguration().getDPLOCE():this.getClassFactory().getClass0ComputationalConfiguration().getDPLOCD();
		return BigDecimal.valueOf(ParseUtils.getBCD2Long(data,offset,length),decimalPoint);
	}

	// TOU BLOCK entries
	public BigDecimal getKWHtotal(int block) {
		return this.KWHtotal[block];
	}
	public BigDecimal getKWH(int block, int rate) {
		return this.KWH[block][rate];
	}
	public BigDecimal getKW(int block, int rate) {
		return this.KW[block][rate];
	}
	public Date getTD(int block, int rate) {
		return this.TD[block][rate];
	}
	public BigDecimal getKWCUM(int block, int rate) {
		return this.KWCUM[block][rate];
	}

	// COINCIDENT BLOCK entries
	public BigDecimal getPFAverage(int block) {
		return this.PFAverage[block];
	}
	public BigDecimal getPF(int block, int rate) {
		return this.PF[block][rate];
	}
	public BigDecimal getAK(int block, int rate) {
		return this.AK[block][rate];
	}
	public Date getTDTR(int block, int rate) {
		return this.TDTR[block][rate];
	}


} // abstract public class ClassBillingData extends AbstractClass
