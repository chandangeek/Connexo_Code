/**
 *
 */
package com.energyict.protocolimpl.edf.trimarancje.core;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/**
 * @author gna
 *
 */
public class PreviousPeriodTable extends AbstractTable{

	private Date timeStamp;
	private int tarif;

	private long activeEnergyP1[] = new long[6]; 			//kWh
	private int durationExceedingPowerP1[] = new int[4]; 	//minutes
	private int exceedingPowerP1[] = new int[4]; 			//daVA(tenths of VA)
	private int maxDemandP1[] = new int[4]; 				//daVA
	private int tarifDuration[] = new int[4];				//h
	private int coefficientP1[] = new int[4];				//%
	private long activeEnergyP2[] = new long[6]; 			//kWh
	private int durationExceedingPowerP2[] = new int[4]; 	//minutes
	private int exceedingPowerP2[] = new int[4]; 			//daVA(tenths of VA)
	private int maxDemandP2[] = new int[4]; 				//daVA
//	private int tarifDurationP2[] = new int[4];				//h
	private int coefficientP2[] = new int[4];				//%

	private int tarifVersionNextPeriod;

	public PreviousPeriodTable(DataFactory dataFactory) {
		super(dataFactory);
	}

	protected int getCode() {
		return 1;
	}

	protected void parse(byte[] data) throws IOException {
//		System.out.println("KV_DEBUG> write to file");
//		File file = new File("c://TEST_FILES/PreviousPeriodtable_" + counter++ + ".bin");
//		FileOutputStream fos = new FileOutputStream(file);
//		fos.write(data);
//		fos.close();

		if ((data[0] != -1) && (data[1] != -1) && (data[2] != -1) && (data[3] != -1) && (data[4] != -1)) {

			int offset = 0;
			Calendar cal = Calendar.getInstance();
			int jour = ProtocolUtils.BCD2hex(data[offset++]);
			cal.set(Calendar.DAY_OF_MONTH, jour);
			int mois = ProtocolUtils.BCD2hex(data[offset++]);
			cal.set(Calendar.MONTH, mois - 1);
			int an = ProtocolUtils.BCD2hex(data[offset++]) % 10;	// we only need the unit (eenheid in dutch)
//			cal.set(Calendar.YEAR, (cal.get(Calendar.YEAR) / 10) * 10 + an);
			cal.set(Calendar.YEAR, getDecenniumYearTable()[an]);
			int heure = ProtocolUtils.BCD2hex(data[offset++]);
			cal.set(Calendar.HOUR_OF_DAY, heure);
			int minute = ProtocolUtils.BCD2hex(data[offset++]);
			cal.set(Calendar.MINUTE, minute);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			setTimeStamp(cal.getTime());

			setTarif(ProtocolUtils.getIntLE(data, offset, 1));
			offset += 1;

			for (int i = 0; i < 6; i++) {
				setActiveEnergyP1(ProtocolUtils.getIntLE(data, offset, 3), i);
				offset += 3;
			}
			for (int i = 0; i < 4; i++) {
				setDurationExceedingPowerP1(ProtocolUtils.getIntLE(data, offset, 2), i);
				offset += 2;
			}
			for (int i = 0; i < 4; i++) {
				setMaxDemandP1(ProtocolUtils.getIntLE(data, offset, 2), i);
				offset += 2;
			}
			for (int i = 0; i < 4; i++) {
				setExceedingPowerP1(ProtocolUtils.getIntLE(data, offset, 2), i);
				offset += 2;
			}
			for (int i = 0; i < 6; i++) {
				setActiveEnergyP2(ProtocolUtils.getIntLE(data, offset, 3), i);
				offset += 3;
			}
			for (int i = 0; i < 4; i++) {
				setDurationExceedingPowerP2(ProtocolUtils.getIntLE(data, offset, 2), i);
				offset += 2;
			}
			for (int i = 0; i < 4; i++) {
				setMaxDemandP2(ProtocolUtils.getIntLE(data, offset, 2), i);
				offset += 2;
			}
			for (int i = 0; i < 4; i++) {
				setExceedingPowerP2(ProtocolUtils.getIntLE(data, offset, 2), i);
				offset += 2;
			}
			for (int i = 0; i < 4; i++) {
				setCoefficientP1(ProtocolUtils.getIntLE(data, offset, 1), i);
				offset += 1;
			}
			for (int i = 0; i < 4; i++) {
				setCoefficientP2(ProtocolUtils.getIntLE(data, offset, 1), i);
				offset += 1;
			}

			setTarifVersionNextPeriod(ProtocolUtils.getIntLE(data, offset, 1));
			offset += 1;

			for (int i = 0; i < 4; i++) {
				setTarifDuration(ProtocolUtils.getIntLE(data, offset, 2), i);
				offset += 2;
			}

		}
	}

