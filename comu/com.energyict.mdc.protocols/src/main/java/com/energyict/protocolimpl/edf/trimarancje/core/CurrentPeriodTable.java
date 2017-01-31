/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 *
 */
package com.energyict.protocolimpl.edf.trimarancje.core;

import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/**
 * @author gna
 *
 */
public class CurrentPeriodTable extends AbstractTable{

	private int DEBUG = 0;

	private Date timeStamp;

	private int zoneA = 0;
	private int zoneB = 1;
	private int zoneC = 2;
	private int zoneD = 3;

	private int pointeMobile = 0;
	private int pointe = 1;
	private int pleinesdHiver = 2;
	private int creusesdHiver = 3;
	private int pleinesdEte = 4;
	private int creusesdEte = 5;

	private int tarif;

	private long activeEnergy[] = new long[6]; 			//kWh
	private int durationExceedingPower[] = new int[4]; 	//minutes
	private int exceedingPower[] = new int[4]; 			//daVA(tenths of VA)
	private int maxDemand[] = new int[4]; 				//daVA
	private int tarifDuration[] = new int[4];			//h
	private int coefficient[] = new int[4];				//%

	private int tarifVersionNextPeriod;

	public CurrentPeriodTable(DataFactory dataFactory) {
		super(dataFactory);
	}

	protected int getCode() {
		return 2;
	}

	public String toString(){

		return "CurrentPeriodTable:\n" +
				"TimeStamp: " + getTimeStamp() + "\n" +
				"Tarif: " + getTarif() + "\n" +
				"A+ PM: " + getActiveEnergy(pointeMobile) + "\n" +
				"A+ P: " + getActiveEnergy(pointe) + "\n" +
				"A+ HPH: " + getActiveEnergy(pleinesdHiver) + "\n" +
				"A+ HCH:" + getActiveEnergy(creusesdHiver) + "\n" +
				"A+ HPE: " + getActiveEnergy(pleinesdEte) + "\n" +
				"A+ HCE: " + getActiveEnergy(creusesdEte) + "\n" +
				"Duration exceeding zoneA " + getDurationExceedingPower(zoneA) + "\n" +
				"Duration exceeding zoneB " + getDurationExceedingPower(zoneB) + "\n" +
				"Duration exceeding zoneC " + getDurationExceedingPower(zoneC) + "\n" +
				"Duration exceeding zondD " + getDurationExceedingPower(zoneD) + "\n" +
				"Max demand zoneA " + getMaxDemand(zoneA) + "\n" +
				"Max demand zoneB " + getMaxDemand(zoneB) + "\n" +
				"Max demand zoneC " + getMaxDemand(zoneC) + "\n" +
				"Max demand zoneD " + getMaxDemand(zoneD) + "\n" +
				"Exceeding power zoneA " + getExceedingPower(zoneA) + "\n" +
				"Exceeding power zoneB " + getExceedingPower(zoneB) + "\n" +
				"Exceeding power zoneC " + getExceedingPower(zoneC) + "\n" +
				"Exceeding power zoneD " + getExceedingPower(zoneD) + "\n" +
				"Coefficient zoneA " + getCoefficient(zoneA) + "\n" +
				"Coefficient zoneB " + getCoefficient(zoneB) + "\n" +
				"Coefficient zoneC " + getCoefficient(zoneC) + "\n" +
				"Coefficient zoneD " + getCoefficient(zoneD) + "\n" +
				"Get tarifVersion next period: " + getTarifVersionNextPeriod() + "\n" +
				"Duration zoneA " + getTarifDuration(zoneA) + "\n" +
				"Duration zoneB " + getTarifDuration(zoneB) + "\n" +
				"Duration zoneC " + getTarifDuration(zoneC) + "\n" +
				"Duration zoneD " + getTarifDuration(zoneD) + "\n";
	}

