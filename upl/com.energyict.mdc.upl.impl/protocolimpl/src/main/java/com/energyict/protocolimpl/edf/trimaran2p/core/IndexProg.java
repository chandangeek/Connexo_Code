/**
 * 
 */
package com.energyict.protocolimpl.edf.trimaran2p.core;

import java.io.IOException;

import com.energyict.protocolimpl.edf.trimarandlms.axdr.DataContainer;
import com.energyict.protocolimpl.edf.trimarandlms.common.DateType;

/**
 * @author gna
 *
 */
public class IndexProg extends AbstractTrimaranObject{
	
	private int variableName;
	
	private DateType debutPeriode;			// Date du jour à 00h de la derniére mise à zero de l'index NJ ou date d'initialisation si elle est postérieure
	private int nombre;						// Périodicité en nombre de jours pour arrêter l'index
	private DateType dernierHorodate;		// Date en cours au moment de la lecture
	private long[] ixProg;					// Index courants des énergies EacpNJ, EacnNJ en kWh, Erc1NJ, Erc3NJ, Erc2NJ, Erc4NJ en kvarh
	private long[] ixProgMoins1;			// Index courants des énergies EacpNJ-1, EacnNJ-1 en kWh, Erc1NJ-1, Erc3NJ-1, Erc2NJ-1, Erc4NJ-1 en kvarh

	/**
	 * 
	 */
	public IndexProg(TrimaranObjectFactory trimaranObjectFactory) {
		super(trimaranObjectFactory);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	@Override
	protected int getVariableName() {
		return variableName;
	}

	@Override
	protected void parse(byte[] data) throws IOException {
		int offset = 0;
		DataContainer dc = new DataContainer();
		dc.parseObjectList(data, getTrimaranObjectFactory().getTrimaran().getLogger());
		
		setDebutPeriode(new DateType(dc.getRoot().getLong(offset++), getTrimaranObjectFactory().getTrimaran().getTimeZone()));
		setNombre(dc.getRoot().getInteger(offset++));
		setDernierHorodate(new DateType(dc.getRoot().getLong(offset++), getTrimaranObjectFactory().getTrimaran().getTimeZone()));
		for(int i = 0; i < 6; i++)
			setIxProg(dc.getRoot().getStructure(offset).getLong(i), i);
		offset++;
		for(int i = 0; i < 6; i++)
			setIxProgMoins1(dc.getRoot().getStructure(offset).getLong(i), i);
		offset++;
		
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

	@Override
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
	 * @return the dernierHorodate
	 */
	public DateType getDernierHorodate() {
		return dernierHorodate;
	}

	/**
	 * @param dernierHorodate the dernierHorodate to set
	 */
	public void setDernierHorodate(DateType dernierHorodate) {
		this.dernierHorodate = dernierHorodate;
	}

	/**
	 * @return the ixProg
	 */
	public long getIxProg(int i) {
		return ixProg[i];
	}

	/**
	 * @param ixProg the ixProg to set
	 */
	public void setIxProg(long ixProg, int i) {
		this.ixProg[i] = ixProg;
	}

	/**
	 * @return the ixProgMoins1
	 */
	public long getIxProgMoins1(int i) {
		return ixProgMoins1[i];
	}

	/**
	 * @param ixProgMoins1 the ixProgMoins1 to set
	 */
	public void setIxProgMoins1(long ixProgMoins1, int i) {
		this.ixProgMoins1[i] = ixProgMoins1;
	}

	/**
	 * @param variableName the variableName to set
	 */
	public void setVariableName(int variableName) {
		this.variableName = variableName;
	}

}