	/**
	 * @return the timeStamp
	 */
	public Date getTimeStamp() {
		return timeStamp;
	}

	/**
	 * @param timeStamp the timeStamp to set
	 */
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * @return the tarifVersionNextPeriod
	 */
	public int getTarifVersionNextPeriod() {
		return tarifVersionNextPeriod;
	}

	/**
	 * @param tarifVersionNextPeriod the tarifVersionNextPeriod to set
	 */
	public void setTarifVersionNextPeriod(int tarifVersionNextPeriod) {
		this.tarifVersionNextPeriod = tarifVersionNextPeriod;
	}

	/**
	 * @return the tarif
	 */
	public int getTarif() {
		return tarif;
	}

	/**
	 * @param tarif the tarif to set
	 */
	public void setTarif(int tarif) {
		this.tarif = tarif;
	}

	/**
	 * @return the activeEnergyP1
	 */
	public long getActiveEnergyP1(int t) {
		return activeEnergyP1[t];
	}

	public Quantity getActiveQuantityP1(int t){
		return new Quantity(BigDecimal.valueOf(getActiveEnergyP1(t)),Unit.get("kWh"));
	}

	/**
	 * @param activeEnergyP1 the activeEnergyP1 to set
	 */
	public void setActiveEnergyP1(long activeEnergyP1, int t) {
		this.activeEnergyP1[t] = activeEnergyP1;
	}

	/**
	 * @return the durationExceedingPowerP1
	 */
	public int getDurationExceedingPowerP1(int t) {
		return durationExceedingPowerP1[t];
	}

	public Quantity getDurationExceedingPowerQuantityP1(int t){
		return new Quantity(BigDecimal.valueOf(getDurationExceedingPowerP1(t)), Unit.get("min"));
	}

	/**
	 * @param durationExceedingPowerP1 the durationExceedingPowerP1 to set
	 */
	public void setDurationExceedingPowerP1(int durationExceedingPowerP1, int t) {
		this.durationExceedingPowerP1[t] = durationExceedingPowerP1;
	}

	/**
	 * @return the exceedingPowerP1
	 */
	public int getExceedingPowerP1(int t) {
		return exceedingPowerP1[t];
	}

	public Quantity getExceedingPowerQuantityP1(int t){
		return new Quantity(BigDecimal.valueOf((long)(getExceedingPowerP1(t)*10)), Unit.get("VA"));
	}

	/**
	 * @param exceedingPowerP1 the exceedingPowerP1 to set
	 */
	public void setExceedingPowerP1(int exceedingPowerP1, int t) {
		this.exceedingPowerP1[t] = exceedingPowerP1;
	}

	/**
	 * @return the maxDemandP1
	 */
	public int getMaxDemandP1(int t) {
		return maxDemandP1[t];
	}

	public Quantity getMaxDemandQuantityP1(int t){
		return new Quantity(BigDecimal.valueOf((long)(getMaxDemandP1(t)*10)),Unit.get("VA"));
	}

	/**
	 * @param maxDemandP1 the maxDemandP1 to set
	 */
	public void setMaxDemandP1(int maxDemandP1, int t) {
		this.maxDemandP1[t] = maxDemandP1;
	}

