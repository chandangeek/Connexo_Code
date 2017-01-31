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

import java.io.IOException;
import java.math.BigDecimal;

/**
 * @author gna
 *
 */
public class ParametersPlus1 extends AbstractTrimaranObject{

	private int KJ_plus1;					// valeur de coefficient de pertes joules, multiplié par 1000
	private int KPr_plus1;					// valeur de coefficient utilisé pour le calcul de réactive positive en kvarh ramenés au primaire, multiplié par 1000, entre -500 et 500
	private Quantity KF_plus1;				// valeur du paramètre pertes Fer, exprimé en W
	private int RL_plus1;					// valeur de la résistance de perte Ligne signée multipliée par 100
	private int XL_plus1;					// valeur de la r"actance de perte Ligne signée multipliée par 100
	private int kep_plus1;					// Facteur d'échelle des puissances en puissance de 10

	private int tcc_plus1;					// période d'intégration Tc pour le suivi de la courbe de charge en multiples de 1mn et selon un sous-multiple de 60

	private boolean ccReact_plus1;			// enregistrement des quatre puissances réactives dans la courbe de charge

	private int variableName_plus1;

	/**
	 *
	 */
	public ParametersPlus1(TrimaranObjectFactory trimaranObjectFacotry) {
		super(trimaranObjectFacotry);
	}

	protected int getVariableName() {
		return variableName_plus1;
	}

	protected void parse(byte[] data) throws IOException {
		int offset = 0;
		TrimaranDataContainer dc = new TrimaranDataContainer();
		dc.parseObjectList(data, null);
		setKJ_plus1(dc.getRoot().getInteger(offset++));
		setKPr_plus1(dc.getRoot().getInteger(offset++));
		setKF_plus1(new Quantity(new BigDecimal(""+dc.getRoot().getInteger(offset++)), Unit.get("W")));
		setRL_plus1(dc.getRoot().getInteger(offset++));
		setXL_plus1(dc.getRoot().getInteger(offset++));
		setKep_plus1(dc.getRoot().getInteger(offset++));
		setTcc_plus1(dc.getRoot().getInteger(offset++));
		if(getTrimaranObjectFactory().getTrimaran().isTECMeter()){
			// Only with TEC meter
			if(dc.getRoot().isInteger(offset)){
				if (dc.getRoot().getInteger(offset) == 1){
					setCcReact_plus1(true);
				} else {
					setCcReact_plus1(false);
				}
			} else {
				setCcReact_plus1(false);
			}
		}
	}

	protected byte[] prepareBuild() throws IOException {
		return null;
	}

	/**
	 * @return the kJ_plus1
	 */
	public int getKJ_plus1() {
		return KJ_plus1;
	}

	/**
	 * @param kj_plus1 the kJ_plus1 to set
	 */
	public void setKJ_plus1(int kj_plus1) {
		KJ_plus1 = kj_plus1;
	}

	/**
	 * @return the kPr_plus1
	 */
	public int getKPr_plus1() {
		return KPr_plus1;
	}

	/**
	 * @param pr_plus1 the kPr_plus1 to set
	 */
	public void setKPr_plus1(int pr_plus1) {
		KPr_plus1 = pr_plus1;
	}

	/**
	 * @return the kF_plus1
	 */
	public Quantity getKF_plus1() {
		return KF_plus1;
	}

	/**
	 * @param kf_plus1 the kF_plus1 to set
	 */
	public void setKF_plus1(Quantity kf_plus1) {
		KF_plus1 = kf_plus1;
	}

	/**
	 * @return the rL_plus1
	 */
	public int getRL_plus1() {
		return RL_plus1;
	}

	/**
	 * @param rl_plus1 the rL_plus1 to set
	 */
	public void setRL_plus1(int rl_plus1) {
		RL_plus1 = rl_plus1;
	}

	/**
	 * @return the xL_plus1
	 */
	public int getXL_plus1() {
		return XL_plus1;
	}

	/**
	 * @param xl_plus1 the xL_plus1 to set
	 */
	public void setXL_plus1(int xl_plus1) {
		XL_plus1 = xl_plus1;
	}

	/**
	 * @return the kep_plus1
	 */
	public int getKep_plus1() {
		return kep_plus1;
	}

	/**
	 * @param kep_plus1 the kep_plus1 to set
	 */
	public void setKep_plus1(int kep_plus1) {
		this.kep_plus1 = kep_plus1;
	}

	/**
	 * @return the tcc_plus1
	 */
	public int getTcc_plus1() {
		return tcc_plus1;
	}

	/**
	 * @param tcc_plus1 the tcc_plus1 to set
	 */
	public void setTcc_plus1(int tcc_plus1) {
		this.tcc_plus1 = tcc_plus1;
	}

	/**
	 * @return the ccReact_plus1
	 */
	public boolean isCcReact_plus1() {
		return ccReact_plus1;
	}

	/**
	 * @param ccReact_plus1 the ccReact_plus1 to set
	 */
	public void setCcReact_plus1(boolean ccReact_plus1) {
		this.ccReact_plus1 = ccReact_plus1;
	}

	/**
	 * @param variableName_plus1 the variableName_plus1 to set
	 */
	public void setVariableName_plus1(int variableName_plus1) {
		this.variableName_plus1 = variableName_plus1;
	}

	public String toString(){
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("*** ParametersPlus1: ***\n");
		strBuff.append("	- KJ_plus1: " + getKJ_plus1());strBuff.append("\n");
		strBuff.append("	- KPr_plus1: " + getKPr_plus1());strBuff.append("\n");
		strBuff.append("	- KF_plus1: " + getKF_plus1());strBuff.append("\n");
		strBuff.append("	- RL_plus1: " + getRL_plus1());strBuff.append("\n");
		strBuff.append("	- XL_plus1: " + getXL_plus1());strBuff.append("\n");
		strBuff.append("	- kep_plus1: " + getKep_plus1());strBuff.append("\n");
		strBuff.append("	- tCourbeCharge_plus1: " + getTcc_plus1());strBuff.append("\n");
		if(isCcReact_plus1()){
			strBuff.append("	- ccReact_plus1: true");strBuff.append("\n");
		}
		else{
			strBuff.append("	- ccReact_plus1: false");strBuff.append("\n");
		}
		return strBuff.toString();
	}

}
