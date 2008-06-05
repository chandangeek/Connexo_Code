/**
 * 
 */
package com.energyict.protocolimpl.edf.trimaran2p.core;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.TimeZone;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.edf.trimarandlms.axdr.DataContainer;
import com.energyict.protocolimpl.edf.trimarandlms.common.DateType;

/**
 * @author gna
 *
 */
public class ParametersMinus1 extends AbstractTrimaranObject{
	
	private DateType debutPeriode_168; 	// Date de dernière écriture de Paramètre
	private DateType dernierHoroDate; 	// Date en cours au moment de la lecture
	
	private int TC_Minus1;						// rapport de transformation de puissance, de 1 à 3000
	private int TT_Minus1;						// rapport de transformation de puissance, de 1 à 6000
	private int KJ_Minus1;						// valeur de coefficient de pertes joules, multiplié par 1000
	private int KPr_Minus1;					// valeur de coefficient utilisé pour le calcul de réactive positive en kvarh ramenés au primaire, multiplié par 1000, entre -500 et 500
	private Quantity KF_Minus1;				// valeur du paramètre pertes Fer, exprimé en W
	private int RL_Minus1;						// valeur de la résistance de perte Ligne signée multipliée par 100
	private int XL_Minus1;						// valeur de la r"actance de perte Ligne signée multipliée par 100
	private int kep_Minus1;					// Facteur d'échelle des puissances en puissance de 10

	private int tcc_Minus1;					// période d'intégration Tc pour le suivi de la courbe de charge en multiples de 1mn et selon un sous-multiple de 60
	
	private boolean ccReact_Minus1;			// enregistrement des quatre puissances réactives dans la courbe de charge
	
	private int variableName_Minus1;


