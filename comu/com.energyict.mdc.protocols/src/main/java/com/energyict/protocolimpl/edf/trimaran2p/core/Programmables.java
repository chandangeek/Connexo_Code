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
public class Programmables{

	private int variableName;

	private DateType debutPeriode;			// Date du jour à 00h de la derniére mise à zero de l'index NJ ou date d'initialisation si elle est postérieure
	private int nombre;						// Périodicité en nombre de jours pour arrêter l'index
	private DateType dernierHorodate;		// Date en cours au moment de la lecture
	private Quantity[] ixProg = {null, null, null,		// Index courants des énergies EacpNJ, EacnNJ en kWh,
									null, null, null};	// Erc1NJ, Erc3NJ, Erc2NJ, Erc4NJ en kvarh
	private Quantity[] ixProgMoins1 = {null, null, null,	// Index courants des énergies EacpNJ-1, EacnNJ-1 en kWh,
										null, null, null};	// Erc1NJ-1, Erc3NJ-1, Erc2NJ-1, Erc4NJ-1 en kvarh

	/**
	 * @throws IOException
	 *
	 */
	public Programmables(TrimaranDataContainer dc, TimeZone timeZone, int variableName) throws IOException {
		int offset = 0;
		setVariableName(variableName);

		setDebutPeriode(new DateType(dc.getRoot().getLong(offset++), timeZone));
		setNombre(dc.getRoot().getInteger(offset++));
		setDernierHorodate(new DateType(dc.getRoot().getLong(offset++), timeZone));
		for(int i = 0; i < 6; i++){
			if (i < 2) {
				setIxProg(new Quantity(new BigDecimal(dc.getRoot().getStructure(offset).getLong(i)), Unit.get("kWh")), i);
			} else {
				setIxProg(new Quantity(new BigDecimal(dc.getRoot().getStructure(offset).getLong(i)), Unit.get("kvarh")), i);
			}
		}
		offset++;
		for(int i = 0; i < 6; i++){
			if (i < 2) {
				setIxProgMoins1(new Quantity(new BigDecimal(dc.getRoot().getStructure(offset).getLong(i)), Unit.get("kWh")), i);
			} else {
				setIxProgMoins1(new Quantity(new BigDecimal(dc.getRoot().getStructure(offset).getLong(i)), Unit.get("kvarh")), i);
			}
		}

	}

	protected int getVariableName() {
		return variableName;
	}

	protected void parse(byte[] data) throws IOException {

	}

	public String toString(){
		StringBuffer strBuff = new StringBuffer();

		strBuff.append("*** IndexProgrammables + " + getVariableName() + ": ***\n");
		strBuff.append("	- DebutPeriode: " + getDebutPeriode());
		strBuff.append("	- Nombre: " + getNombre());strBuff.append("\n");
		strBuff.append("	- DernierHorodate: " + getDernierHorodate());

		strBuff.append("	- IxProg EacpNJ: " + getIxProg(0));strBuff.append("\n");
		strBuff.append("	- IxProg EacnNJ: " + getIxProg(1));strBuff.append("\n");
		strBuff.append("	- IxProg Erc1NJ: " + getIxProg(2));strBuff.append("\n");
		strBuff.append("	- IxProg Erc3NJ: " + getIxProg(3));strBuff.append("\n");
		strBuff.append("	- IxProg Erc2NJ: " + getIxProg(4));strBuff.append("\n");
		strBuff.append("	- IxProg Erc4NJ: " + getIxProg(5));strBuff.append("\n");

		strBuff.append("	- IxProg EacpNJ-1: " + getIxProgMoins1(0));strBuff.append("\n");
		strBuff.append("	- IxProg EacnNJ-1: " + getIxProgMoins1(1));strBuff.append("\n");
		strBuff.append("	- IxProg Erc1NJ-1: " + getIxProgMoins1(2));strBuff.append("\n");
		strBuff.append("	- IxProg Erc3NJ-1: " + getIxProgMoins1(3));strBuff.append("\n");
		strBuff.append("	- IxProg Erc2NJ-1: " + getIxProgMoins1(4));strBuff.append("\n");
		strBuff.append("	- IxProg Erc4NJ-1: " + getIxProgMoins1(5));strBuff.append("\n");

		return strBuff.toString();
	}

	protected byte[] prepareBuild() throws IOException {
		return null;
	}

	/**
	 * @return the debutPeriode - Start of measurement period
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
	 * @return the nombre
	 */
	public int getNombre() {
		return nombre;
	}

	/**
	 * @param nombre the nombre to set
	 */
	public void setNombre(int nombre) {
		this.nombre = nombre;
	}

	/**
	 * @return the dernierHorodate - Current Date
	 */
	public DateType getDernierHorodate() {
		return dernierHorodate;
	}

	/**
	 * @param dernierHorodate the dernierHorodate to set - Set the current date
	 */
	public void setDernierHorodate(DateType dernierHorodate) {
		this.dernierHorodate = dernierHorodate;
	}

	/**
	 * @return the ixProg
	 */
	public Quantity getIxProg(int i) {
		return ixProg[i];
	}

	/**
	 * @param ixProg the ixProg to set
	 */
	public void setIxProg(Quantity ixProg, int i) {
		this.ixProg[i] = ixProg;
	}

	/**
	 * @return the ixProgMoins1
	 */
	public Quantity getIxProgMoins1(int i) {
		return ixProgMoins1[i];
	}

	/**
	 * @param ixProgMoins1 the ixProgMoins1 to set
	 */
	public void setIxProgMoins1(Quantity ixProgMoins1, int i) {
		this.ixProgMoins1[i] = ixProgMoins1;
	}

	/**
	 * @param variableName the variableName to set
	 */
	public void setVariableName(int variableName) {
		this.variableName = variableName;
	}

}
