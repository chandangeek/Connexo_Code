/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.protocolimpl.ansi.c12.tables.ActualRegisterTable;
import com.energyict.protocolimpl.ansi.c12.tables.DataBlock;
import com.energyict.protocolimpl.ansi.c12.tables.RegisterData;
import com.energyict.protocolimpl.ansi.c12.tables.RegisterInf;
import com.energyict.protocolimpl.ansi.c12.tables.StandardTableFactory;
import com.energyict.protocolimpl.elster.a3.tables.ObisCodeDescriptor;
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

	List obisCodeInfos;
	AlphaA3 alphaA3;

	/** Creates a new instance of ObisCodeInfoFactory */
	public ObisCodeInfoFactory(AlphaA3 alphaA3) throws IOException {
		this.alphaA3=alphaA3;
		buildObisCodeInfos();
	}

	public String toString() {
		StringBuffer strBuff = new StringBuffer();
		Iterator it = obisCodeInfos.iterator();
		while(it.hasNext()) {
			strBuff.append(it.next()+"\n");
		}
		return strBuff.toString();
	}

	public static final int CURRENT=255;
	public static final int PREVIOUS_SEASON=0;
	public static final int PREVIOUS_DEMAND_RESET=1;
	public static final int SELF_READ_OFFSET=254;

	public void buildObisCodeInfos() throws IOException {
		obisCodeInfos=new ArrayList();
		ActualRegisterTable art = alphaA3.getStandardTableFactory().getActualRegisterTable();

		// current registers
		if (alphaA3.getStandardTableFactory().getConfigurationTable().isStdTableUsed(StandardTableFactory.CURRENT_REGISTER_DATA_TABLE))
			buildRegisterObisCodeInfos(CURRENT);
		// previous season registers
		if (alphaA3.getStandardTableFactory().getConfigurationTable().isStdTableUsed(StandardTableFactory.PREVIOUS_SEASON_DATA_TABLE))
			buildRegisterObisCodeInfos(PREVIOUS_SEASON);
		// previous demand reset registers
		if (alphaA3.getStandardTableFactory().getConfigurationTable().isStdTableUsed(StandardTableFactory.PREVIOUS_DEMAND_RESET_DATA_TABLE))
			buildRegisterObisCodeInfos(PREVIOUS_DEMAND_RESET);
		// self read registers
		if (alphaA3.getStandardTableFactory().getConfigurationTable().isStdTableUsed(StandardTableFactory.SELF_READ_DATA_TABLE))
			for(int index=0;index<art.getNrOfSelfReads();index++) {
				buildRegisterObisCodeInfos(SELF_READ_OFFSET-index);
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
                        if (e.getMessage().equalsIgnoreCase("ReadResponse, parse, checksum failure in table data!")) {
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

		if (obi.isCurrent()) { // F FIELD
			RegisterData registerData = alphaA3.getStandardTableFactory().getCurrentRegisterDataTable().getRegisterData();
			if (obi.getTierIndex() == -1)  // E FIELD
				registerValue = doGetRegister(obi, registerData.getTotDatablock());
			else
				registerValue = doGetRegister(obi, registerData.getTierDataBlocks()[obi.getTierIndex()]);
		}
		else if (obi.isPreviousSeason()) {
			RegisterData registerData = alphaA3.getStandardTableFactory().getPreviousSeasonDataTable().getPreviousSeasonRegisterData();
			RegisterInf registerInf = alphaA3.getStandardTableFactory().getPreviousSeasonDataTable().getRegisterInfo();
			if (obi.getTierIndex() == -1)  // E FIELD
				registerValue = doGetRegister(obi, registerData.getTotDatablock(), registerInf.getEndDateTime());
			else
				registerValue = doGetRegister(obi, registerData.getTierDataBlocks()[obi.getTierIndex()], registerInf.getEndDateTime());
		}
		else if (obi.isPreviousDemandReset()) {
			RegisterData registerData = alphaA3.getStandardTableFactory().getPreviousDemandResetDataTable().getPreviousDemandResetData();
			RegisterInf registerInf = alphaA3.getStandardTableFactory().getPreviousDemandResetDataTable().getRegisterInfo();
			if (obi.getTierIndex() == -1)  // E FIELD
				registerValue = doGetRegister(obi, registerData.getTotDatablock(), registerInf.getEndDateTime());
			else
				registerValue = doGetRegister(obi, registerData.getTierDataBlocks()[obi.getTierIndex()], registerInf.getEndDateTime());
		}
		else if (obi.isSelfRead()) {
			int index = obi.getSelfReadIndex();
			RegisterData registerData = alphaA3.getStandardTableFactory().getSelfReadDataTable().getSelfReadList().getSelfReadEntries()[index].getSelfReadRegisterData();
			RegisterInf registerInf = alphaA3.getStandardTableFactory().getSelfReadDataTable().getSelfReadList().getSelfReadEntries()[index].getRegisterInfo();
			if (obi.getTierIndex() == -1)  // E FIELD
				registerValue = doGetRegister(obi, registerData.getTotDatablock(), registerInf.getEndDateTime());
			else
				registerValue = doGetRegister(obi, registerData.getTierDataBlocks()[obi.getTierIndex()], registerInf.getEndDateTime());
		}

		return registerValue;
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
			if (dataBlock.getDemands()[registerIndex].getEventTimes() != null)
				date = dataBlock.getDemands()[registerIndex].getEventTimes()[obi.getOccurance()];
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
		Iterator it = obisCodeInfos.iterator();
		while(it.hasNext()) {
			ObisCodeInfo obi = (ObisCodeInfo)it.next();
			if (obi.getObisCode().equals(obisCode)) return obi;
		}
		throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
	}

}
