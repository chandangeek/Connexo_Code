/*
 * ObisCodeInfoFactory.java
 *
 * Created on 16 november 2005, 16:14
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3;

import com.energyict.mdc.io.NestedIOException;
import com.energyict.mdc.upl.NoSuchRegisterException;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.ansi.c12.tables.ActualRegisterTable;
import com.energyict.protocolimpl.ansi.c12.tables.DataBlock;
import com.energyict.protocolimpl.ansi.c12.tables.ElectricConstants;
import com.energyict.protocolimpl.ansi.c12.tables.RegisterData;
import com.energyict.protocolimpl.ansi.c12.tables.RegisterInf;
import com.energyict.protocolimpl.ansi.c12.tables.StandardTableFactory;
import com.energyict.protocolimpl.elster.a1800.tables.ABBInstrumentConstants;
import com.energyict.protocolimpl.elster.a1800.tables.ActualService;
import com.energyict.protocolimpl.elster.a1800.tables.DSPRawInstrumentationCache;
import com.energyict.protocolimpl.elster.a3.tables.ObisCodeDescriptor;
import com.energyict.protocolimpl.elster.a3.tables.SourceDefinitionTable;
import com.energyict.protocolimpl.elster.a3.tables.SourceInfo;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
/**
 *
 * @author Koen
 */
public class ObisCodeInfoFactory {

	public static int DEBUG = 0;

	List<ObisCodeInfo> obisCodeInfos;
	AlphaA3 alphaA3;

	/** Creates a new instance of ObisCodeInfoFactory */
	public ObisCodeInfoFactory(AlphaA3 alphaA3) throws IOException {
		this.alphaA3=alphaA3;
		buildObisCodeInfos();
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		Iterator<ObisCodeInfo> it = obisCodeInfos.iterator();
		while (it.hasNext()) {
			builder.append(it.next()).append("\n");
		}
		return builder.toString();
	}

	public static final int CURRENT=255;
	public static final int PREVIOUS_SEASON=0;
	public static final int PREVIOUS_DEMAND_RESET=1;
	public static final int SELF_READ_OFFSET=254;

	public void buildObisCodeInfos() throws IOException {
		obisCodeInfos=new ArrayList<>();
		ActualRegisterTable art = alphaA3.getStandardTableFactory().getActualRegisterTable();

		// current registers
		if (alphaA3.getStandardTableFactory().getConfigurationTable().isStdTableUsed(StandardTableFactory.CURRENT_REGISTER_DATA_TABLE)) {
			buildRegisterObisCodeInfos(CURRENT);
		}
		// previous season registers
		if (alphaA3.getStandardTableFactory().getConfigurationTable().isStdTableUsed(StandardTableFactory.PREVIOUS_SEASON_DATA_TABLE)) {
			buildRegisterObisCodeInfos(PREVIOUS_SEASON);
		}
		// previous demand reset registers
		if (alphaA3.getStandardTableFactory().getConfigurationTable().isStdTableUsed(StandardTableFactory.PREVIOUS_DEMAND_RESET_DATA_TABLE)) {
			buildRegisterObisCodeInfos(PREVIOUS_DEMAND_RESET);
		}
		// self read registers
		if (alphaA3.getStandardTableFactory().getConfigurationTable().isStdTableUsed(StandardTableFactory.SELF_READ_DATA_TABLE)) {
			for (int index = 0; index < art.getNrOfSelfReads(); index++) {
				buildRegisterObisCodeInfos(SELF_READ_OFFSET - index);
			}
		}
	}


	public static final int CONT_CUMULATIVE_DEMAND=128;
	public static final int COIN_DEMAND=129; // 129..138

