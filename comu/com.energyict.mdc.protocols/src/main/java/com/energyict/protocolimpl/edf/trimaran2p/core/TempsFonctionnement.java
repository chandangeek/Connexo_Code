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
public class TempsFonctionnement extends AbstractTrimaranObject{

	private int variableName;
	private DateType debutPeriode152;	// date du jour à 00h de la dernière mise à zéro de l'index NJ ou date d'initialisation si elle est postérieure
	private DateType dernierHoroDate;	// date courante
	private Quantity tempsFonct;				// temps de fonctionnement du compteur depuis la derniére initialisation (en minutes)


	/**
	 *
	 */
	public TempsFonctionnement(TrimaranObjectFactory trimaranObjectFactory) {
		super(trimaranObjectFactory);
	}

	protected int getVariableName() {
		return variableName;
	}

	protected void parse(byte[] data) throws IOException {
		int offset = 0;
		TrimaranDataContainer dc = new TrimaranDataContainer();
		dc.parseObjectList(data, null);

		setDebutPeriode152(new DateType(dc.getRoot().getLong(offset++), getTrimaranObjectFactory().getTrimaran().getTimeZone()));
		setDernierHoroDate(new DateType(dc.getRoot().getLong(offset++), getTrimaranObjectFactory().getTrimaran().getTimeZone()));
		setTempsFonct(new Quantity(new BigDecimal(dc.getRoot().getLong(offset++)), Unit.get("min")));

	}

	protected byte[] prepareBuild() throws IOException {
		return null;
	}

	/**
	 * @return the debutPeriode152
	 */
	public DateType getDebutPeriode152() {
		return debutPeriode152;
	}

	/**
	 * @param debutPeriode152 the debutPeriode152 to set
	 */
	public void setDebutPeriode152(DateType debutPeriode152) {
		this.debutPeriode152 = debutPeriode152;
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
	 * @return the tempsFonct
	 */
	public Quantity getTempsFonct() {
		return tempsFonct;
	}

	/**
	 * @param tempsFonct the tempsFonct to set
	 */
	public void setTempsFonct(Quantity tempsFonct) {
		this.tempsFonct = tempsFonct;
	}

	/**
	 * @param variableName the variableName to set
	 */
	public void setVariableName(int variableName) {
		this.variableName = variableName;
	}

	public String toString(){
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("*** TempsFonctionnement: ***\n");
		strBuff.append("	- debutPeriod152: " + getDebutPeriode152());
		strBuff.append("	- dernierHoroDate: " + getDernierHoroDate());
		strBuff.append("	- tempsFonctionne: " + getTempsFonct());strBuff.append("\n");
		return strBuff.toString();
	}

}
