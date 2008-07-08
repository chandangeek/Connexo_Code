/**
 * 
 */
package com.energyict.protocolimpl.edf.trimaran2p.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.edf.trimarandlms.axdr.DataContainer;
import com.energyict.protocolimpl.edf.trimarandlms.common.DateType;

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
		StringBuffer strBuff = new StringBuffer();
		
		strBuff.append(" *** PMaxMois: ***" + "\n");
		strBuff.append("	- Numbre mois: " + getNumMois() + "\n");
		
		strBuff.append("	- PApMax 1: " + getPApMax(0) + "\n");
		strBuff.append("	- PApMax 2: " + getPApMax(1) + "\n");
		strBuff.append("	- PApMax 3: " + getPApMax(2) + "\n");
		strBuff.append("	- PApMax 4: " + getPApMax(3) + "\n");
		strBuff.append("	- PApMax 5: " + getPApMax(4) + "\n");
		
		strBuff.append("	- PAnMax 1: " + getPAnMax(0) + "\n");
		strBuff.append("	- PAnMax 2: " + getPAnMax(1) + "\n");
		strBuff.append("	- PAnMax 3: " + getPAnMax(2) + "\n");
		strBuff.append("	- PAnMax 4: " + getPAnMax(3) + "\n");
		strBuff.append("	- PAnMax 5: " + getPAnMax(4) + "\n");
		
		strBuff.append("	- PRpMax 1: " + getPRpMax(0) + "\n");
		strBuff.append("	- PRpMax 2: " + getPRpMax(1) + "\n");
		strBuff.append("	- PRpMax 3: " + getPRpMax(2) + "\n");
		strBuff.append("	- PRpMax 4: " + getPRpMax(3) + "\n");
		strBuff.append("	- PRpMax 5: " + getPRpMax(4) + "\n");
		
		strBuff.append("	- PRnMax 1: " + getPRnMax(0) + "\n");
		strBuff.append("	- PRnMax 2: " + getPRnMax(1) + "\n");
		strBuff.append("	- PRnMax 3: " + getPRnMax(2) + "\n");
		strBuff.append("	- PRnMax 4: " + getPRnMax(3) + "\n");
		strBuff.append("	- PRnMax 5: " + getPRnMax(4) + "\n");
		
		strBuff.append(" 	- DatesPApMax 1: " + getDatesPApMax(0));
		strBuff.append(" 	- DatesPApMax 2: " + getDatesPApMax(1));
		strBuff.append(" 	- DatesPApMax 3: " + getDatesPApMax(2));
		strBuff.append(" 	- DatesPApMax 4: " + getDatesPApMax(3));
		strBuff.append(" 	- DatesPApMax 5: " + getDatesPApMax(4));
		
		strBuff.append(" 	- DatesPAnMax 1: " + getDatesPAnMax(0));
		strBuff.append(" 	- DatesPAnMax 2: " + getDatesPAnMax(1));
		strBuff.append(" 	- DatesPAnMax 3: " + getDatesPAnMax(2));
		strBuff.append(" 	- DatesPAnMax 4: " + getDatesPAnMax(3));
		strBuff.append(" 	- DatesPAnMax 5: " + getDatesPAnMax(4));
		
		strBuff.append(" 	- DatesPRpMax 1: " + getDatesPRpMax(0));
		strBuff.append(" 	- DatesPRpMax 2: " + getDatesPRpMax(1));
		strBuff.append(" 	- DatesPRpMax 3: " + getDatesPRpMax(2));
		strBuff.append(" 	- DatesPRpMax 4: " + getDatesPRpMax(3));
		strBuff.append(" 	- DatesPRpMax 5: " + getDatesPRpMax(4));
		
		strBuff.append(" 	- DatesPRnMax 1: " + getDatesPRnMax(0));
		strBuff.append(" 	- DatesPRnMax 2: " + getDatesPRnMax(1));
		strBuff.append(" 	- DatesPRnMax 3: " + getDatesPRnMax(2));
		strBuff.append(" 	- DatesPRnMax 4: " + getDatesPRnMax(3));
		strBuff.append(" 	- DatesPRnMax 5: " + getDatesPRnMax(4));
		
		return strBuff.toString();
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
		
//    	System.out.println("GN_DEBUG> write to file");
//    	File file = new File("c://TEST_FILES/089807000857PMaxMois.bin");
//    	FileOutputStream fos = new FileOutputStream(file);
//    	fos.write(data);
//    	fos.close();
		
		int offset = 0;
		DataContainer dc = new DataContainer();
		dc.parseObjectList(data, getTrimaranObjectFactory().getTrimaran().getLogger());
		
		setNumMois(dc.getRoot().getInteger(offset++));
		for(int i = 0; i < 5; i++)
			setPApMax(new Quantity(new BigDecimal(dc.getRoot().getStructure(offset).getLong(i)), Unit.get("kW")), i);
		offset++;
		for(int i = 0; i < 5; i++)
			setPAnMax(new Quantity(new BigDecimal(dc.getRoot().getStructure(offset).getLong(i)), Unit.get("kW")), i);
		offset++;
		for(int i = 0; i < 5; i++)
			setPRpMax(new Quantity(new BigDecimal(dc.getRoot().getStructure(offset).getLong(i)), Unit.get("kW")), i);
		offset++;
		for(int i = 0; i < 5; i++)
			setPRnMax(new Quantity(new BigDecimal(dc.getRoot().getStructure(offset).getLong(i)), Unit.get("kW")), i);
		offset++;
		
		for(int i = 0; i < 5; i++)
			setDatesPApMax(new DateType(dc.getRoot().getStructure(offset).getLong(i), getTrimaranObjectFactory().getTrimaran().getTimeZone()), i);
		offset++;
		for(int i = 0; i < 5; i++)
			setDatesPAnMax(new DateType(dc.getRoot().getStructure(offset).getLong(i), getTrimaranObjectFactory().getTrimaran().getTimeZone()), i);
		offset++;
		for(int i = 0; i < 5; i++)
			setDatesPRpMax(new DateType(dc.getRoot().getStructure(offset).getLong(i), getTrimaranObjectFactory().getTrimaran().getTimeZone()), i);
		offset++;
		for(int i = 0; i < 5; i++)
			setDatesPRnMax(new DateType(dc.getRoot().getStructure(offset).getLong(i), getTrimaranObjectFactory().getTrimaran().getTimeZone()), i);
		offset++;
	}

	@Override
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
