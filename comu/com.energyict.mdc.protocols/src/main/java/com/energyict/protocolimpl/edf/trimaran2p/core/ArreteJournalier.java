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
public class ArreteJournalier extends AbstractTrimaranObject{

	private int variableName;

	private DateType dernierHoroDate;	// date en cours au moment de la lecture
	private Quantity[] ixJour 		= {null, null,		// Index courants des Energies EacpJ, EacnJ en kWh,
										null, null,		// Erc1J, Erc3J, Erc2J, Erc4J en kvarh
										null, null};
	private Quantity[] ixJourmoins1 = {null, null,		// Index courants des Energies EacpJ-1, EacnJ-1 en kWh,
										null, null,		// Erc1J-1, Erc3J-1, Erc2J-1, Erc4J-1 en kvarh
										null, null};
	private Quantity[] ixJourmoins2 = {null, null,		// Index courants des Energies EacpJ-2, EacnJ-2 en kWh,
										null, null,		// Erc1J-2, Erc3J-2, Erc2J-2, Erc4J-2 en kvarh
										null, null};
	private Quantity[] ixJourmoins3 = {null, null,		// Index courants des Energies EacpJ-3, EacnJ-3 en kWh,
										null, null,		// Erc1J-3, Erc3J-3, Erc2J-3, Erc4J-3 en kvarh
										null, null};

	/**
	 *
	 */
	public ArreteJournalier(TrimaranObjectFactory trimaranObjectFactory) {
		super(trimaranObjectFactory);
	}

	protected int getVariableName() {
		return variableName;
	}

	protected void parse(byte[] data) throws IOException {
		int offset = 0;
		TrimaranDataContainer dc = new TrimaranDataContainer();
		dc.parseObjectList(data, getTrimaranObjectFactory().getTrimaran().getLogger());

		setDernierHoroDate(new DateType(dc.getRoot().getLong(offset++), getTrimaranObjectFactory().getTrimaran().getTimeZone()));
		for(int i = 0; i <= 5; i++){
			if (i < 2){
				setIxJour(new Quantity(new BigDecimal(dc.getRoot().getStructure(offset).getLong(i)), Unit.get("kWh")), i);
			} else {
				setIxJour(new Quantity(new BigDecimal(dc.getRoot().getStructure(offset).getLong(i)), Unit.get("kvarh")), i);
			}
		}
		offset++;
		for(int i = 0; i <= 5; i++){
			if (i < 2) {
				setIxJourmoins1(new Quantity(new BigDecimal(dc.getRoot().getStructure(offset).getLong(i)), Unit.get("kWh")), i);
			} else {
				setIxJourmoins1(new Quantity(new BigDecimal(dc.getRoot().getStructure(offset).getLong(i)), Unit.get("kvarh")), i);
			}
		}
		offset++;
		for(int i = 0; i <= 5; i++){
			if (i < 2) {
				setIxJourmoins2(new Quantity(new BigDecimal(dc.getRoot().getStructure(offset).getLong(i)), Unit.get("kWh")), i);
			} else {
				setIxJourmoins2(new Quantity(new BigDecimal(dc.getRoot().getStructure(offset).getLong(i)), Unit.get("kvarh")), i);
			}
		}
		offset++;
		for(int i = 0; i <= 5; i++){
			if (i < 2) {
				setIxJourmoins3(new Quantity(new BigDecimal(dc.getRoot().getStructure(offset).getLong(i)), Unit.get("kWh")), i);
			} else {
				setIxJourmoins3(new Quantity(new BigDecimal(dc.getRoot().getStructure(offset).getLong(i)), Unit.get("kvarh")), i);
			}
		}
	}

	protected byte[] prepareBuild() throws IOException {
		return null;
	}

	/**
	 * @return the dernierHoroDate
	 */
	public DateType getDernierHoroDate() {
		return dernierHoroDate;
	}

	/**
	 * @param dernierHoroDate the dernierHoroDate to set
	 */
	public void setDernierHoroDate(DateType dernierHoroDate) {
		this.dernierHoroDate = dernierHoroDate;
	}

	/**
	 * @return the ixJour
	 */
	public Quantity getIxJour(int i) {
		return ixJour[i];
	}

	/**
	 * @param ixJour the ixJour to set
	 */
	public void setIxJour(Quantity ixJour, int i) {
		this.ixJour[i] = ixJour;
	}

	/**
	 * @return the ixJourmoins1
	 */
	public Quantity getIxJourmoins1(int i) {
		return ixJourmoins1[i];
	}

