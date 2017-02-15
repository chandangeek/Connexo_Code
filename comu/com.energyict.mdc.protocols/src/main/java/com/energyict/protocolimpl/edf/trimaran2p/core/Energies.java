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
import java.util.TimeZone;

/**
 * @author gna
 *
 */
public class Energies{

	private int variableName;

	private DateType debutPeriode;	// Date de mise en service
	private DateType dernierHoroDate;	// Date courante au moment de la lecture de la variable
	private Quantity[] ixNRJact = {null, null};			// seq of 2 - index des Energies Eabp, Eabn exprimés en MWh
	private Quantity[] nRJact_Reste = {null, null};		// seq of 2 - reste des Energies Eabp, Eabn exprimés en kWh
	private Quantity[] ixNRJind = {null, null};			// seq of 2 - index des Energies Q+ Erb1, Q- Erb3 exprimés en Mvarh
	private Quantity[] nRJind_Reste = {null, null};		// seq of 2 - reste des Energies Q+ Erb1, Q- Erb3 exprimés en kvarh
	private Quantity[] ixNRJcap = {null, null};			// seq of 2 - index des Energies Q+ Erb2, Q- Erb4 exprimés en Mvarh
	private Quantity[] nRJcap_Reste = {null, null};		// seq of 2 - reste des Energies Q+ Erb2, Q- Erb4 exprimés en kvarh


	public Energies(TrimaranDataContainer dc, TimeZone timeZone, int variableName) throws IOException {


//    	System.out.println("GN_DEBUG> write to file");
//    	File file = new File("c://TEST_FILES/Energies184.bin");
//    	FileOutputStream fos = new FileOutputStream(file);
//    	ObjectOutputStream oos = new ObjectOutputStream(fos);
//    	oos.writeObject(dc);
//    	oos.close();
//    	fos.close();

		setVariableName(variableName);
		int offset = 0;
		setDebutPeriode(new DateType(dc.getRoot().getLong(offset++), timeZone));
		setDernierHoroDate(new DateType(dc.getRoot().getLong(offset++), timeZone));
		setIxNRJact(new Quantity(new BigDecimal(""+dc.getRoot().getStructure(offset).getLong(0)), Unit.get("MWh")), 0);
		setIxNRJact(new Quantity(new BigDecimal(""+dc.getRoot().getStructure(offset++).getLong(1)), Unit.get("MWh")), 1);
		setNRJact_Reste(new Quantity(new BigDecimal(""+dc.getRoot().getStructure(offset).getInteger(0)), Unit.get("kWh")), 0);
		setNRJact_Reste(new Quantity(new BigDecimal(""+dc.getRoot().getStructure(offset++).getInteger(1)), Unit.get("kWh")), 1);
		setIxNRJind(new Quantity(new BigDecimal(""+dc.getRoot().getStructure(offset).getLong(0)), Unit.get("Mvarh")), 0);
		setIxNRJind(new Quantity(new BigDecimal(""+dc.getRoot().getStructure(offset++).getLong(1)), Unit.get("Mvarh")), 1);
		setNRJind_Reste(new Quantity(new BigDecimal(""+dc.getRoot().getStructure(offset).getInteger(0)), Unit.get("kvarh")), 0);
		setNRJind_Reste(new Quantity(new BigDecimal(""+dc.getRoot().getStructure(offset++).getInteger(1)), Unit.get("kvarh")), 1);
		setIxNRJcap(new Quantity(new BigDecimal(""+dc.getRoot().getStructure(offset).getLong(0)), Unit.get("Mvarh")), 0);
		setIxNRJcap(new Quantity(new BigDecimal(""+dc.getRoot().getStructure(offset++).getLong(1)), Unit.get("Mvarh")), 1);
		setNRJcap_Reste(new Quantity(new BigDecimal(""+dc.getRoot().getStructure(offset).getInteger(0)), Unit.get("kvarh")), 0);
		setNRJcap_Reste(new Quantity(new BigDecimal(""+dc.getRoot().getStructure(offset++).getInteger(1)), Unit.get("kvarh")), 1);
	}

