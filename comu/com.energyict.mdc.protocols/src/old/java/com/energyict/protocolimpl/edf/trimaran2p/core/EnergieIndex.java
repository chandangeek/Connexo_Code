/**
 *
 */
package com.energyict.protocolimpl.edf.trimaran2p.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author gna
 *
 */
public class EnergieIndex {

	List energies;

	/**
	 *
	 */
	public EnergieIndex() {
		setEnergies(new ArrayList());
	}

	private void setEnergies(ArrayList energies) {
		this.energies = energies;
	}

	public String toString(){
        StringBuffer strBuff = new StringBuffer();
        for(int i = 0; i < getEnergies().size(); i++) {
            Energies obj = (Energies)getEnergies().get(i);
            strBuff.append(obj+"\n");
        }
        return strBuff.toString();
	}

	private List getEnergies() {
		return this.energies;
	}

	public Energies getEnergie(int variableName) throws IOException{
        Iterator it = getEnergies().iterator();
        while(it.hasNext()) {
            Energies obj = (Energies)it.next();
            if (obj.getVariableName() == variableName){
            	return obj;
            }
        }
        throw new IOException("EnergieIndex, invalid variableName "+variableName);
	}

	public void addEnergie(Energies energie){
		getEnergies().add(energie);
	}

}