	/**
	 * @param ixJourmoins1 the ixJourmoins1 to set
	 */
	public void setIxJourmoins1(Quantity ixJourmoins1, int i) {
		this.ixJourmoins1[i] = ixJourmoins1;
	}

	/**
	 * @return the ixJourmoins2
	 */
	public Quantity getIxJourmoins2(int i) {
		return ixJourmoins2[i];
	}

	/**
	 * @param ixJourmoins2 the ixJourmoins2 to set
	 */
	public void setIxJourmoins2(Quantity ixJourmoins2, int i) {
		this.ixJourmoins2[i] = ixJourmoins2;
	}

	/**
	 * @return the ixJourmoins3
	 */
	public Quantity getIxJourmoins3(int i) {
		return ixJourmoins3[i];
	}

	/**
	 * @param ixJourmoins3 the ixJourmoins3 to set
	 */
	public void setIxJourmoins3(Quantity ixJourmoins3, int i) {
		this.ixJourmoins3[i] = ixJourmoins3;
	}

	/**
	 * @param variableName the variableName to set
	 */
	public void setVariableName(int variableName) {
		this.variableName = variableName;
	}

	public String toString(){
		StringBuilder strBuff = new StringBuilder();

		strBuff.append("*** ArretJournalier: ***\n");
		strBuff.append("	- DateFinPeriode: ").append(getDernierHoroDate());

		strBuff.append("	- IxJour EacpJ: ").append(getIxJour(0));strBuff.append("\n");
		strBuff.append("	- IxJour EacnJ: ").append(getIxJour(1));strBuff.append("\n");
		strBuff.append("	- IxJour Erc1J: ").append(getIxJour(2));strBuff.append("\n");
		strBuff.append("	- IxJour Erc3J: ").append(getIxJour(3));strBuff.append("\n");
		strBuff.append("	- IxJour Erc2J: ").append(getIxJour(4));strBuff.append("\n");
		strBuff.append("	- IxJour Erc4J: ").append(getIxJour(5));strBuff.append("\n");

		strBuff.append("	- IxJourmoins1 EacpJ-1: ").append(getIxJourmoins1(0));strBuff.append("\n");
		strBuff.append("	- IxJourmoins1 EacnJ-1: ").append(getIxJourmoins1(1));strBuff.append("\n");
		strBuff.append("	- IxJourmoins1 Erc1J-1: ").append(getIxJourmoins1(2));strBuff.append("\n");
		strBuff.append("	- IxJourmoins1 Erc3J-1: ").append(getIxJourmoins1(3));strBuff.append("\n");
		strBuff.append("	- IxJourmoins1 Erc2J-1: ").append(getIxJourmoins1(4));strBuff.append("\n");
		strBuff.append("	- IxJourmoins1 Erc4J-1: ").append(getIxJourmoins1(5));strBuff.append("\n");

		strBuff.append("	- IxJourmoins2 EacpJ-2: ").append(getIxJourmoins2(0));strBuff.append("\n");
		strBuff.append("	- IxJourmoins2 EacnJ-2: ").append(getIxJourmoins2(1));strBuff.append("\n");
		strBuff.append("	- IxJourmoins2 Erc1J-2: ").append(getIxJourmoins2(2));strBuff.append("\n");
		strBuff.append("	- IxJourmoins2 Erc3J-2: ").append(getIxJourmoins2(3));strBuff.append("\n");
		strBuff.append("	- IxJourmoins2 Erc2J-2: ").append(getIxJourmoins2(4));strBuff.append("\n");
		strBuff.append("	- IxJourmoins2 Erc4J-2: ").append(getIxJourmoins2(5));strBuff.append("\n");

		strBuff.append("	- IxJourmoins3 EacpJ-3: ").append(getIxJourmoins3(0));strBuff.append("\n");
		strBuff.append("	- IxJourmoins3 EacnJ-3: ").append(getIxJourmoins3(1));strBuff.append("\n");
		strBuff.append("	- IxJourmoins3 Erc1J-3: ").append(getIxJourmoins3(2));strBuff.append("\n");
		strBuff.append("	- IxJourmoins3 Erc3J-3: ").append(getIxJourmoins3(3));strBuff.append("\n");
		strBuff.append("	- IxJourmoins3 Erc2J-3: ").append(getIxJourmoins3(4));strBuff.append("\n");
		strBuff.append("	- IxJourmoins3 Erc4J-3: ").append(getIxJourmoins3(5));strBuff.append("\n");

		return strBuff.toString();
	}

}