	private void buildRegisterObisCodeInfos(int fField) throws IOException {
		SourceInfo si = new SourceInfo(alphaA3);
		ActualRegisterTable art = alphaA3.getStandardTableFactory().getActualRegisterTable();
		String registerSetInfo;

		switch(fField) {
		case CURRENT: {
			registerSetInfo = "current, ";
		} break;
		case PREVIOUS_SEASON: {
			registerSetInfo = "previous season, ";
		} break;
		case PREVIOUS_DEMAND_RESET: {
			registerSetInfo = "previous demand reset, ";
		} break;
		default: { // SELF_READ_OFFSET
			registerSetInfo = "self read, ";
		} break;
		}

		if (fField == CURRENT) {
            int[] presentValueSelect = alphaA3.getStandardTableFactory().getPresentRegisterSelectionTable().getPresentValueSelect();
            for (int index = 0; index < art.getNrOfPresentValues(); index++) {
                int dataControlEntryIndex = presentValueSelect[index];
                if (dataControlEntryIndex != 255) {
                    ObisCodeDescriptor obisCodeDescriptor;
                    try {
                        obisCodeDescriptor = si.getObisCodeDescriptor(dataControlEntryIndex);
                    } catch (IOException e) {
                        if ("ReadResponse, parse, checksum failure in table data!".equalsIgnoreCase(e.getMessage())) {
                            throw new NestedIOException(e); // In this case, it is not useful to continue, as we will run into other exceptions...
                        } else if (e.getMessage().contains("EAXPrimeEncoder - Failed to decrypt the frame")) {
                            throw new NestedIOException(e);
                        } else {
                            continue;
                        }
                    }
                    if (obisCodeDescriptor != null) {
                        try {
                            si.getUnit(dataControlEntryIndex);
                        } catch (IOException e) {
                            continue;
                        }
                        obisCodeInfos.add(new ObisCodeInfo(new ObisCode(1, obisCodeDescriptor.getBField(), obisCodeDescriptor.getCField(), obisCodeDescriptor.getCurrentDField(), obisCodeDescriptor.getCurrentEField(), fField), registerSetInfo + " present value register index " + index + ", " + obisCodeDescriptor.getDescription(), si.getUnit(dataControlEntryIndex), index, dataControlEntryIndex));
                    }
                }
            }
        }
		for(int tier=0;tier<=art.getNrOfTiers();tier++) {
			for(int index=0;index<art.getNrOfSummations();index++) {
				int dataControlEntryIndex = alphaA3.getStandardTableFactory().getDataSelectionTable().getSummationSelects()[index];
				if (dataControlEntryIndex != 255) {
					ObisCodeDescriptor obisCodeDescriptor = si.getObisCodeDescriptor(dataControlEntryIndex);
					if (obisCodeDescriptor != null) {
						obisCodeInfos.add(new ObisCodeInfo(new ObisCode(1,obisCodeDescriptor.getBField(),obisCodeDescriptor.getCField(),ObisCode.CODE_D_TIME_INTEGRAL,tier,fField),registerSetInfo+"summation register index "+index+", "+obisCodeDescriptor.getDescription(),si.getUnit(dataControlEntryIndex).getVolumeUnit(),index,dataControlEntryIndex));
					}
				}
			}

			for(int index=0;index<art.getNrOfDemands();index++) {
				int dataControlEntryIndex = alphaA3.getStandardTableFactory().getDataSelectionTable().getDemandSelects()[index];
				if (dataControlEntryIndex != 255) {
					ObisCodeDescriptor obisCodeDescriptor = si.getObisCodeDescriptor(dataControlEntryIndex);
					if (obisCodeDescriptor != null) {
						obisCodeInfos.add(new ObisCodeInfo(new ObisCode(1,obisCodeDescriptor.getBField(),obisCodeDescriptor.getCField(),ObisCode.CODE_D_MAXIMUM_DEMAND,tier,fField),registerSetInfo+"max/min demand register index "+index+", "+obisCodeDescriptor.getDescription(),si.getUnit(dataControlEntryIndex).getFlowUnit(),index,dataControlEntryIndex));
						if (art.isCumulativeDemandFlag()) {
							obisCodeInfos.add(new ObisCodeInfo(new ObisCode(1,obisCodeDescriptor.getBField(),obisCodeDescriptor.getCField(),ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND,tier,fField),registerSetInfo+"cumulative demand register index "+index+", "+obisCodeDescriptor.getDescription(),si.getUnit(dataControlEntryIndex).getFlowUnit(),index,dataControlEntryIndex));
						}
						if (art.isContinueCumulativeDemandFlag()) {
							obisCodeInfos.add(new ObisCodeInfo(new ObisCode(1,obisCodeDescriptor.getBField(),obisCodeDescriptor.getCField(),CONT_CUMULATIVE_DEMAND,tier,fField),registerSetInfo+"continue cumulative demand register index "+index+", "+obisCodeDescriptor.getDescription(),si.getUnit(dataControlEntryIndex).getFlowUnit(),index,dataControlEntryIndex));
						}
					}
				}
			}

			for(int index=0;index<art.getNrOfCoinValues();index++) {

				int dataControlEntryIndex = alphaA3.getStandardTableFactory().getDataSelectionTable().getCoincidentSelects()[index];
				if (dataControlEntryIndex != 255) {
					ObisCodeDescriptor obisCodeDescriptor = si.getObisCodeDescriptor(dataControlEntryIndex);
					if (obisCodeDescriptor != null) {
						obisCodeInfos.add(new ObisCodeInfo(new ObisCode(1,obisCodeDescriptor.getBField(),obisCodeDescriptor.getCField(),COIN_DEMAND+index,tier,fField),registerSetInfo+"coincident demand register index "+index+", "+obisCodeDescriptor.getDescription(),si.getUnit(dataControlEntryIndex).getFlowUnit(),index,dataControlEntryIndex));
					}
				}
			}
		}
	}

