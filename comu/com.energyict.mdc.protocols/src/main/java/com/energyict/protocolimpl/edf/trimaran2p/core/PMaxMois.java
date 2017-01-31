/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 *
 */
package com.energyict.protocolimpl.edf.trimaran2p.core;

import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;

import com.energyict.protocolimpl.edf.trimarandlms.axdr.TrimaranDataContainer;
import com.energyict.protocolimpl.edf.trimarandlms.common.DateType;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * @author gna
 *
 */
public class PMaxMois extends AbstractTrimaranObject{

	private int variableName;

	private int numMois;
	private Quantity[] pApMax = {null,null,null,null,null};			// seq of 5 - les puissances actives produites max en kW du mois
	private Quantity[] pAnMax = {null,null,null,null,null};			// seq of 5 - les puissances actives consommées max de kW du mois
	private Quantity[] pRpMax = {null,null,null,null,null};			// seq of 5 - les puissances réactives inductives ou capacitives produites max en kvar du mois
	private Quantity[] pRnMax = {null,null,null,null,null};			// seq of 5 - les puissances réactives inductives ou capacitives consommées max en kvar du mois
	private DateType[] datesPApMax = {null,null,null,null,null};	// seq of 5 - date des puissances actives produites max
	private DateType[] datesPAnMax = {null,null,null,null,null};	// seq of 5 - date des puissances actives consommées max en kW
	private DateType[] datesPRpMax = {null,null,null,null,null};	// seq of 5 - date des puissances réactives inductives ou capacitives produites max en kvar
	private DateType[] datesPRnMax = {null,null,null,null,null};	// seq of 5 - date des puissances réactives inductives ou capacitives consommées max en kvar

	/**
	 *
	 */
	public PMaxMois(TrimaranObjectFactory trimaranObjectFactory) {
		super(trimaranObjectFactory);
	}

	public String toString(){
		StringBuilder strBuff = new StringBuilder();

		strBuff.append(" *** PMaxMois: ***" + "\n");
		strBuff.append("	- Numbre mois: ").append(getNumMois()).append("\n");

		strBuff.append("	- PApMax 1: ").append(getPApMax(0)).append("\n");
		strBuff.append("	- PApMax 2: ").append(getPApMax(1)).append("\n");
		strBuff.append("	- PApMax 3: ").append(getPApMax(2)).append("\n");
		strBuff.append("	- PApMax 4: ").append(getPApMax(3)).append("\n");
		strBuff.append("	- PApMax 5: ").append(getPApMax(4)).append("\n");

		strBuff.append("	- PAnMax 1: ").append(getPAnMax(0)).append("\n");
		strBuff.append("	- PAnMax 2: ").append(getPAnMax(1)).append("\n");
		strBuff.append("	- PAnMax 3: ").append(getPAnMax(2)).append("\n");
		strBuff.append("	- PAnMax 4: ").append(getPAnMax(3)).append("\n");
		strBuff.append("	- PAnMax 5: ").append(getPAnMax(4)).append("\n");

		strBuff.append("	- PRpMax 1: ").append(getPRpMax(0)).append("\n");
		strBuff.append("	- PRpMax 2: ").append(getPRpMax(1)).append("\n");
		strBuff.append("	- PRpMax 3: ").append(getPRpMax(2)).append("\n");
		strBuff.append("	- PRpMax 4: ").append(getPRpMax(3)).append("\n");
		strBuff.append("	- PRpMax 5: ").append(getPRpMax(4)).append("\n");

		strBuff.append("	- PRnMax 1: ").append(getPRnMax(0)).append("\n");
		strBuff.append("	- PRnMax 2: ").append(getPRnMax(1)).append("\n");
		strBuff.append("	- PRnMax 3: ").append(getPRnMax(2)).append("\n");
		strBuff.append("	- PRnMax 4: ").append(getPRnMax(3)).append("\n");
		strBuff.append("	- PRnMax 5: ").append(getPRnMax(4)).append("\n");

		strBuff.append(" 	- DatesPApMax 1: ").append(getDatesPApMax(0));
		strBuff.append(" 	- DatesPApMax 2: ").append(getDatesPApMax(1));
		strBuff.append(" 	- DatesPApMax 3: ").append(getDatesPApMax(2));
		strBuff.append(" 	- DatesPApMax 4: ").append(getDatesPApMax(3));
		strBuff.append(" 	- DatesPApMax 5: ").append(getDatesPApMax(4));

		strBuff.append(" 	- DatesPAnMax 1: ").append(getDatesPAnMax(0));
		strBuff.append(" 	- DatesPAnMax 2: ").append(getDatesPAnMax(1));
		strBuff.append(" 	- DatesPAnMax 3: ").append(getDatesPAnMax(2));
		strBuff.append(" 	- DatesPAnMax 4: ").append(getDatesPAnMax(3));
		strBuff.append(" 	- DatesPAnMax 5: ").append(getDatesPAnMax(4));

		strBuff.append(" 	- DatesPRpMax 1: ").append(getDatesPRpMax(0));
		strBuff.append(" 	- DatesPRpMax 2: ").append(getDatesPRpMax(1));
		strBuff.append(" 	- DatesPRpMax 3: ").append(getDatesPRpMax(2));
		strBuff.append(" 	- DatesPRpMax 4: ").append(getDatesPRpMax(3));
		strBuff.append(" 	- DatesPRpMax 5: ").append(getDatesPRpMax(4));

		strBuff.append(" 	- DatesPRnMax 1: ").append(getDatesPRnMax(0));
		strBuff.append(" 	- DatesPRnMax 2: ").append(getDatesPRnMax(1));
		strBuff.append(" 	- DatesPRnMax 3: ").append(getDatesPRnMax(2));
		strBuff.append(" 	- DatesPRnMax 4: ").append(getDatesPRnMax(3));
		strBuff.append(" 	- DatesPRnMax 5: ").append(getDatesPRnMax(4));

		return strBuff.toString();
	}

