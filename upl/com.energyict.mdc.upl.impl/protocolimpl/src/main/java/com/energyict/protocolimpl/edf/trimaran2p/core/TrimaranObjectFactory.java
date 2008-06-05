/**
 * 
 */
package com.energyict.protocolimpl.edf.trimaran2p.core;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.edf.trimaran2p.Trimaran2P;
import com.energyict.protocolimpl.edf.trimarandlms.common.DateType;

/**
 * @author gna
 *
 */
public class TrimaranObjectFactory {
	
	private int DEBUG = 0;
	
	private Parameters parameters = null;
	private ParametersPlus1 parametersPlus1 = null;
	private ParametersMinus1 parametersMinus1 = null;
	private TempsFonctionnement tempsFonctionnement = null;
	private EnergieIndex energieIndex = null;
	private ArreteJournalier arreteJournalier = null;
	private Trimaran2P trimaran;
	
	private final int brute = 56;
	private final int nette = 64;

	/**
	 * 
	 */
	public TrimaranObjectFactory(Trimaran2P trimaran2P) {
		setTrimaran(trimaran2P);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	/**
	 * @return the trimaran
	 */
	protected Trimaran2P getTrimaran() {
		return trimaran;
	}

	/**
	 * @param trimaran the trimaran to set
	 */
	protected void setTrimaran(Trimaran2P trimaran) {
		this.trimaran = trimaran;
	}
	
	/**
	 * @param
	 * @return 
	 * @throws IOException 
	 */
	public Parameters readParameters() throws IOException{
		if(parameters == null){
			parameters = new Parameters(this);
			parameters.setVariableName(48);
			parameters.read();
		}
		return parameters;
	}
	
	public ParametersPlus1 readParametersPlus1() throws IOException{
		if(parametersPlus1 == null){
			parametersPlus1 = new ParametersPlus1(this);
			parametersPlus1.setVariableName_plus1(40);
			parametersPlus1.read();
		}
		return parametersPlus1;
	}
	
	public ParametersMinus1 readParametersMinus1() throws IOException{
		if(parametersMinus1 == null){
			parametersMinus1 = new ParametersMinus1(this);
			parametersMinus1.setVariableName_Minus1(168);
			parametersMinus1.read();
		}
		return parametersMinus1;
	}
	
	public TempsFonctionnement readTempsFonctionnement() throws IOException{
		if(tempsFonctionnement == null){
			tempsFonctionnement = new TempsFonctionnement(this);
			tempsFonctionnement.setVariableName(152);
			tempsFonctionnement.read();
		}
		return tempsFonctionnement;
	}
	
	public ArreteJournalier readArreteJournalier() throws IOException{
		if(arreteJournalier == null){
			arreteJournalier = new ArreteJournalier(this);
			arreteJournalier.setVariableName(104);
			arreteJournalier.read();
		}
		return arreteJournalier;
	}
	
	public EnergieIndex readEnergieIndex() throws IOException{
		if(energieIndex == null){
			energieIndex = new EnergieIndex();
			energieIndex.addEnergie(readEnergieIndexReader(brute).getEnergie());
			energieIndex.addEnergie(readEnergieIndexReader(nette).getEnergie());
		}
		return energieIndex;
	}
	
	private EnergieIndexReader readEnergieIndexReader(int variableName) throws IOException{
		EnergieIndexReader eir = new EnergieIndexReader(this);
		eir.setVariableName(variableName);
		eir.read();
		return eir;
	}
	
    public CourbeCharge getCourbeCharge(Date from) throws IOException {
        CourbeCharge cc = new CourbeCharge(this);
        cc.collect(from);
        return cc;
    }
	
    public AccessPartiel readAccessPartiel() throws IOException {
        AccessPartiel obj = new AccessPartiel(this);
        obj.read();
        return obj;
    }

    protected void writeAccessPartiel(Date dateAccess) throws IOException {
        AccessPartiel obj = new AccessPartiel(this);
        obj.setDateAccess(dateAccess);
        obj.setNomAccess(1);
        if (DEBUG>=1) System.out.println("GN_DEBUG> AccesPartiel: " + obj.toString());
        if (DEBUG>=1) System.out.println("GN_DEBUG> The DateType: " + new DateType(obj.getDateAccess(), obj.getTrimaranObjectFactory().getTrimaran().getTimeZone()));
        obj.write();
    }
    
    protected CourbeChargePartielle1 getCourbeChargePartielle1() throws IOException {
        CourbeChargePartielle1 ccp1 = new CourbeChargePartielle1(this);
        ccp1.read();
        return ccp1;
    }
    
    protected CourbeChargePartielle2 getCourbeChargePartielle2() throws IOException {
        CourbeChargePartielle2 ccp2 = new CourbeChargePartielle2(this);
        ccp2.read();
        return ccp2;
    }
    
    protected CourbeChargePartielle getCourbeChargePartielle() throws IOException{
    	CourbeChargePartielle ccp = new CourbeChargePartielle(this);
    	ccp.read();
    	return ccp;
    }
    
    protected void writeAccessPartiel(int nr) throws IOException {
        Calendar cal = ProtocolUtils.getCleanCalendar(getTrimaran().getTimeZone());
        cal.set(Calendar.YEAR,1992);
        cal.set(Calendar.MONTH,0);
        cal.set(Calendar.DAY_OF_MONTH,1);
        cal.set(Calendar.HOUR_OF_DAY,0);
        int num = nr;
        int M = num / 6000;
        num -= M * 6000;
        int S = num / 100;
        int C = num % 100;        
        cal.set(Calendar.MINUTE,M);
        cal.set(Calendar.SECOND,S);
        cal.set(Calendar.MILLISECOND,C*10);
        AccessPartiel obj = new AccessPartiel(this);
        obj.setDateAccess(cal.getTime());
        obj.setNomAccess(0x01);
        obj.write();
    }

	/**
	 * @param parameters the parameters to set
	 */
	protected void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return the parameters
	 */
	protected Parameters getParameters() {
		return parameters;
	}

}