	public String toString(){
		StringBuffer strBuff = new StringBuffer();

		strBuff.append("*** Energies " + getVariableName() + ": ***\n");
		strBuff.append("	- DateDebutPeriode: " + getDebutPeriode());
		strBuff.append("	- DateDernierHoroDate: " + getDernierHoroDate());

		strBuff.append("	- IxNRJact - Eabp: " + getIxNRJact(0));strBuff.append("\n");
		strBuff.append("	- IxNRJact - Eabn: " + getIxNRJact(1));strBuff.append("\n");
		strBuff.append("	- NRJact_Reste - Eabp: " + getNRJact_Reste(0));strBuff.append("\n");
		strBuff.append("	- NRJact_Reste - Eabn: " + getNRJact_Reste(1));strBuff.append("\n");

		strBuff.append("	- IxNRJind - Q+ Erb1: " + getIxNRJind(0));strBuff.append("\n");
		strBuff.append("	- IxNRJind - Q- Erb3: " + getIxNRJind(1));strBuff.append("\n");
		strBuff.append("	- NRJind_Reste - Q+ Erb1: " + getNRJind_Reste(0));strBuff.append("\n");
		strBuff.append("	- NRJind_Reste - Q- Erb3: " + getNRJind_Reste(1));strBuff.append("\n");

		strBuff.append("	- IxNRJcap - Q+ Erb2: " + getIxNRJcap(0));strBuff.append("\n");
		strBuff.append("	- IxNRJcap - Q- Erb4: " + getIxNRJcap(1));strBuff.append("\n");
		strBuff.append("	- NRJcap_Reste - Q+ Erb2: " + getNRJcap_Reste(0));strBuff.append("\n");
		strBuff.append("	- NRJcap_Reste - Q- Erb4: " + getNRJcap_Reste(1));strBuff.append("\n");

		return strBuff.toString();
	}

	protected int getVariableName() {
		return variableName;
	}

	protected void parse(byte[] data) throws IOException {
	}

	protected byte[] prepareBuild() throws IOException {
		return null;
	}

	/**
	 * @return the debutPeriode
	 */
	public DateType getDebutPeriode() {
		return debutPeriode;
	}

	/**
	 * @param debutPeriode the debutPeriode to set
	 */
	public void setDebutPeriode(DateType debutPeriode) {
		this.debutPeriode = debutPeriode;
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
	 * @return the ixNRJact
	 */
	public Quantity getIxNRJact(int i) {
		return ixNRJact[i];
	}

	/**
	 * @param ixNRJact the ixNRJact to set
	 */
	public void setIxNRJact(Quantity ixNRJact, int i) {
		this.ixNRJact[i] = ixNRJact;
	}

	/**
	 * @return the nRJact_Reste
	 */
	public Quantity getNRJact_Reste(int i) {
		return nRJact_Reste[i];
	}

	/**
	 * @param jact_Reste the nRJact_Reste to set
	 */
	public void setNRJact_Reste(Quantity jact_Reste, int i) {
		nRJact_Reste[i] = jact_Reste;
	}

	/**
	 * @return the ixNRJind
	 */
	public Quantity getIxNRJind(int i) {
		return ixNRJind[i];
	}

	/**
	 * @param ixNRJind the ixNRJind to set
	 */
	public void setIxNRJind(Quantity ixNRJind, int i) {
		this.ixNRJind[i] = ixNRJind;
	}

	/**
	 * @return the nRJind_Reste
	 */
	public Quantity getNRJind_Reste(int i) {
		return nRJind_Reste[i];
	}

	/**
	 * @param jind_Reste the nRJind_Reste to set
	 */
	public void setNRJind_Reste(Quantity jind_Reste, int i) {
		nRJind_Reste[i] = jind_Reste;
	}

	/**
	 * @return the ixNRJcap
	 */
	public Quantity getIxNRJcap(int i) {
		return ixNRJcap[i];
	}

	/**
	 * @param ixNRJcap the ixNRJcap to set
	 */
	public void setIxNRJcap(Quantity ixNRJcap, int i) {
		this.ixNRJcap[i] = ixNRJcap;
	}

	/**
	 * @return the nRJcap_Reste
	 */
	public Quantity getNRJcap_Reste(int i) {
		return nRJcap_Reste[i];
	}

	/**
	 * @param jcap_Reste the nRJcap_Reste to set
	 */
	public void setNRJcap_Reste(Quantity jcap_Reste, int i) {
		nRJcap_Reste[i] = jcap_Reste;
	}

	/**
	 * @param variableName the variableName to set
	 */
	public void setVariableName(int variableName) {
		this.variableName = variableName;
	}

}