	public ParametersMinus1(TrimaranObjectFactory trimaranObjectFactory) {
		super(trimaranObjectFactory);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	protected int getVariableName() {
		return variableName_Minus1;
	}

	@Override
	protected void parse(byte[] data) throws IOException {
		int offset = 0;
		DataContainer dc = new DataContainer();
		dc.parseObjectList(data, null);
		
		setDebutPeriode_168(new DateType(dc.getRoot().getLong(offset++), getTrimaranObjectFactory().getTrimaran().getTimeZone()));
		setDernierHoroDate(new DateType(dc.getRoot().getLong(offset++), getTrimaranObjectFactory().getTrimaran().getTimeZone()));
		
		setTC_Minus1(dc.getRoot().getInteger(offset++));
		setTT_Minus1(dc.getRoot().getInteger(offset++));
		setKJ_Minus1(dc.getRoot().getInteger(offset++));
		setKPr_Minus1(dc.getRoot().getInteger(offset++));
		setKF_Minus1(new Quantity(new BigDecimal(""+dc.getRoot().getInteger(offset++)), Unit.get("W")));
		setRL_Minus1(dc.getRoot().getInteger(offset++));
		setXL_Minus1(dc.getRoot().getInteger(offset++));
		setKep_Minus1(dc.getRoot().getInteger(offset++));
		setTcc_Minus1(dc.getRoot().getInteger(offset++));
		if (dc.getRoot().getInteger(offset) == 1)
			setCcReact_Minus1(true);
		else
			setCcReact_Minus1(false);		
	}

	@Override
	protected byte[] prepareBuild() throws IOException {
		return null;
	}

	public void setVariableName_Minus1(int variableName_Minus1) {
		this.variableName_Minus1 = variableName_Minus1;
	}

	/**
	 * @return the debutPeriode_168
	 */
	public DateType getDebutPeriode_168() {
		return debutPeriode_168;
	}

	/**
	 * @param debutPeriode_168 the debutPeriode_168 to set
	 */
	public void setDebutPeriode_168(DateType debutPeriode_168) {
		this.debutPeriode_168 = debutPeriode_168;
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
	 * @return the tC_Minus1
	 */
	public int getTC_Minus1() {
		return TC_Minus1;
	}

	/**
	 * @param minus1 the tC_Minus1 to set
	 */
	public void setTC_Minus1(int minus1) {
		TC_Minus1 = minus1;
	}

	/**
	 * @return the tT_Minus1
	 */
	public int getTT_Minus1() {
		return TT_Minus1;
	}

	/**
	 * @param minus1 the tT_Minus1 to set
	 */
	public void setTT_Minus1(int minus1) {
		TT_Minus1 = minus1;
	}

	/**
	 * @return the kJ_Minus1
	 */
	public int getKJ_Minus1() {
		return KJ_Minus1;
	}

	/**
	 * @param minus1 the kJ_Minus1 to set
	 */
	public void setKJ_Minus1(int minus1) {
		KJ_Minus1 = minus1;
	}

	/**
	 * @return the kPr_Minus1
	 */
	public int getKPr_Minus1() {
		return KPr_Minus1;
	}

	/**
	 * @param pr_Minus1 the kPr_Minus1 to set
	 */
	public void setKPr_Minus1(int pr_Minus1) {
		KPr_Minus1 = pr_Minus1;
	}

	/**
	 * @return the kF_Minus1
	 */
	public Quantity getKF_Minus1() {
		return KF_Minus1;
	}

	/**
	 * @param minus1 the kF_Minus1 to set
	 */
	public void setKF_Minus1(Quantity minus1) {
		KF_Minus1 = minus1;
	}

	/**
	 * @return the rL_Minus1
	 */
	public int getRL_Minus1() {
		return RL_Minus1;
	}

	/**
	 * @param minus1 the rL_Minus1 to set
	 */
	public void setRL_Minus1(int minus1) {
		RL_Minus1 = minus1;
	}

	/**
	 * @return the xL_Minus1
	 */
	public int getXL_Minus1() {
		return XL_Minus1;
	}

	/**
	 * @param minus1 the xL_Minus1 to set
	 */
	public void setXL_Minus1(int minus1) {
		XL_Minus1 = minus1;
	}

	/**
	 * @return the kep_Minus1
	 */
	public int getKep_Minus1() {
		return kep_Minus1;
	}

	/**
	 * @param kep_Minus1 the kep_Minus1 to set
	 */
	public void setKep_Minus1(int kep_Minus1) {
		this.kep_Minus1 = kep_Minus1;
	}

	/**
	 * @return the tcc_Minus1
	 */
	public int getTcc_Minus1() {
		return tcc_Minus1;
	}

	/**
	 * @param tcc_Minus1 the tcc_Minus1 to set
	 */
	public void setTcc_Minus1(int tcc_Minus1) {
		this.tcc_Minus1 = tcc_Minus1;
	}

	/**
	 * @return the ccReact_Minus1
	 */
	public boolean isCcReact_Minus1() {
		return ccReact_Minus1;
	}

	/**
	 * @param ccReact_Minus1 the ccReact_Minus1 to set
	 */
	public void setCcReact_Minus1(boolean ccReact_Minus1) {
		this.ccReact_Minus1 = ccReact_Minus1;
	}

	/**
	 * @return the variableName_Minus1
	 */
	public int getVariableName_Minus1() {
		return variableName_Minus1;
	}
	
	public String toString(){
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("*** ParametersMinus1: ***\n");
		strBuff.append("	- DateDebutPeriode_Minus1: " + getDebutPeriode_168());
		strBuff.append("	- DateFinPeriode_Minus1: " + getDernierHoroDate());
		strBuff.append("	- TC_Minus1: " + getTC_Minus1());strBuff.append("\n");
		strBuff.append("	- TT_Minus1: " + getTT_Minus1());strBuff.append("\n");
		strBuff.append("	- KJ_Minus1: " + getKJ_Minus1());strBuff.append("\n");
		strBuff.append("	- KPr_Minus1: " + getKPr_Minus1());strBuff.append("\n");
		strBuff.append("	- KF_Minus1: " + getKF_Minus1());strBuff.append("\n");
		strBuff.append("	- RL_Minus1: " + getRL_Minus1());strBuff.append("\n");
		strBuff.append("	- XL_Minus1: " + getXL_Minus1());strBuff.append("\n");
		strBuff.append("	- kep_Minus1: " + getKep_Minus1());strBuff.append("\n");
		strBuff.append("	- tCourbeCharge_Minus1: " + getTcc_Minus1());strBuff.append("\n");
		if(isCcReact_Minus1()){
			strBuff.append("	- ccReact_Minus1: true");strBuff.append("\n");
		}
		else{
			strBuff.append("	- ccReact_Minus1: false");strBuff.append("\n");
		}
		return strBuff.toString();
	}

}
