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
public class ParametersMoins1 extends AbstractTrimaranObject{

	private DateType debutPeriode_168; 	// Date de dernière écriture de Paramètre
	private DateType dernierHoroDate; 	// Date en cours au moment de la lecture

	private int TC_Moins1;						// rapport de transformation de puissance, de 1 à 3000
	private int TT_Moins1;						// rapport de transformation de puissance, de 1 à 6000
	private int KJ_Moins1;						// valeur de coefficient de pertes joules, multiplié par 1000
	private int KPr_Moins1;					// valeur de coefficient utilisé pour le calcul de réactive positive en kvarh ramenés au primaire, multiplié par 1000, entre -500 et 500
	private Quantity KF_Moins1;				// valeur du paramètre pertes Fer, exprimé en W
	private int RL_Moins1;						// valeur de la résistance de perte Ligne signée multipliée par 100
	private int XL_Moins1;						// valeur de la r"actance de perte Ligne signée multipliée par 100
	private int kep_Moins1;					// Facteur d'échelle des puissances en puissance de 10

	private int tcc_Moins1;					// période d'intégration Tc pour le suivi de la courbe de charge en multiples de 1mn et selon un sous-multiple de 60

	private boolean ccReact_Moins1;			// enregistrement des quatre puissances réactives dans la courbe de charge

	private int variableName_Moins1;


	public ParametersMoins1(TrimaranObjectFactory trimaranObjectFactory) {
		super(trimaranObjectFactory);
	}

	protected int getVariableName() {
		return variableName_Moins1;
	}

	protected void parse(byte[] data) throws IOException {

		int offset = 0;
		TrimaranDataContainer dc = new TrimaranDataContainer();
		dc.parseObjectList(data, null);

		setDebutPeriode_168(new DateType(dc.getRoot().getLong(offset++), getTrimaranObjectFactory().getTrimaran().getTimeZone()));
		setDernierHoroDate(new DateType(dc.getRoot().getLong(offset++), getTrimaranObjectFactory().getTrimaran().getTimeZone()));

		setTC_Moins1(dc.getRoot().getInteger(offset++));
		setTT_Moins1(dc.getRoot().getInteger(offset++));
		setKJ_Moins1(dc.getRoot().getInteger(offset++));
		setKPr_Moins1(dc.getRoot().getInteger(offset++));
		setKF_Moins1(new Quantity(new BigDecimal(""+dc.getRoot().getInteger(offset++)), Unit.get("W")));
		setRL_Moins1(dc.getRoot().getInteger(offset++));
		setXL_Moins1(dc.getRoot().getInteger(offset++));
		setKep_Moins1(dc.getRoot().getInteger(offset++));
		setTcc_Moins1(dc.getRoot().getInteger(offset++));
		if(getTrimaranObjectFactory().getTrimaran().isTECMeter()){
			// Only with TEC meter
			if(dc.getRoot().isInteger(offset)){
				if (dc.getRoot().getInteger(offset) == 1){
					setCcReact_Moins1(true);
				} else {
					setCcReact_Moins1(false);
				}
			} else {
				setCcReact_Moins1(false);
			}
		}
	}

	protected byte[] prepareBuild() throws IOException {
		return null;
	}

