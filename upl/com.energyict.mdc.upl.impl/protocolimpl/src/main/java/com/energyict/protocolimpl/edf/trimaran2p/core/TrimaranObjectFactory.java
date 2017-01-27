/**
 * 
 */
package com.energyict.protocolimpl.edf.trimaran2p.core;

import com.energyict.protocolimpl.edf.trimaran2p.Trimaran2P;
import com.energyict.protocolimpl.edf.trimarandlms.common.DateType;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * @author gna
 *
 */
public class TrimaranObjectFactory {
	
	private int DEBUG = 0;
	
	private Parameters parameters = null;
	private ParametersPlus1 parametersPlus1 = null;
	private ParametersMoins1 parametersMoins1 = null;
	private TempsFonctionnement tempsFonctionnement = null;
	private EnergieIndex energieIndex = null;
	private ProgrammablesIndex programmablesIndex = null;
	private ArreteJournalier arreteJournalier = null;
	private ArreteProgrammables arreteProgrammables = null;
	private DureesPnonGarantie dureesPnonGarantie = null;
	private PMaxMois pMaxMois = null;
	private Trimaran2P trimaran;
	
	protected final int brute = 56;
	protected final int nette = 64;
	private final int jour = 120;
	private final int mois = 128;

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
	 * @return parameters
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
	
	public ParametersMoins1 readParametersMoins1() throws IOException{
		if(parametersMoins1 == null){
			parametersMoins1 = new ParametersMoins1(this);
			parametersMoins1.setVariableName_Moins1(168);
			parametersMoins1.read();
		}
		return parametersMoins1;
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
	
	public DureesPnonGarantie readDureesPnonGarantie() throws IOException{
		if(dureesPnonGarantie == null){
			dureesPnonGarantie = new DureesPnonGarantie(this);
			dureesPnonGarantie.setVariableName(118);
			dureesPnonGarantie.read();
		}
		return dureesPnonGarantie;
	}
	
	public PMaxMois readPMaxMois() throws IOException{
		if(pMaxMois == null){
			pMaxMois = new PMaxMois(this);
			pMaxMois.setVariableName(104);
			pMaxMois.read();
		}
		return pMaxMois;
	}
	
	public EnergieIndex readEnergieIndex() throws IOException{
		if(energieIndex == null){
			energieIndex = new EnergieIndex();
			energieIndex.addEnergie(readEnergieIndexReader(brute).getEnergie());
			if(getTrimaran().isTECMeter()){
				energieIndex.addEnergie(readEnergieIndexReader(nette).getEnergie());	// not with TEP
			}
		}
		return energieIndex;
	}
	
	public ProgrammablesIndex readProgrammablesIndex() throws IOException{
		if(programmablesIndex == null){
			programmablesIndex = new ProgrammablesIndex();
			programmablesIndex.addProgrammables(readProgrammablesIndexReader(jour).getProgrammables());
			programmablesIndex.addProgrammables(readProgrammablesIndexReader(mois).getProgrammables());
		}
		return programmablesIndex;
	}
	
	private ProgrammablesIndexReader readProgrammablesIndexReader(int variableName) throws IOException {
		ProgrammablesIndexReader pir = new ProgrammablesIndexReader(this);
		pir.setVariableName(variableName);
		pir.read();
		return pir;
	}

	protected EnergieIndexReader readEnergieIndexReader(int variableName) throws IOException{
		EnergieIndexReader eir = new EnergieIndexReader(this);
		eir.setVariableName(variableName);
		eir.read();
		return eir;
	}
	
	public ArreteProgrammables readArreteProgrammables() throws IOException{
		if(arreteProgrammables == null){
			arreteProgrammables = new ArreteProgrammables(this);
			arreteProgrammables.setVariableName(112);
			arreteProgrammables.read();
		}
		return arreteProgrammables;
	}
	
    public CourbeCharge getCourbeCharge(Date from, Date to) throws IOException {
        CourbeCharge cc = new CourbeCharge(this);
        cc.collect(from, to);
        return cc;
    }
	
    public AccessPartiel readAccessPartiel() throws IOException {
        AccessPartiel obj = new AccessPartiel(this);
        obj.read();
        return obj;
    }

    protected void writeAccessPartiel(Date dateAccess) throws IOException {
        AccessPartiel acp = new AccessPartiel(this);
        acp.setDateAccess(dateAccess);
        acp.setNomAccess(1);
        if (DEBUG>=1) {
			System.out.println("GN_DEBUG> AccesPartiel: " + acp.toString());
		}
        if (DEBUG>=1) {
			System.out.println("GN_DEBUG> The DateType: " + new DateType(acp.getDateAccess(), acp.getTrimaranObjectFactory().getTrimaran().getTimeZone()));
		}
        acp.write();
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