	/**
	 * @return the coefficientP1
	 */
	public int getCoefficientP1(int t) {
		return coefficientP1[t];
	}

	public Quantity getCoefficientQuantityP1(int t){
		return new Quantity(BigDecimal.valueOf(getCoefficientP1(t)),Unit.get("%"));
	}

	/**
	 * @param coefficientP1 the coefficientP1 to set
	 */
	public void setCoefficientP1(int coefficientP1, int t) {
		this.coefficientP1[t] = coefficientP1;
	}

	/**
	 * @return the activeEnergyP2
	 */
	public long getActiveEnergyP2(int t) {
		return activeEnergyP2[t];
	}

	public Quantity getActiveQuantityP2(int t){
		return new Quantity(BigDecimal.valueOf(getActiveEnergyP2(t)),Unit.get("kWh"));
	}

	/**
	 * @param activeEnergyP2 the activeEnergyP2 to set
	 */
	public void setActiveEnergyP2(long activeEnergyP2, int t) {
		this.activeEnergyP2[t] = activeEnergyP2;
	}

	/**
	 * @return the durationExceedingPowerP2
	 */
	public int getDurationExceedingPowerP2(int t) {
		return durationExceedingPowerP2[t];
	}

	public Quantity getDurationExceedingPowerQuantityP2(int t){
		return new Quantity(BigDecimal.valueOf(getDurationExceedingPowerP2(t)), Unit.get("min"));
	}

	/**
	 * @param durationExceedingPowerP2 the durationExceedingPowerP2 to set
	 */
	public void setDurationExceedingPowerP2(int durationExceedingPowerP2, int t) {
		this.durationExceedingPowerP2[t] = durationExceedingPowerP2;
	}

	/**
	 * @return the exceedingPowerP2
	 */
	public int getExceedingPowerP2(int t) {
		return exceedingPowerP2[t];
	}

	public Quantity getExceedingPowerQuantityP2(int t){
		return new Quantity(BigDecimal.valueOf((long)(getExceedingPowerP2(t)*10)), Unit.get("VA"));
	}

	/**
	 * @param exceedingPowerP2 the exceedingPowerP2 to set
	 */
	public void setExceedingPowerP2(int exceedingPowerP2, int t) {
		this.exceedingPowerP2[t] = exceedingPowerP2;
	}

	/**
	 * @return the maxDemandP2
	 */
	public int getMaxDemandP2(int t) {
		return maxDemandP2[t];
	}

	public Quantity getMaxDemandQuantityP2(int t){
		return new Quantity(BigDecimal.valueOf((long)(getMaxDemandP2(t)*10)),Unit.get("VA"));
	}

	/**
	 * @param maxDemandP2 the maxDemandP2 to set
	 */
	public void setMaxDemandP2(int maxDemandP2, int t) {
		this.maxDemandP2[t] = maxDemandP2;
	}

	/**
	 * @return the coefficientP2
	 */
	public int getCoefficientP2(int t) {
		return coefficientP2[t];
	}

	/**
	 * @param coefficientP2 the coefficientP2 to set
	 */
	public void setCoefficientP2(int coefficientP2, int t) {
		this.coefficientP2[t] = coefficientP2;
	}

	public Quantity getCoefficientQuantityP2(int t){
		return new Quantity(BigDecimal.valueOf(getCoefficientP2(t)),Unit.get("%"));
	}

	/**
	 * @return the tarifDurationP1
	 */
	public int getTarifDuration(int t) {
		return tarifDuration[t];
	}

	public Quantity getTarifDurationQuantity(int t){
		return new Quantity(BigDecimal.valueOf(getTarifDuration(t)), Unit.get("h"));
	}

	/**
	 * @param tarifDurationP1 the tarifDurationP1 to set
	 */
	public void setTarifDuration(int tarifDuration, int t) {
		this.tarifDuration[t] = tarifDuration;
	}

}
