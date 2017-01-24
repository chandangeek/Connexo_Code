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
public class Parameters extends AbstractTrimaranObject {


	private DateType debutPeriode_48; 	// Date de dernière écriture de Paramètre
	private DateType dernierHoroDate; 	// Date en cours au moment de la lecture

	private int TC;						// rapport de transformation de puissance, de 1 à 3000
	private int TT;						// rapport de transformation de puissance, de 1 à 6000
	private int KJ;						// valeur de coefficient de pertes joules, multiplié par 1000
	private int KPr;					// valeur de coefficient utilisé pour le calcul de réactive positive en kvarh ramenés au primaire, multiplié par 1000, entre -500 et 500
	private Quantity KF;				// valeur du paramètre pertes Fer, exprimé en W
	private int RL;						// valeur de la résistance de perte Ligne signée multipliée par 100
	private int XL;						// valeur de la r"actance de perte Ligne signée multipliée par 100
	private int kep;					// Facteur d'échelle des puissances en puissance de 10

	private int tcc;					// période d'intégration Tc pour le suivi de la courbe de charge en multiples de 1mn et selon un sous-multiple de 60

	private boolean ccReact;			// enregistrement des quatre puissances réactives dans la courbe de charge

	private int variableName;

	/**
	 *
	 */
	public Parameters(TrimaranObjectFactory trimaranObjectFactory) {
		super(trimaranObjectFactory);
	}

	protected int getVariableName() {
		return variableName;
	}

	protected void parse(byte[] data) throws IOException {
//
//   	System.out.println("GN_DEBUG> write to file");
//    	File file = new File("c://TEST_FILES/089807000857Parameters.bin");
//    	FileOutputStream fos = new FileOutputStream(file);
//    	fos.write(data);
//    	fos.close();

		int offset = 0;
		TrimaranDataContainer dc = new TrimaranDataContainer();
		dc.parseObjectList(data, null);

		setDebutPeriode_48(new DateType(dc.getRoot().getLong(offset++), getTrimaranObjectFactory().getTrimaran().getTimeZone()));
		setDernierHoroDate(new DateType(dc.getRoot().getLong(offset++), getTrimaranObjectFactory().getTrimaran().getTimeZone()));
		getTrimaranObjectFactory().getTrimaran().setRoundTripStart(System.currentTimeMillis());

		setTC(dc.getRoot().getInteger(offset++));
		setTT(dc.getRoot().getInteger(offset++));
		setKJ(dc.getRoot().getInteger(offset++));
		setKPr(dc.getRoot().getInteger(offset++));
		setKF(new Quantity(new BigDecimal(""+dc.getRoot().getInteger(offset++)), Unit.get("W")));
		setRL(dc.getRoot().getInteger(offset++));
		setXL(dc.getRoot().getInteger(offset++));
		setKep(dc.getRoot().getInteger(offset++));
		setTCourbeCharge(dc.getRoot().getInteger(offset++));
		if(getTrimaranObjectFactory().getTrimaran().isTECMeter()){
			// Only with TEC meter
			if(dc.getRoot().isInteger(offset)){
				if (dc.getRoot().getInteger(offset) == 1){
					setCcReact(true);
				} else {
					setCcReact(false);
				}
			} else {
				setCcReact(false);
			}
		}
	}

	protected byte[] prepareBuild() throws IOException {
		return null;
	}

	public void setVariableName(int variableName) {
		this.variableName = variableName;
	}

	/**
	 * @return the debutPeriode_48
	 */
	public DateType getDebutPeriode_48() {
		return debutPeriode_48;
	}

	/**
	 * @param debutPeriode_48 the dateDebutPeriode to set
	 */
	public void setDebutPeriode_48(DateType debutPeriode_48) {
		this.debutPeriode_48 = debutPeriode_48;
	}

	/**
	 * @return the dernierHoroDate
	 */
	public DateType getDernierHoroDate() {
		return dernierHoroDate;
	}

	/**
	 * @param dernierHoroDate the dateFinPeriode to set
	 */
	public void setDernierHoroDate(DateType dernierHoroDate) {
		this.dernierHoroDate = dernierHoroDate;
	}

	/**
	 * @return the tC
	 */
	public int getTC() {
		return TC;
	}

	/**
	 * @param tc the tC to set
	 */
	public void setTC(int tc) {
		TC = tc;
	}

	/**
	 * @return the tT
	 */
	public int getTT() {
		return TT;
	}

	/**
	 * @param tt the tT to set
	 */
	public void setTT(int tt) {
		TT = tt;
	}

	/**
	 * @return the kJ
	 */
	public int getKJ() {
		return KJ;
	}

	/**
	 * @param kj the kJ to set
	 */
	public void setKJ(int kj) {
		KJ = kj;
	}

	/**
	 * @return the kPr
	 */
	public int getKPr() {
		return KPr;
	}

	/**
	 * @param pr the kPr to set
	 */
	public void setKPr(int pr) {
		KPr = pr;
	}

	/**
	 * @return the kF
	 */
	public Quantity getKF() {
		return KF;
	}

	/**
	 * @param kf the kF to set
	 */
	public void setKF(Quantity kf) {
		KF = kf;
	}

	/**
	 * @return the rL
	 */
	public int getRL() {
		return RL;
	}

	/**
	 * @param rl the rL to set
	 */
	public void setRL(int rl) {
		RL = rl;
	}

	/**
	 * @return the xL
	 */
	public int getXL() {
		return XL;
	}

	/**
	 * @param xl the xL to set
	 */
	public void setXL(int xl) {
		XL = xl;
	}

	/**
	 * @return the kep
	 */
	public int getKep() {
		return kep;
	}

	/**
	 * @param kep the kep to set
	 */
	public void setKep(int kep) {
		this.kep = kep;
	}

	/**
	 * @return the tCourbeCharge
	 */
	public int getTCourbeCharge() {
		return tcc;
	}

	/**
	 * @param courbeCharge the tCourbeCharge to set
	 */
	public void setTCourbeCharge(int courbeCharge) {
		tcc = courbeCharge;
	}

	public String toString(){
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("*** Parameters: ***\n");
		strBuff.append("	- DateDebutPeriode: " + getDebutPeriode_48());
		strBuff.append("	- DateFinPeriode: " + getDernierHoroDate());
		strBuff.append("	- TC: " + getTC());strBuff.append("\n");
		strBuff.append("	- TT: " + getTT());strBuff.append("\n");
		strBuff.append("	- KJ: " + getKJ());strBuff.append("\n");
		strBuff.append("	- KPr: " + getKPr());strBuff.append("\n");
		strBuff.append("	- KF: " + getKF());strBuff.append("\n");
		strBuff.append("	- RL: " + getRL());strBuff.append("\n");
		strBuff.append("	- XL: " + getXL());strBuff.append("\n");
		strBuff.append("	- kep: " + getKep());strBuff.append("\n");
		strBuff.append("	- tCourbeCharge: " + getTCourbeCharge());strBuff.append("\n");
		if(isCcReact()){
			strBuff.append("	- ccReact: true");strBuff.append("\n");
		}
		else{
			strBuff.append("	- ccReact: false");strBuff.append("\n");
		}
		return strBuff.toString();
	}

	/**
	 * @return the ccReact
	 */
	public boolean isCcReact() {
		return ccReact;
	}

	/**
	 * @param ccReact the ccReact to set
	 */
	protected void setCcReact(boolean ccReact) {
		this.ccReact = ccReact;
	}

}