	protected void parse(byte[] data) throws IOException {
//      System.out.println("KV_DEBUG> write to file");

//		File file = new File("c://TEST_FILES/CurrentPeriodTable_" + counter++ + ".bin");
//		FileOutputStream fos = new FileOutputStream(file);
//		fos.write(data);
//		fos.close();

		if((data[0]!=-1)&&(data[1]!=-1)&&(data[2]!=-1)&&(data[3]!=-1)&&(data[4]!=-1)){

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
				setActiveEnergy(ProtocolUtils.getIntLE(data, offset, 3), i);
				offset += 3;
			}
			for (int i = 0; i < 4; i++) {
				setDurationExceedingPower(ProtocolUtils.getIntLE(data, offset, 2), i);
				offset += 2;
			}
			for (int i = 0; i < 4; i++) {
				setMaxDemand(ProtocolUtils.getIntLE(data, offset, 2), i);
				offset += 2;
			}
			for (int i = 0; i < 4; i++) {
				setExceedingPower(ProtocolUtils.getIntLE(data, offset, 2), i);
				offset += 2;
			}
			for (int i = 0; i < 4; i++) {
				setCoefficient(ProtocolUtils.getIntLE(data, offset, 1), i);
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

	public int getTarif() {
		return tarif;
	}

	public void setTarif(int tarif) {
		this.tarif = tarif;
	}

	public int getTarifVersionNextPeriod() {
		return tarifVersionNextPeriod;
	}

	public void setTarifVersionNextPeriod(int tarifVersionNextPeriod) {
		this.tarifVersionNextPeriod = tarifVersionNextPeriod;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * @return the activeEnergy
	 */
	public long getActiveEnergy(int t) {
		return activeEnergy[t];
	}

	public Quantity getActiveQuantity(int t){
		return new Quantity(BigDecimal.valueOf(getActiveEnergy(t)),Unit.get("kWh"));
	}

	/**
	 * @param activeEnergy the activeEnergy to set
	 */
	public void setActiveEnergy(long activeEnergy, int t) {
		this.activeEnergy[t] = activeEnergy;
	}

	/**
	 * @return the durationExceedingPower
	 */
	public int getDurationExceedingPower(int t) {
		return durationExceedingPower[t];
	}

	public Quantity getDurationExceedingPowerQuantity(int t){
		return new Quantity(BigDecimal.valueOf(getDurationExceedingPower(t)), Unit.get("min"));
	}

	/**
	 * @param durationExceedingPower the durationExceedingPower to set
	 */
	public void setDurationExceedingPower(int durationExceedingPower, int t) {
		this.durationExceedingPower[t] = durationExceedingPower;
	}

	/**
	 * @return the exceedingPower
	 */
	public int getExceedingPower(int t) {
		return exceedingPower[t];
	}

	public Quantity getExceedingPowerQuantity(int t){
		return new Quantity(BigDecimal.valueOf((long)(getExceedingPower(t)*10)), Unit.get("VA"));
	}

	/**
	 * @param exceedingPower the exceedingPower to set
	 */
	public void setExceedingPower(int exceedingPower, int t) {
		this.exceedingPower[t] = exceedingPower;
	}

	/**
	 * @return the maxDemand
	 */
	public int getMaxDemand(int t) {
		return maxDemand[t];
	}

	public Quantity getMaxDemandQuantity(int t){
		return new Quantity(BigDecimal.valueOf((long)(getMaxDemand(t)*10)),Unit.get("VA"));
	}

	/**
	 * @param maxDemand the maxDemand to set
	 */
	public void setMaxDemand(int maxDemand, int t) {
		this.maxDemand[t] = maxDemand;
	}

	/**
	 * @return the tarifDuration
	 */
	public int getTarifDuration(int t) {
		return tarifDuration[t];
	}

	public Quantity getTarifDurationQuantity(int t){
		return new Quantity(BigDecimal.valueOf(getTarifDuration(t)), Unit.get("h"));
	}

	/**
	 * @param tarifDuration the tarifDuration to set
	 */
	public void setTarifDuration(int tarifDuration, int t) {
		this.tarifDuration[t] = tarifDuration;
	}

	/**
	 * @return the coefficient
	 */
	public int getCoefficient(int t) {
		return coefficient[t];
	}

	public Quantity getCoefficientQuantity(int t){
		return new Quantity(BigDecimal.valueOf(getCoefficient(t)),Unit.get("%"));
	}

	/**
	 * @param coefficient the coefficient to set
	 */
	public void setCoefficient(int coefficient, int t) {
		this.coefficient[t] = coefficient;
	}

}
