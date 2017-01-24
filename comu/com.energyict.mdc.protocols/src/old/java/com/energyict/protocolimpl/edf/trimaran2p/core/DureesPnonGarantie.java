/**
 *
 */
package com.energyict.protocolimpl.edf.trimaran2p.core;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.edf.trimarandlms.axdr.TrimaranDataContainer;
import com.energyict.protocolimpl.edf.trimarandlms.common.DateType;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * @author gna
 *
 */
public class DureesPnonGarantie extends AbstractTrimaranObject{

	private int variableName;

	private Quantity[] puissancesGaranties = {null, null};	// valeurs des puissances produites garanties minimale et maximale en kW
	private DateType dateProg;								// dernière date de programmation de PGmin ou PGmax
	private Quantity[] DnG = {null,null,null,null,null,null,null,null,null,null,null,null};
					// durée en minute de puissance active produite non garantie par mois

	/**
	 *
	 */
	public DureesPnonGarantie(TrimaranObjectFactory trimaranObjectFactory) {
		super(trimaranObjectFactory);
	}

	public String toString(){
		StringBuffer strBuff = new StringBuffer();

		strBuff.append("*** Durees P non Garantie: ***\n");
		strBuff.append("	- PuissancesGaranties Max: " + getPuissancesGaranties(0) + "\n");
		strBuff.append("	- PuissancesGaranties Min: " + getPuissancesGaranties(1) + "\n");
		strBuff.append("	- Date de programmation: " + getDateProg());
		strBuff.append("	- DnG current: " + getDnG(0) + "\n");
		strBuff.append("	- DnG previous: " + getDnG(1) + "\n");
		strBuff.append("	- DnG previous: " + getDnG(2) + "\n");
		strBuff.append("	- DnG previous: " + getDnG(3) + "\n");
		strBuff.append("	- DnG previous: " + getDnG(4) + "\n");
		strBuff.append("	- DnG previous: " + getDnG(5) + "\n");
		strBuff.append("	- DnG previous: " + getDnG(6) + "\n");
		strBuff.append("	- DnG previous: " + getDnG(7) + "\n");
		strBuff.append("	- DnG previous: " + getDnG(8) + "\n");
		strBuff.append("	- DnG previous: " + getDnG(9) + "\n");
		strBuff.append("	- DnG previous: " + getDnG(10) + "\n");
		strBuff.append("	- DnG previous: " + getDnG(11) + "\n");

		return strBuff.toString();
	}

	protected int getVariableName() {
		return variableName;
	}

	protected void parse(byte[] data) throws IOException {
		int offset = 0;
		TrimaranDataContainer dc = new TrimaranDataContainer();
		dc.parseObjectList(data, getTrimaranObjectFactory().getTrimaran().getLogger());

		setPuissancesGaranties(new Quantity(new BigDecimal(dc.getRoot().getStructure(offset).getInteger(0)), Unit.get("kW")), 0);
		setPuissancesGaranties(new Quantity(new BigDecimal(dc.getRoot().getStructure(offset++).getInteger(1)), Unit.get("kW")), 1);

		setDateProg(new DateType(dc.getRoot().getLong(offset++), getTrimaranObjectFactory().getTrimaran().getTimeZone()));

		for(int i = 0; i < 11; i++){	// the doc says this structure has 12 elements ... but it doesn't say that it has to be 12 ...
			if(dc.getRoot().getStructure(offset).isLong(i)){
				setDnG(new Quantity(new BigDecimal(dc.getRoot().getStructure(offset).getLong(i)), Unit.get("min")), i);
			}
		}
	}

	protected byte[] prepareBuild() throws IOException {
		return null;
	}

	/**
	 * @return the puissancesGaranties
	 */
	protected Quantity getPuissancesGaranties(int i) {
		return puissancesGaranties[i];
	}

	/**
	 * @param puissancesGaranties the puissancesGaranties to set
	 */
	protected void setPuissancesGaranties(Quantity puissancesGaranties, int i) {
		this.puissancesGaranties[i] = puissancesGaranties;
	}

	/**
	 * @return the dateProg
	 */
	protected DateType getDateProg() {
		return dateProg;
	}

	/**
	 * @param dateProg the dateProg to set
	 */
	protected void setDateProg(DateType dateProg) {
		this.dateProg = dateProg;
	}

	/**
	 * @return the dnG
	 */
	protected Quantity getDnG(int i) {
		return DnG[i];
	}

	/**
	 * @param dnG the dnG to set
	 */
	protected void setDnG(Quantity dnG, int i) {
		DnG[i] = dnG;
	}

	/**
	 * @param variableName the variableName to set
	 */
	protected void setVariableName(int variableName) {
		this.variableName = variableName;
	}

}