	protected int getVariableName() {
		return variableName;
	}

	protected void parse(byte[] data) throws IOException {

		int offset = 0;
		TrimaranDataContainer dc = new TrimaranDataContainer();
		dc.parseObjectList(data, getTrimaranObjectFactory().getTrimaran().getLogger());

		setNumMois(dc.getRoot().getInteger(offset++));
		for(int i = 0; i < 5; i++) {
			setPApMax(new Quantity(new BigDecimal(dc.getRoot().getStructure(offset).getLong(i)), Unit.get("kW")), i);
		}
		offset++;
		for(int i = 0; i < 5; i++) {
			setPAnMax(new Quantity(new BigDecimal(dc.getRoot().getStructure(offset).getLong(i)), Unit.get("kW")), i);
		}
		offset++;
		for(int i = 0; i < 5; i++) {
			setPRpMax(new Quantity(new BigDecimal(dc.getRoot().getStructure(offset).getLong(i)), Unit.get("kW")), i);
		}
		offset++;
		for(int i = 0; i < 5; i++) {
			setPRnMax(new Quantity(new BigDecimal(dc.getRoot().getStructure(offset).getLong(i)), Unit.get("kW")), i);
		}
		offset++;

		for(int i = 0; i < 5; i++) {
			setDatesPApMax(new DateType(dc.getRoot().getStructure(offset).getLong(i), getTrimaranObjectFactory().getTrimaran().getTimeZone()), i);
		}
		offset++;
		for(int i = 0; i < 5; i++) {
			setDatesPAnMax(new DateType(dc.getRoot().getStructure(offset).getLong(i), getTrimaranObjectFactory().getTrimaran().getTimeZone()), i);
		}
		offset++;
		for(int i = 0; i < 5; i++) {
			setDatesPRpMax(new DateType(dc.getRoot().getStructure(offset).getLong(i), getTrimaranObjectFactory().getTrimaran().getTimeZone()), i);
		}
		offset++;
		for(int i = 0; i < 5; i++) {
			setDatesPRnMax(new DateType(dc.getRoot().getStructure(offset).getLong(i), getTrimaranObjectFactory().getTrimaran().getTimeZone()), i);
		}
		offset++;
	}

	protected byte[] prepareBuild() throws IOException {
		return null;
	}

	/**
	 * @return the numMois
	 */
	protected int getNumMois() {
		return numMois;
	}

	/**
	 * @param numMois the numMois to set
	 */
	protected void setNumMois(int numMois) {
		this.numMois = numMois;
	}

	/**
	 * @return the pApMax
	 */
	protected Quantity getPApMax(int i) {
		return pApMax[i];
	}

	/**
	 * @param apMax the pApMax to set
	 */
	protected void setPApMax(Quantity apMax, int i) {
		pApMax[i] = apMax;
	}

	/**
	 * @return the pAnMax
	 */
	protected Quantity getPAnMax(int i) {
		return pAnMax[i];
	}

	/**
	 * @param anMax the pAnMax to set
	 */
	protected void setPAnMax(Quantity anMax, int i) {
		pAnMax[i] = anMax;
	}

	/**
	 * @return the pRpMax
	 */
	protected Quantity getPRpMax(int i) {
		return pRpMax[i];
	}

	/**
	 * @param rpMax the pRpMax to set
	 */
	protected void setPRpMax(Quantity rpMax, int i) {
		pRpMax[i] = rpMax;
	}

	/**
	 * @return the pRnMax
	 */
	protected Quantity getPRnMax(int i) {
		return pRnMax[i];
	}

	/**
	 * @param rnMax the pRnMax to set
	 */
	protected void setPRnMax(Quantity rnMax, int i) {
		pRnMax[i] = rnMax;
	}

	/**
	 * @return the datesPApMax
	 */
	protected DateType getDatesPApMax(int i) {
		return datesPApMax[i];
	}

	/**
	 * @param datesPApMax the datesPApMax to set
	 */
	protected void setDatesPApMax(DateType datesPApMax, int i) {
		this.datesPApMax[i] = datesPApMax;
	}

	/**
	 * @return the datesPAnMax
	 */
	protected DateType getDatesPAnMax(int i) {
		return datesPAnMax[i];
	}

	/**
	 * @param datesPAnMax the datesPAnMax to set
	 */
	protected void setDatesPAnMax(DateType datesPAnMax, int i) {
		this.datesPAnMax[i] = datesPAnMax;
	}

	/**
	 * @return the datesPRpMax
	 */
	protected DateType getDatesPRpMax(int i) {
		return datesPRpMax[i];
	}

	/**
	 * @param datesPRpMax the datesPRpMax to set
	 */
	protected void setDatesPRpMax(DateType datesPRpMax, int i) {
		this.datesPRpMax[i] = datesPRpMax;
	}

	/**
	 * @return the datesPRnMax
	 */
	protected DateType getDatesPRnMax(int i) {
		return datesPRnMax[i];
	}

	/**
	 * @param datesPRnMax the datesPRnMax to set
	 */
	protected void setDatesPRnMax(DateType datesPRnMax, int i) {
		this.datesPRnMax[i] = datesPRnMax;
	}

	/**
	 * @param variableName the variableName to set
	 */
	protected void setVariableName(int variableName) {
		this.variableName = variableName;
	}

}
