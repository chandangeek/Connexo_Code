/*
 * Class14LoadProfileConfiguration.java
 *
 * Created on 13 juli 2005, 16:58
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes;

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class Class14LoadProfileConfiguration extends AbstractClass {

	private ClassIdentification classIdentification = new ClassIdentification(14,36,true);

	private static final int MAX_PROFILE_CHANNELS=4;
	private static final int MAX_BLOCKS=8;

	private int[] TBLKCF = new int[MAX_BLOCKS];

	// d1 (tblkcf1:7); 1 = accumulate kW delivered pulses in this TOU block, 0 = do not accumulate kw delivered pulses in this TOU block.
	// r1 (tblkcf1:6); 1 = accumulate kW received pulses in this TOU block, 0 = do not accumulate kw received pulses in this TOU block.
	// d2 (tblkcf1:5); for A1K meter, 1 = accumulate kVA delivered pulses in this block,
	//                 for A1R meter 1 = accumulate kVAR delivered pulses in this block,
	//                 0 = do not accumulate kVAR or kVA delivered pulses in this TOU block.
	// r2 (tblkcf1:4); for A1K meter, 1 = accumulate kVA pulses received in this block;
	//                 for A1R meter, 1 = accumulate kVAR received pulses in this TOU block;
	//                 0 = do not accumulate kVA or kVAR received pulses in this TOU block.
	// q4 (tblkcf1:3); 1 = accumulate quadrant 4 kVAR pulses in this TOU block, 0 = do not accumulate quadrant 4 kVAR pulses in this TOU block.
	// q3 (tblkcf1:2); 1 = accumulate quadrant 3 kVAR pulses in this TOU block, 0 = do not accumulate quadrant 3 kVAR pulses in this TOU block.
	// q2 (tblkcf1:1); 1 = accumulate quadrant 2 kVAR pulses in this TOU block, 0 = do not accumulate quadrant 2 kVAR pulses in this TOU block.
	// q1 (tblkcf1:0); 1 = accumulate quadrant 1 kVAR pulses in this TOU block, 0 = do not accumulate quadrant 1 kVAR pulses in this TOU block.
	// note:q1, q2, q3, and q4 must be 0 for an A1K meter.

	// kWh delivered 0x80
	// kWh received 0x40
	// w Quadrant 1 VARh (set bit 0)
	// w Quadrant 2 VARh (set bit 1)
	// w Quadrant 3 VARh (set bit 2)
	// w Quadrant 4 VARh (set bit 3)
	// The quadrant bits can also be combined. This is also how delivered or received VARh is selected:
	// w Delivered VARh = Quadrant 1 & 2 VARh (set bits 0 & 1) 0x03
	// w Received VARh = Quadrant 3 & 4 VARh (set bits 2 & 3)  0x0C

	// Vectorial kVA (A1R only)
	// kVA delivered based on kVARh, Q1 & Q4 $89
	// kVA delivered based on kVARh, Q1 $81
	// kVA delivered based on kVARh, Q4 $88
	// kVA received based on kVARh, Q2 & Q3 $46
	// kVA received based on kVARh, Q2 $42
	// kVA received based on kVARh, Q3 $44

	// Vectorial kVAR (A1K only)
	// kVAR TOU Block EBLCKF1
	// kVAR delivered $83
	// kVAR received $4C

	private int DTYFLG; // 1 byte demand type flags
	private int LCFLG; // 1 byte load control and eoi flags
	private int KYZDIV; // 1 byte kyz output divider
	// spares 4 (set to zero)
	private int LPMEM; // 2 bytes load profile memory size (words)
	private int LPOUT; // 1 byte load profile outage threshold
	private int LPLEN; // 1 byte load profile interval length
	private int[] INPUTQUANTITIES = new int[MAX_PROFILE_CHANNELS]; // load profile channels
	//00 = disable channel
	//01 = record real power delivered = kWh Import
	//02 = record real power received = kWh Export
	//03 = record reactive power delivered = kvar
	//04 = record reactive power received
	//05 = record reactive power for quadrant 4
	//06 = record reactive power for quadrant 3
	//07 = record reactive power for quadrant 2
	//08 = record reactive power for quadrant 1
	//10 = record TOU block 1 data
	//20 = record TOU block 2 data
	//30 = record TOU block 3 data
	//40 = record TOU block 4 data
	//50 = record TOU block 5 data
	//60 = record TOU block 6 data .

	private int LPSCALE; // 1 byte load profile scaling factor
	private int EXSCALE; // 1 byte reserved for future, set to 0
	// spares 6 set to zero

	@Override
	public String toString() {

		if ((this.LPLEN+this.LPMEM)==0) {
			return "NO Profile data configured in the meter!";
		}

		StringBuffer strBuff = new StringBuffer();
		strBuff.append("Class14LoadProfileConfiguration: DTYFLG="+this.DTYFLG);
		strBuff.append(", LPLEN="+this.LPLEN);
		strBuff.append(", LPOUT="+this.LPOUT);
		strBuff.append(", LPMEM="+this.LPMEM);
		strBuff.append(", LCFLG="+this.LCFLG);
		for (int i=0;i<MAX_PROFILE_CHANNELS;i++) {
			strBuff.append(", INPUTQUANTITIES["+i+"]="+this.INPUTQUANTITIES[i]);
		}
		strBuff.append(", LPSCALE="+this.LPSCALE);

		for (int i=0;i<MAX_BLOCKS;i++) {
			strBuff.append(", TBLKCF["+i+"]=0x"+Integer.toHexString(this.TBLKCF[i]));
		}

		try {
			strBuff.append("\ngetDayRecordSize()="+getDayRecordSize());
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return strBuff.toString();
	}

	/** Creates a new instance of Class14LoadProfileConfiguration */
	public Class14LoadProfileConfiguration(ClassFactory classFactory) {
		super(classFactory);
	}


	/*
	 *  @result 1 = import, 0 = export
	 */
	public int getDirection(int block) throws IOException {
		if (((getTBLKCF(block) >> 6) & 0x03) == 0x1) {
			return 0;
		} else if (((getTBLKCF(block) >> 6) & 0x03) == 0x2) {
			return 1;
		} else {
			throw new IOException("CLASS14, getDirection(), delivered AND receved are both active? (TBLKCF["+block+"]=0x"+Integer.toHexString(getTBLKCF(block)));
		}
	}

	public int getTBLKCF(int block) {
		return this.TBLKCF[block];
	}

	public boolean isTOUBlockEnabled(int block) {
		return getTBLKCF(block) != 0;
	}

	public Unit getTOUUnit(int block, boolean energy) throws IOException {
		return TOUConfig.getTOUUnit(getTBLKCF(block),getClassFactory().getClass8FirmwareConfiguration().getMeterType(),energy);
	}
	public int getTOUObisCField(int block) throws IOException {
		return TOUConfig.getTOUObisCField(getTBLKCF(block),getClassFactory().getClass8FirmwareConfiguration().getMeterType());
	}
	public String getTOUDescription(int block) throws IOException {
		return TOUConfig.getTOUDescription(getTBLKCF(block),getClassFactory().getClass8FirmwareConfiguration().getMeterType());
	}


	//    public int getQuadrantInfo(int block) throws IOException {
	//        return (getTBLKCF(block) & 0x0F);
	//    }
	//
	//
	//    public boolean isVAImport(int block) throws IOException {
	//        return getQuadrantInfo(block)==0x9;
	//    }
	//    public boolean isVAExport(int block) throws IOException {
	//        return getQuadrantInfo(block)==0x6;
	//    }
	//    public boolean isVASum(int block) throws IOException {
	//        return getQuadrantInfo(block)==0xF;
	//    }
	//    public boolean isvarImport(int block) throws IOException {
	//        return getQuadrantInfo(block)==0x3;
	//    }
	//    public boolean isvarExport(int block) throws IOException {
	//        return getQuadrantInfo(block)==0xC;
	//    }
	//    public boolean isvarSum(int block) throws IOException {
	//        return getQuadrantInfo(block)==0xF;
	//    }
	//    public boolean isOnlyQ1(int block) throws IOException {
	//        return getQuadrantInfo(block)==0x1;
	//    }
	//    public boolean isOnlyQ2(int block) throws IOException {
	//        return getQuadrantInfo(block)==0x2;
	//    }
	//    public boolean isOnlyQ3(int block) throws IOException {
	//        return getQuadrantInfo(block)==0x4;
	//    }
	//    public boolean isOnlyQ4(int block) throws IOException {
	//        return getQuadrantInfo(block)==0x8;
	//    }
	//    public boolean isOnlyQ1Q4(int block) throws IOException {
	//        return getQuadrantInfo(block)==0x9;
	//    }



	@Override
	protected void parse(byte[] data) throws IOException {
		for (int block=0; block<MAX_BLOCKS; block++) {
			this.TBLKCF[block] = ProtocolUtils.getInt(data,block,1);
		}
		setDTYFLG(ProtocolUtils.getInt(data,8,1));
		setLCFLG(ProtocolUtils.getInt(data,9,1));
		setKYZDIV(ProtocolUtils.getInt(data,10,1));
		// spares 4 (set to zero)
		setLPMEM(ProtocolUtils.getInt(data,15,2));
		setLPOUT(ProtocolUtils.getInt(data,17,1));
		setLPLEN(ProtocolUtils.getInt(data,18,1));

		for (int i=0;i<MAX_PROFILE_CHANNELS;i++) {
			this.INPUTQUANTITIES[i] = ProtocolUtils.getInt(data,19+i,1);
		}

		setLPSCALE(ProtocolUtils.getInt(data,27,1));
		setEXSCALE(ProtocolUtils.getInt(data,28,1));

	}

	@Override
	protected ClassIdentification getClassIdentification() {
		return this.classIdentification;
	}


	/*************************************************************************************************
	 * Derived methods to calculate profile specific parameters
	 *************************************************************************************************/
	/*
	 *  @result profile interval in seconds
	 */
	public int getLoadProfileInterval() {
		return getLPLEN()*60;
	}
	//    /*
	//     *  @result nr of configured profile data channels
	//     *  @throws IOException if nr of configured channels > MAX_PROFILE_CHANNELS
	//     */
	public int getNrOfChannels() throws IOException {
		int nrOfChannels = 0;
		for (int i=0;i<MAX_PROFILE_CHANNELS;i++) {
			if (this.INPUTQUANTITIES[i] != 0) {
				nrOfChannels++;
			}
		}
		return nrOfChannels;
	}
	//    /*
	//     *  @throws IOException if calculated daylength does not match with the daylength within the class
	//     *  @result day length in bytes...
	//     */
	//    public int getDayRecordSize() throws IOException {
	//        int dalen = ((3600 / getLoadProfileInterval())*24)*2*getCHANS() + DAY_HEADER_SIZE;
	//        if (dalen != getDASIZE())
	//            throw new IOException("Class14LoadProfileConfiguration, getDayRecordSize(), class field DASIZE ("+getDASIZE()+") does not mach with calculated length ("+dalen+")");
	//        return dalen;
	//    }

	public int getIntervalsPerDay() {
		return 24 * (3600/getLoadProfileInterval());
	}
	public int getIntervalsPerHour() {
		return (3600/getLoadProfileInterval());
	}

	public int[] getINPUTQUANTITIES() {
		return this.INPUTQUANTITIES;
	}

	public int getINPUTQUANTITY(int channel) {
		return this.INPUTQUANTITIES[channel];
	}


	public int getDayRecordSize() throws IOException {
		int dalen = ((3600 / getLoadProfileInterval())*24)*2*getNrOfChannels();
		return dalen;
	}

	public Unit getUnit(int profileChannel) throws IOException {
		int channelCount=0;
		for (int meterChannel=0;meterChannel<MAX_PROFILE_CHANNELS;meterChannel++) {
			if (getINPUTQUANTITY(meterChannel) > 0) {
				if (channelCount==profileChannel) {
					return getInputQuantityUnit(getINPUTQUANTITY(meterChannel));
				}
				channelCount++;
			}
		}
		throw new IOException("Class14LoadProfileConfiguration, getUnit, invalid profileChannel "+profileChannel);
	} // public Unit getUnit(int profileChannel) throws IOException

	/*
	 *   @result The meter's channel index.
	 *           E.g. 2 active channels, channel 0 and 3 enabled, means, profile data channels 0 and 1 have respectively
	 *                meterchannelindex 0 and 3
	 */
	public int getMeterChannelIndex(int profileChannel) throws IOException {
		int channelCount=0;
		for (int meterChannel=0;meterChannel<MAX_PROFILE_CHANNELS;meterChannel++) {
			if (getINPUTQUANTITY(meterChannel) >0) {
				if (channelCount==profileChannel) {
					return meterChannel;
				}
				channelCount++;
			}
		}
		throw new IOException("Class14LoadProfileConfiguration, getUnit, invalid profileChannel "+profileChannel);
	} // public int getMeterChannelIndex(int profileChannel)

	private Unit getInputQuantityUnit(int inputQuantity) throws IOException {
		if ((inputQuantity == 1) || (inputQuantity == 2)) {
			return Unit.get("kW");
		} else if ((inputQuantity >= 3) && (inputQuantity <= 4)) {
			if (this.getClassFactory().getClass8FirmwareConfiguration().isVAAlternateInput()) {
				return Unit.get("kVA");
			} else if (this.getClassFactory().getClass8FirmwareConfiguration().isVarAlternateInput()) {
				return Unit.get("kvar");
			} else {
				throw new IOException("Class14LoadProfileConfiguration, getInputQuantityUnit(), Wrong inputQuantity "+inputQuantity+" for metertype "+this.getClassFactory().getClass8FirmwareConfiguration().getMeterType());
			}
		}
		else if ((inputQuantity >= 5) && (inputQuantity <= 8)) {
			if (this.getClassFactory().getClass8FirmwareConfiguration().isVarAlternateInput()) {
				return Unit.get("kvar");
			} else {
				throw new IOException("Class14LoadProfileConfiguration, getInputQuantityUnit(), Wrong inputQuantity "+inputQuantity+" for metertype "+this.getClassFactory().getClass8FirmwareConfiguration().getMeterType());
			}
		}
		else if ((inputQuantity >= 10) && (inputQuantity <= 60)) {
			int touBlock = (inputQuantity/10) - 1;
			return getClassFactory().getClass14LoadProfileConfiguration().getTOUUnit(touBlock,true);
		} else {
			throw new IOException("Class14LoadProfileConfiguration, getInputQuantityUnit, invalid inputQuantity "+inputQuantity);
		}
	}

	//    public int getPhenomenon(int profileChannel) throws IOException {
	//       int channelCount=0;
	//       for (int meterChannel=0;meterChannel<MAX_PROFILE_CHANNELS;meterChannel++) {
	//           if (getINPUTQUANTITY(meterChannel) > 0) {
	//               if (channelCount==profileChannel) {
	//                   return getInputQuantityPhenomenon(getINPUTQUANTITY(meterChannel));
	//               }
	//               channelCount++;
	//           }
	//       }
	//       throw new IOException("Class14LoadProfileConfiguration, getPhenomenon, invalid profileChannel "+profileChannel);
	//    } // public Unit getPhenomenon(int profileChannel) throws IOException

	//    private int getInputQuantityPhenomenon(int inputQuantity) throws IOException {
	//        if ((inputQuantity == 1) || (inputQuantity == 2))
	//            return Class8FirmwareConfiguration.PHENOMENON_ACTIVE;
	//        else if ((inputQuantity >= 3) && (inputQuantity <= 4)) {
	//            if (classFactory.getClass8FirmwareConfiguration().isVAAlternateInput())
	//                return Class8FirmwareConfiguration.PHENOMENON_APPARENT;
	//            else if (classFactory.getClass8FirmwareConfiguration().isVarAlternateInput())
	//                return Class8FirmwareConfiguration.PHENOMENON_REACTIVE;
	//            else
	//                throw new IOException("Class14LoadProfileConfiguration, getInputQuantityPhenomenon(), Wrong inputQuantity "+inputQuantity+" for metertype "+classFactory.getClass8FirmwareConfiguration().getMeterType());
	//        }
	//        else if ((inputQuantity >= 5) && (inputQuantity <= 8)) {
	//            if (classFactory.getClass8FirmwareConfiguration().isVarAlternateInput())
	//                return Class8FirmwareConfiguration.PHENOMENON_REACTIVE;
	//            else
	//                throw new IOException("Class14LoadProfileConfiguration, getInputQuantityPhenomenon(), Wrong inputQuantity "+inputQuantity+" for metertype "+classFactory.getClass8FirmwareConfiguration().getMeterType());
	//        }
	////        else if (inputQuantity == 9) // KV_TO_DO??
	////            return classFactory.getClass8FirmwareConfiguration().getBlockPhenomenon(0);
	////        else if (inputQuantity == 10) // KV_TO_DO??
	////            return classFactory.getClass8FirmwareConfiguration().getBlockPhenomenon(1);
	//        else throw new IOException("Class14LoadProfileConfiguration, getInputQuantityPhenomenon(), invalid inputQuantity "+inputQuantity);
	//    } // private int getInputQuantityPhenomenon(int inputQuantity) throws IOException

	public int getDTYFLG() {
		return this.DTYFLG;
	}

	public void setDTYFLG(int DTYFLG) {
		this.DTYFLG = DTYFLG;
	}

	public int getLCFLG() {
		return this.LCFLG;
	}

	public void setLCFLG(int LCFLG) {
		this.LCFLG = LCFLG;
	}

	public int getKYZDIV() {
		return this.KYZDIV;
	}

	public void setKYZDIV(int KYZDIV) {
		this.KYZDIV = KYZDIV;
	}

	public int getLPMEM() {
		return this.LPMEM;
	}

	public void setLPMEM(int LPMEM) {
		this.LPMEM = LPMEM;
	}

	public int getLPOUT() {
		return this.LPOUT;
	}

	public void setLPOUT(int LPOUT) {
		this.LPOUT = LPOUT;
	}

	public int getLPLEN() {
		return this.LPLEN;
	}

	public void setLPLEN(int LPLEN) {
		this.LPLEN = LPLEN;
	}


	public int getLPSCALE() {
		return this.LPSCALE;
	}

	public void setLPSCALE(int LPSCALE) {
		this.LPSCALE = LPSCALE;
	}

	public int getEXSCALE() {
		return this.EXSCALE;
	}

	public void setEXSCALE(int EXSCALE) {
		this.EXSCALE = EXSCALE;
	}
}