	public void setVariableName_Moins1(int variableName_Moins1) {
		this.variableName_Moins1 = variableName_Moins1;
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
	 * @return the tC_Moins1
	 */
	public int getTC_Moins1() {
		return TC_Moins1;
	}

	/**
	 * @param moins1 the tC_Moins1 to set
	 */
	public void setTC_Moins1(int moins1) {
		TC_Moins1 = moins1;
	}

	/**
	 * @return the tT_Moins1
	 */
	public int getTT_Moins1() {
		return TT_Moins1;
	}

	/**
	 * @param moins1 the tT_Moins1 to set
	 */
	public void setTT_Moins1(int moins1) {
		TT_Moins1 = moins1;
	}

	/**
	 * @return the kJ_Moins1
	 */
	public int getKJ_Moins1() {
		return KJ_Moins1;
	}

	/**
	 * @param moins1 the kJ_Moins1 to set
	 */
	public void setKJ_Moins1(int moins1) {
		KJ_Moins1 = moins1;
	}

	/**
	 * @return the kPr_Moins1
	 */
	public int getKPr_Moins1() {
		return KPr_Moins1;
	}

	/**
	 * @param pr_Moins1 the kPr_Moins1 to set
	 */
	public void setKPr_Moins1(int pr_Moins1) {
		KPr_Moins1 = pr_Moins1;
	}

	/**
	 * @return the kF_Moins1
	 */
	public Quantity getKF_Moins1() {
		return KF_Moins1;
	}

	/**
	 * @param moins1 the kF_Moins1 to set
	 */
	public void setKF_Moins1(Quantity moins1) {
		KF_Moins1 = moins1;
	}

	/**
	 * @return the rL_Moins1
	 */
	public int getRL_Moins1() {
		return RL_Moins1;
	}

	/**
	 * @param moins1 the rL_Moins1 to set
	 */
	public void setRL_Moins1(int moins1) {
		RL_Moins1 = moins1;
	}

	/**
	 * @return the xL_Moins1
	 */
	public int getXL_Moins1() {
		return XL_Moins1;
	}

	/**
	 * @param moins1 the xL_Moins1 to set
	 */
	public void setXL_Moins1(int moins1) {
		XL_Moins1 = moins1;
	}

	/**
	 * @return the kep_Moins1
	 */
	public int getKep_Moins1() {
		return kep_Moins1;
	}

	/**
	 * @param kep_Moins1 the kep_Moins1 to set
	 */
	public void setKep_Moins1(int kep_Moins1) {
		this.kep_Moins1 = kep_Moins1;
	}

	/**
	 * @return the tcc_Moins1
	 */
	public int getTcc_Moins1() {
		return tcc_Moins1;
	}

	/**
	 * @param tcc_Moins1 the tcc_Moins1 to set
	 */
	public void setTcc_Moins1(int tcc_Moins1) {
		this.tcc_Moins1 = tcc_Moins1;
	}

	/**
	 * @return the ccReact_Moins1
	 */
	public boolean isCcReact_Moins1() {
		return ccReact_Moins1;
	}

	/**
	 * @param ccReact_Moins1 the ccReact_Moins1 to set
	 */
	public void setCcReact_Moins1(boolean ccReact_Moins1) {
		this.ccReact_Moins1 = ccReact_Moins1;
	}

	/**
	 * @return the variableName_Moins1
	 */
	public int getVariableName_Moins1() {
		return variableName_Moins1;
	}

	public String toString(){
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("*** ParametersMinus1: ***\n");
		strBuff.append("	- DateDebutPeriode_Moins1: " + getDebutPeriode_168());
		strBuff.append("	- DateFinPeriode_Moins1: " + getDernierHoroDate());
		strBuff.append("	- TC_Moins1: " + getTC_Moins1());strBuff.append("\n");
		strBuff.append("	- TT_Moins1: " + getTT_Moins1());strBuff.append("\n");
		strBuff.append("	- KJ_Moins1: " + getKJ_Moins1());strBuff.append("\n");
		strBuff.append("	- KPr_Moins1: " + getKPr_Moins1());strBuff.append("\n");
		strBuff.append("	- KF_Moins1: " + getKF_Moins1());strBuff.append("\n");
		strBuff.append("	- RL_Moins1: " + getRL_Moins1());strBuff.append("\n");
		strBuff.append("	- XL_Moins1: " + getXL_Moins1());strBuff.append("\n");
		strBuff.append("	- kep_Moins1: " + getKep_Moins1());strBuff.append("\n");
		strBuff.append("	- tCourbeCharge_Moins1: " + getTcc_Moins1());strBuff.append("\n");
		if(isCcReact_Moins1()){
			strBuff.append("	- ccReact_Moins1: true");strBuff.append("\n");
		}
		else{
			strBuff.append("	- ccReact_Moins1: false");strBuff.append("\n");
		}
		return strBuff.toString();
	}

}
