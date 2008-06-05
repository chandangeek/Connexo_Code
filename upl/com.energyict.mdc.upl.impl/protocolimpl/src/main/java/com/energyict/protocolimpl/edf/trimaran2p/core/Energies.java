/**
 * 
 */
package com.energyict.protocolimpl.edf.trimaran2p.core;

import java.io.IOException;
import java.util.TimeZone;

import com.energyict.protocolimpl.edf.trimarandlms.axdr.DataContainer;
import com.energyict.protocolimpl.edf.trimarandlms.common.DateType;

/**
 * @author gna
 *
 */
public class Energies{
	
	private int variableName;
	
	private DateType debutPeriode;	// Date de mise en service
	private DateType dernierHoroDate;	// Date courante au moment de la lecture de la variable
	private long[] ixNRJact;			// seq of 2 - index des Energies Eabp, Eabn exprimés en MWh
	private int[] nRJact_Reste;		// seq of 2 - reste des Energies Eabp, Eabn exprimés en kWh
	private long[] ixNRJind;			// seq of 2 - index des Energies Q+ Erb1, Q- Erb3 exprimés en Mvarh
	private int[] nRJind_Reste;		// seq of 2 - reste des Energies Q+ Erb1, Q- Erb3 exprimés en kvarh
	private long[] ixNRJcap;			// seq of 2 - index des Energies Q+ Erb2, Q- Erb4 exprimés en Mvarh
	private int[] nRJcap_Reste;		// seq of 2 - reste des Energies Q+ Erb2, Q- Erb4 exprimés en kvarh


	public Energies(DataContainer dc, TimeZone timeZone, int variableName) throws IOException {
		setVariableName(variableName);
		int offset = 0;
		setDebutPeriode(new DateType(dc.getRoot().getLong(offset++), timeZone));
		setDernierHoroDate(new DateType(dc.getRoot().getLong(offset++), timeZone));
		setIxNRJact(dc.getRoot().getLong(offset++), 0);
		setIxNRJact(dc.getRoot().getLong(offset++), 1);
		setNRJact_Reste(dc.getRoot().getInteger(offset++), 0);
		setNRJact_Reste(dc.getRoot().getInteger(offset++), 1);
		setIxNRJind(dc.getRoot().getLong(offset++), 0);
		setIxNRJind(dc.getRoot().getLong(offset++), 1);
		setNRJind_Reste(dc.getRoot().getInteger(offset++), 0);
		setNRJind_Reste(dc.getRoot().getInteger(offset++), 1);
		setIxNRJcap(dc.getRoot().getLong(offset++), 0);
		setIxNRJcap(dc.getRoot().getLong(offset++), 1);
		setNRJcap_Reste(dc.getRoot().getInteger(offset++), 0);
		setNRJcap_Reste(dc.getRoot().getInteger(offset++), 1);
	}
	
	public String toString(){
		StringBuffer strBuff = new StringBuffer();
		
		strBuff.append("*** Energies: ***\n");
		strBuff.append("	- DateDebutPeriode: " + getDebutPeriode());strBuff.append("\n");
		strBuff.append("	- DateDernierHoroDate: " + getDernierHoroDate());strBuff.append("\n");
		
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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

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
	public long getIxNRJact(int i) {
		return ixNRJact[i];
	}

	/**
	 * @param ixNRJact the ixNRJact to set
	 */
	public void setIxNRJact(long ixNRJact, int i) {
		this.ixNRJact[i] = ixNRJact;
	}

	/**
	 * @return the nRJact_Reste
	 */
	public int getNRJact_Reste(int i) {
		return nRJact_Reste[i];
	}

	/**
	 * @param jact_Reste the nRJact_Reste to set
	 */
	public void setNRJact_Reste(int jact_Reste, int i) {
		nRJact_Reste[i] = jact_Reste;
	}

	/**
	 * @return the ixNRJind
	 */
	public long getIxNRJind(int i) {
		return ixNRJind[i];
	}

	/**
	 * @param ixNRJind the ixNRJind to set
	 */
	public void setIxNRJind(long ixNRJind, int i) {
		this.ixNRJind[i] = ixNRJind;
	}

	/**
	 * @return the nRJind_Reste
	 */
	public int getNRJind_Reste(int i) {
		return nRJind_Reste[i];
	}

	/**
	 * @param jind_Reste the nRJind_Reste to set
	 */
	public void setNRJind_Reste(int jind_Reste, int i) {
		nRJind_Reste[i] = jind_Reste;
	}

	/**
	 * @return the ixNRJcap
	 */
	public long getIxNRJcap(int i) {
		return ixNRJcap[i];
	}

	/**
	 * @param ixNRJcap the ixNRJcap to set
	 */
	public void setIxNRJcap(long ixNRJcap, int i) {
		this.ixNRJcap[i] = ixNRJcap;
	}

	/**
	 * @return the nRJcap_Reste
	 */
	public int getNRJcap_Reste(int i) {
		return nRJcap_Reste[i];
	}

	/**
	 * @param jcap_Reste the nRJcap_Reste to set
	 */
	public void setNRJcap_Reste(int jcap_Reste, int i) {
		nRJcap_Reste[i] = jcap_Reste;
	}

	/**
	 * @param variableName the variableName to set
	 */
	public void setVariableName(int variableName) {
		this.variableName = variableName;
	}

}