	public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
		ObisCodeInfo obi = findObisCodeInfo(obisCode);
		return new RegisterInfo(obi.getDescription());
	}

	public RegisterValue getRegister(ObisCode obisCode) throws IOException {

		ObisCodeInfo obi = findObisCodeInfo(obisCode);
		RegisterValue registerValue=null;

		if (computePhaseBInstrumentation(obisCode)) {
        	return getRegisterFromDSPInstrumentationCache(obisCode, obi);
        }


		if (obi.isCurrent()) { // F FIELD
			RegisterData registerData = alphaA3.getStandardTableFactory().getCurrentRegisterDataTable().getRegisterData();
			if (obi.getTierIndex() == -1)  // E FIELD
			{
				registerValue = doGetRegister(obi, registerData.getTotDatablock());
			} else {
				registerValue = doGetRegister(obi, registerData.getTierDataBlocks()[obi.getTierIndex()]);
			}
		}
		else if (obi.isPreviousSeason()) {
			RegisterData registerData = alphaA3.getStandardTableFactory().getPreviousSeasonDataTable().getPreviousSeasonRegisterData();
			RegisterInf registerInf = alphaA3.getStandardTableFactory().getPreviousSeasonDataTable().getRegisterInfo();
			if (obi.getTierIndex() == -1)  // E FIELD
			{
				registerValue = doGetRegister(obi, registerData.getTotDatablock(), registerInf.getEndDateTime());
			} else {
				registerValue = doGetRegister(obi, registerData.getTierDataBlocks()[obi.getTierIndex()], registerInf.getEndDateTime());
			}
		}
		else if (obi.isPreviousDemandReset()) {
			RegisterData registerData = alphaA3.getStandardTableFactory().getPreviousDemandResetDataTable().getPreviousDemandResetData();
			RegisterInf registerInf = alphaA3.getStandardTableFactory().getPreviousDemandResetDataTable().getRegisterInfo();
			if (obi.getTierIndex() == -1)  // E FIELD
			{
				registerValue = doGetRegister(obi, registerData.getTotDatablock(), registerInf.getEndDateTime());
			} else {
				registerValue = doGetRegister(obi, registerData.getTierDataBlocks()[obi.getTierIndex()], registerInf.getEndDateTime());
			}
		}
		else if (obi.isSelfRead()) {
			int index = obi.getSelfReadIndex();
			RegisterData registerData = alphaA3.getStandardTableFactory().getSelfReadDataTable().getSelfReadList().getSelfReadEntries()[index].getSelfReadRegisterData();
			RegisterInf registerInf = alphaA3.getStandardTableFactory().getSelfReadDataTable().getSelfReadList().getSelfReadEntries()[index].getRegisterInfo();
			if (obi.getTierIndex() == -1)  // E FIELD
			{
				registerValue = doGetRegister(obi, registerData.getTotDatablock(), registerInf.getEndDateTime());
			} else {
				registerValue = doGetRegister(obi, registerData.getTierDataBlocks()[obi.getTierIndex()], registerInf.getEndDateTime());
			}
		}

		return registerValue;
	}

	private RegisterValue getRegisterFromDSPInstrumentationCache(
			ObisCode obisCode, ObisCodeInfo obi) throws IOException {

		ActualService actualService = alphaA3.getManufacturerTableFactory().getActualService();
        ABBInstrumentConstants abbic = alphaA3.getManufacturerTableFactory().getABBInstrumentConstants();
        DSPRawInstrumentationCache dspic = alphaA3.getManufacturerTableFactory().getDSPRawInstrumentationCache();
        int sourceIndex = obi.getDatacontrolEntryIndex();
    	SourceDefinitionTable sourceDefinitionTable = alphaA3.getManufacturerTableFactory().getSourceDefinitionTable();

        int multiplierSelect = sourceDefinitionTable.getSourceDefinitionEntries()[sourceIndex].getMultiplierSelect();
        int scale = alphaA3.getManufacturerTableFactory().getFactoryDefaultMeteringInformation().getInstrumentationScale();
        if (obisCode.getC()==52) {
        	BigDecimal voltAC = abbic.getVoltage_mult().multiply(dspic.getLine_c_to_a_voltage());
        	BigDecimal vt = (BigDecimal)((ElectricConstants)alphaA3.getStandardTableFactory().getConstantsTable().getConstants()[multiplierSelect]).getSet1Constants().getRatioP1();
        	vt = apply10Scaler(vt, scale);
        	voltAC = voltAC.multiply(vt);
        	return new RegisterValue(obisCode, new Quantity(voltAC, Unit.get(BaseUnit.VOLT)), new Date());
        }


        // //    	1 - From MT51, determine if phase rotation is ABC or CBA
//    	2 - Read MT55 to get multiplier values to convert raw Voltage, Current & Energy values from MT71
//    	Do following steps on each read of MT71
//    	3 - Read MT71 and convert raw values with MT55 multipliers to engineering units


    	BigDecimal voltA = abbic.getVoltage_mult().multiply(dspic.getPhase_a_v_rms());//480.5;
    	BigDecimal currA = abbic.getCurrent_mult().multiply(dspic.getPhase_a_i_rms());
    	BigDecimal wattA = abbic.getWatts_mult().multiply(dspic.getPhase_a_w());
    	BigDecimal varA = abbic.getCurrent_mult().multiply(abbic.getVoltage_mult().multiply(dspic.getPhase_a_var()));
    	BigDecimal voltC = abbic.getVoltage_mult().multiply(dspic.getPhase_c_v_rms());
    	BigDecimal currC = abbic.getCurrent_mult().multiply(dspic.getPhase_c_i_rms());
    	BigDecimal wattC = abbic.getWatts_mult().multiply(dspic.getPhase_c_w());
    	BigDecimal varC = abbic.getCurrent_mult().multiply(abbic.getVoltage_mult().multiply(dspic.getPhase_c_var()));
    	BigDecimal voltAC = abbic.getVoltage_mult().multiply(dspic.getLine_c_to_a_voltage());//479.2;


    	if(DEBUG>0) {
		    System.out.println("voltA: " + voltA);
	    }
    	if(DEBUG>0) {
		    System.out.println("currA: " + currA);
	    }
    	if(DEBUG>0) {
		    System.out.println("wattA: " + wattA);
	    }
    	if(DEBUG>0) {
		    System.out.println("varA: " + varA);
	    }
    	if(DEBUG>0) {
		    System.out.println("voltC: " + voltC);
	    }
    	if(DEBUG>0) {
		    System.out.println("currC: " + currC);
	    }
    	if(DEBUG>0) {
		    System.out.println("wattC: " + wattC);
	    }
    	if(DEBUG>0) {
		    System.out.println("varC: " + varC);
	    }
    	if(DEBUG>0) {
		    System.out.println("voltAC: " + voltAC);
	    }


    	int rotation = actualService.getRotation();
    	if(DEBUG>0) {
		    System.out.println("rotation: " + rotation);
	    }


//    	4 - Use Wa and VARa to determine Phase angle of Ia with respect to Vab
//			=MOD(DEGREES(ATAN(VARa/Wa))+IF(Wa<0,180,0),360)
    	if(DEBUG>0) {
		    System.out.println("var/watt: " + varA.doubleValue() / wattA.doubleValue());
	    }

    	double pA = 0;
    	if (Double.isNaN(varA.doubleValue()/wattA.doubleValue())) {
    		pA=90;
    		if (varA.doubleValue()<0) {
    			pA += 180;
    		}
    	} else {
    		pA = Math.toDegrees(Math.atan(varA.doubleValue()/wattA.doubleValue()));
    		if (wattA.doubleValue()<0) {
    			pA += 180;
    		}
    		if (pA<0) {
    			pA = pA+360;
    		}
    	}
    	if(DEBUG>0) {
		    System.out.println("pA: " + pA);
	    }

//    	4a - Use Ia magnitude & phase angle to get real and imaginary components of Ia
//			Ia_real=Ia*COS(RADIANS(Pa))
//    		Ia_imag=Ia*SIN(RADIANS(Pa))
    	double currA_real = currA.doubleValue() * Math.cos(Math.toRadians(pA));
    	double currA_imag = currA.doubleValue() * Math.sin(Math.toRadians(pA));
    	if(DEBUG>0) {
		    System.out.println("currA_real: " + currA_real);
	    }
    	if(DEBUG>0) {
		    System.out.println("currA_imag: " + currA_imag);
	    }

//
//    	5 - use three line to line voltage magnitudes to determine the actual angle of Vcb to Vab
//    	6 - use phase rotation or nominal angle Vcb to Vab to determine the sign of the angle from step 2
//			PVac=
    	int mult = 1;
    	if (rotation==1) {
    		mult = -1;
    	}
    	double pvCA = 360+(mult*Math.toDegrees((Math.acos((Math.pow(voltA.doubleValue(),2)+Math.pow(voltC.doubleValue(),2)-Math.pow(voltAC.doubleValue(),2))/(2*voltA.doubleValue()*voltC.doubleValue())))));
    	pvCA = pvCA % 360;

    	if(DEBUG>0) {
		    System.out.println("pvCA: " + pvCA);
	    }

//    	7 - Use Wc and VARc to determine Phase angle of Ic with respect to Vcb
//			Pc=MOD(DEGREES(ATAN(VARc/Wc))+IF(Wc<0,180,0),360)
    	double pC = 0;

    	if (Double.isNaN(varC.doubleValue()/wattC.doubleValue())) {
    		pC=90;
    		if (varC.doubleValue()<0) {
    			pC += 180;
    		}
    	} else {
    		pC = Math.toDegrees(Math.atan(varC.doubleValue()/wattC.doubleValue()));
    		if (wattC.doubleValue()<0) {
    			pC += 180;
    		}
    		if (pC<0) {
    			pC = pC+360;
    		}
    	}

    	if(DEBUG>0) {
		    System.out.println("pC: " + pC);
	    }

//    	8 - Add the angle from step 4 to the resultant angle from steps 2 & 3 to get the angle of Ic to Vab
//			Pca=MOD(Pc+PVca,360)
    	double pCA=(pC+pvCA)%360;
    	if(DEBUG>0) {
		    System.out.println("pCA " + pCA);
	    }

//    	8a - Use Ic magnitude & phase angle to get real and imaginary components of Ic
//			Ic_real=Ic*COS(RADIANS(Pca))
//    		Ic_imag=Ic*SIN(RADIANS(Pca))
    	double currC_real = currC.doubleValue() * Math.cos(Math.toRadians(pCA));
    	double currC_imag = currC.doubleValue() * Math.sin(Math.toRadians(pCA));
    	if(DEBUG>0) {
		    System.out.println("currC_real " + currC_real);
	    }
    	if(DEBUG>0) {
		    System.out.println("currC_imag " + currC_imag);
	    }

//
//    	9 - Negate the sum of the real components of Ia and Ic to get the real component of Ib
//			Ib_real=-(Ia_real+Ic_real)
    	double currB_real = -1 * (currA_real+currC_real);
    	if(DEBUG>0) {
		    System.out.println("currB_real " + currB_real);
	    }

//    	10 - Negate the sum of the imaginary components of Ia and Ic to get the imaginary component of Ib
//			Ib_imag=-(Ia_imag+Ic_imag)
    	double currB_imag = -1 * (currA_imag+currC_imag);
    	if(DEBUG>0) {
		    System.out.println("currB_imag " + currB_imag);
	    }
//    	11 - Use the real and imaginary components of Ib to calculate a magnitude and phase angle for Ib
//			Ib_mag=(Ib_real^2+Ib_imag^2)^0.5
//    		Pba=MOD(DEGREES(ATAN(Ib_imag/Ib_real))+IF(Ib_real<0,180,0),360)
    	BigDecimal currB = new BigDecimal(Math.sqrt(Math.pow(currB_real, 2)+Math.pow(currB_imag, 2)));
    	double pBA = Math.toDegrees(Math.atan(currB_imag/currB_real));
    	if (currB_real < 0 ) {
    		pBA += 180;
    	}
    	pBA = pBA % 360;

    	BigDecimal ct = (BigDecimal)((ElectricConstants)alphaA3.getStandardTableFactory().getConstantsTable().getConstants()[0]).getSet1Constants().getRatioF1();
        ct = apply10Scaler(ct, scale);
        currB = currB.multiply(ct);

		return new RegisterValue(obisCode, new Quantity(currB, Unit.get(BaseUnit.AMPERE)), new Date());
	}

	private BigDecimal apply10Scaler(BigDecimal bd, int scale) {
        if (scale > 0) {
	        return (bd.movePointRight(scale));
        } else if (scale < 0) {
	        return (bd.movePointLeft((-1) * scale));
        } else {
	        return bd;
        }
    }

	private boolean computePhaseBInstrumentation(ObisCode obisCode) throws IOException {
		int c = obisCode.getC();
		int d = obisCode.getD();
		int e = obisCode.getE();
		int f = obisCode.getF();
		ActualService actualService = alphaA3.getManufacturerTableFactory().getActualService();
		//ensure we're looking for phase B current or voltage, the meter has 2 elements, and the meter
		//is wired in a 3 wire wye or 3 wire delta configuration
		return (c == 51 || c == 52) && (d == 7) && (e == 0) && (f == 255) &&
				(actualService.getMeterElements()==0 && (actualService.getServiceType()==0 || actualService.getServiceType()==2));
	}

	private RegisterValue doGetRegister(ObisCodeInfo obi,DataBlock dataBlock) throws IOException {
		return doGetRegister(obi,dataBlock,null);
	}

	private RegisterValue doGetRegister(ObisCodeInfo obi,DataBlock dataBlock,Date toTime) throws IOException {
		Number value=null;
		Date date=null;
		boolean energy=false;

		if (obi.isTimeIntegral()) { // D FIELD
			int registerIndex = obi.getRegisterIndex();// C
			value = dataBlock.getSummations()[registerIndex];
			energy=true;
		}
		else if (obi.isMaximumDemand()) {
			int registerIndex = obi.getRegisterIndex();// C
			value = dataBlock.getDemands()[registerIndex].getDemands()[obi.getOccurance()];
			if (dataBlock.getDemands()[registerIndex].getEventTimes() != null) {
				date = dataBlock.getDemands()[registerIndex].getEventTimes()[obi.getOccurance()];
			}
		}
		else if (obi.isCumulativeMaximumDemand()) {
			int registerIndex = obi.getRegisterIndex();// C
			value = dataBlock.getDemands()[registerIndex].getCumDemand();
		}
		else if (obi.isContCumulativeMaximumDemand()) {
			int registerIndex = obi.getRegisterIndex();// C
			value = dataBlock.getDemands()[registerIndex].getContinueCumDemand();
		}
		else if (obi.isCoinMaximumDemandDemand()) {
			int registerIndex = obi.getRegisterIndex();// C
			value = dataBlock.getCoincidents()[registerIndex].getCoincidentValues()[obi.getOccurance()];
		}
		else if (obi.isInstantaneous()) {
			Number[] data = alphaA3.getStandardTableFactory().getPresentRegisterDataTable().getPresentValues();
			int registerIndex = obi.getRegisterIndex();
			value = data[registerIndex];
		}

		BigDecimal bd = (BigDecimal)value;
		bd = bd.multiply(alphaA3.getAdjustRegisterMultiplier()); // KV 28062007
		return new RegisterValue(obi.getObisCode(),new Quantity(getEngineeringValue(bd,energy, obi), obi.getUnit()),date,toTime);
	}

	private BigDecimal getEngineeringValue(BigDecimal bd, boolean energy, ObisCodeInfo obi) throws IOException {
		SourceInfo si = new SourceInfo(alphaA3);
		return si.basic2engineering(bd,obi.getDatacontrolEntryIndex(),false,energy);
	}

	private ObisCodeInfo findObisCodeInfo(ObisCode obisCode) throws IOException {
		Iterator<ObisCodeInfo> it = obisCodeInfos.iterator();
		while (it.hasNext()) {
			ObisCodeInfo obi = it.next();
			if (obi.getObisCode().equals(obisCode)) {
				return obi;
			}
		}
		throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
	}

}
