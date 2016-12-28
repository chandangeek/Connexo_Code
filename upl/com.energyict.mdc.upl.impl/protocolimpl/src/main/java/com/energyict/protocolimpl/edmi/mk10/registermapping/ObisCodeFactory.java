/*
 * ObisCodeFactory.java
 *
 * Created on 24 maart 2006, 11:15
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10.registermapping;

import com.energyict.mdc.upl.NoSuchRegisterException;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.edmi.mk10.MK10;
import com.energyict.protocolimpl.edmi.mk10.command.ReadCommand;
import com.energyict.protocolimpl.edmi.mk10.core.TOUChannelTypeParser;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author koen
 */
public class ObisCodeFactory {
	static final int DEBUG = 0;
	MK10 mk10;
	List<TOURegisterInfo> touRegisterInfos;
	private BillingInfo billingInfo=null;

	/** Creates a new instance of ObisCodeFactory */
	public ObisCodeFactory(MK10 mk10) throws IOException {
		this.mk10=mk10;
		initTOURegisterInfos();
	}

	// tou register type
	private final int TYPE_ENERGY=0;
	private final int TYPE_MAX_DEMAND=1;

	// tou period
	private final int PERIOD_CURRENT=0;
	private final int PERIOD_PREVIOUS1=1;
	private final int PERIOD_BILLING_TOTAL=14;
	private final int PERIOD_TOTAL=15;

	// tou channel
	private static final int CHANNEL_START=0;
	private static final int CHANNEL_NR_OF_CHANNELS=16;

	// tou register function
	private static final int RATE_UNIFIED=0;
	private static final int RATE_START=1;

	public void initTOURegisterInfos() throws IOException {
		touRegisterInfos = new ArrayList<>();
		TOUChannelTypeParser tou_ctp;

		for (int channel=CHANNEL_START;channel<CHANNEL_NR_OF_CHANNELS;channel++) {
			int c_definitions = mk10.getCommandFactory().getReadCommand(MK10Register.TOU_CHANNEL_DEFINITIONS + channel).getRegister().getBigDecimal().intValue();

			tou_ctp = new TOUChannelTypeParser(c_definitions);

			if (tou_ctp.isChannel() && (tou_ctp.getObisCField()>0)) {

				// energy tou registers
				int obisc = tou_ctp.getObisCField();
				int rates = tou_ctp.getRates() + 1; // Total amount of rates + unified rate
				int dps = tou_ctp.getDecimalPointScaling();
				Unit unit = tou_ctp.getUnit();

				if (DEBUG >= 1) {
					System.out.println(	"###### initTOURegisterInfos()" +
							" - c_definitions: " + c_definitions +
							" - obisc: " + obisc +
							" - rates: " + rates +
							" - dps: " + dps +
							" - unit: " + unit.toString() +
							" - unit.scaler: " + tou_ctp.getScaling()
					);
				}

				String name1 = "Energy "+tou_ctp.getName()+" Current period";
				String name2 = "Energy "+tou_ctp.getName()+" Previous period";
				String name3 = "Energy "+tou_ctp.getName()+" Billing total period";
				String name4 = "Energy "+tou_ctp.getName()+" Total period";

				String name5 = "Max demand "+tou_ctp.getName()+" Current period";
				String name6 = "Max demand "+tou_ctp.getName()+" Previous period";
				String name7 = "Max demand "+tou_ctp.getName()+" Billing totalent period";
				String name8 = "Max demand "+tou_ctp.getName()+" Total period";

				addTOURegisters(TYPE_ENERGY, channel, PERIOD_CURRENT, obisc, name1, rates, dps, unit);
				addTOURegisters(TYPE_ENERGY, channel, PERIOD_PREVIOUS1, obisc, name2, rates, dps, unit);
				addTOURegisters(TYPE_ENERGY, channel, PERIOD_BILLING_TOTAL, obisc, name3, rates, dps, unit);
				addTOURegisters(TYPE_ENERGY, channel, PERIOD_TOTAL, obisc, name4, rates, dps, unit);

				// max demand registers
				addTOURegisters(TYPE_MAX_DEMAND, channel, PERIOD_CURRENT, obisc, name5, rates, dps, unit);
				addTOURegisters(TYPE_MAX_DEMAND, channel, PERIOD_PREVIOUS1, obisc, name6, rates, dps, unit);
				addTOURegisters(TYPE_MAX_DEMAND, channel, PERIOD_BILLING_TOTAL, obisc, name7, rates, dps, unit);
				addTOURegisters(TYPE_MAX_DEMAND, channel, PERIOD_TOTAL, obisc, name8, rates, dps, unit);

			}
		}
		return;
	}


	// The TOU registerid for the EDMI MK10 is of the following format: 0x0000 => 0aab bbbc cccc dddd
	//	aa		=>	type of data
	//					0 = accumulated
	//					1 = maximum demand value
	//					3 = time of maximum demand
	//	bbbb	=>	specified period
	//					0 = current period
	//					1-13 = previous periods
	//					14 = billing total
	//					15 = total
	//	ccccc	=>	TOU channel
	//	dddd	=>	rate number
	//					0 = unified rate
	//					1-8 = specified rate
	private int buildEdmiEnergyRegisterId(int type, int channel, int period, int rate) {

		if (DEBUG == 1) {
			this.mk10.sendDebug("Channel: " + channel + " Type: " + type + " Period: " + period + " Rate: " + rate);
		}

		type &= 0x0003;
		period &= 0x000F;
		channel &= 0x001F;
		rate &= 0x000F;
		return (type << 13) | ( period << 9) | (channel << 4) | rate;
	}

	private void addTOURegisters(int type, int channel, int period, int cField, String description, int numberofrates, int decimal, Unit unit) {

		boolean timeOfMaxDemand=false;
		boolean billingTimestampFrom=false;

		int dField=0;
		int eField=255;

		switch(period) {

		case PERIOD_CURRENT: {
			billingTimestampFrom=true; // beginning of the current billing period...
			eField=255;
			switch(type) {
			case TYPE_ENERGY: { // Time Integral 2, from begin of current billing period to the instantaneous time point
				dField=9;
			} break; // TYPE_ENERGY

			case TYPE_MAX_DEMAND: {
				timeOfMaxDemand=true;
				dField=16;
			} break; // TYPE_MAX_DEMAND
			} //switch(type)
		} break; // PERIOD_CURRENT

		case PERIOD_PREVIOUS1: { // Time Integral 2, from begin of previous billing period to the end of the previous billing period
			eField=0;
			billingTimestampFrom=true;
			switch(type) {
			case TYPE_ENERGY: {
				dField=9;
			} break; // TYPE_ENERGY

			case TYPE_MAX_DEMAND: {
				timeOfMaxDemand=true;
				dField=16;
			} break; // TYPE_MAX_DEMAND
			} //switch(type)
		} break; // PERIOD_PREVIOUS1

		case PERIOD_BILLING_TOTAL: { // Time Integral 1 , from the start of measurements to the end of the previous billing period
			eField=0;
			switch(type) {
			case TYPE_ENERGY: {
				dField=8;
			} break; // TYPE_ENERGY

			case TYPE_MAX_DEMAND: { // Cumulative max demand
				dField=2;
			} break; // TYPE_MAX_DEMAND
			} //switch(type)
		} break; // PERIOD_BILLING_TOTAL

		case PERIOD_TOTAL: {  // Time Integral 1 , from the start of measurements to the instantaneous time point
			eField=255;
			switch(type) {
			case TYPE_ENERGY: {
				dField=8;
			} break; // TYPE_ENERGY

			case TYPE_MAX_DEMAND: { // Cumulative max demand
				dField=2;
			} break; // TYPE_MAX_DEMAND
			} //switch(type)
		} break; // PERIOD_TOTAL

		} // switch(period)

		touRegisterInfos.add(new TOURegisterInfo(new ObisCode(1,1,cField,dField,0,eField), buildEdmiEnergyRegisterId(type, channel, period, RATE_UNIFIED), "EDMI descr: "+description+" total",timeOfMaxDemand,billingTimestampFrom, decimal, unit));
		for (int rate=RATE_START;rate<numberofrates;rate++) {
			touRegisterInfos.add(new TOURegisterInfo(new ObisCode(1,1,cField,dField,rate,eField), buildEdmiEnergyRegisterId(type, channel, period, rate), "EDMI descr: "+description+" rate "+rate,timeOfMaxDemand,billingTimestampFrom, decimal, unit));
		}
	}

	public String getRegisterInfoDescription() {
		StringBuilder builder = new StringBuilder();
		for (TOURegisterInfo touri : touRegisterInfos) {
			builder
                .append(touri.getObisCode().toString())
                .append(", ")
                .append(touri.getObisCode().toString())
                .append(", ")
                .append(touri.getDescription())
                .append("\n");
		}
		return builder.toString();
	}


	private int findEdmiEnergyRegisterId(ObisCode obisCode) throws IOException {
        for (TOURegisterInfo touri : touRegisterInfos) {
            if (touri.getObisCode().equals(obisCode)) {
                return touri.getEdmiEnergyRegisterId();
            }
        }
		throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
	}

	private TOURegisterInfo findTOURegisterInfo(ObisCode obisCode) throws IOException {
        for (TOURegisterInfo touri : touRegisterInfos) {
            if (touri.getObisCode().equals(obisCode)) {
                return touri;
            }
        }
		throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
	}

	public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
		BigDecimal registervalue;
		TOURegisterInfo touri = findTOURegisterInfo(obisCode);
		ReadCommand rc = mk10.getCommandFactory().getReadCommand(touri.getEdmiEnergyRegisterId());
		Date to=null;
		int dp = touri.getDecimalPoint();
		Unit unit = touri.getUnit();
		int scaler = unit.getScale();

		if (touri.isBillingTimestampTo()) {
			to = doValidateDate(getBillingInfo().getToDate());
		}

		if (DEBUG >= 1) {
			System.out.println();
			System.out.println("rc.getRegister().getBigDecimal().floatValue " + rc.getRegister().getBigDecimal().floatValue());
			System.out.println(" decimal point = " + dp);
		}

		registervalue = rc.getRegister().getBigDecimal();
		registervalue = registervalue.movePointLeft(scaler);
		registervalue = registervalue.setScale(dp, BigDecimal.ROUND_HALF_UP);

		if (touri.isTimeOfMaxDemand()) {
			Date eventDate = doValidateDate(mk10.getCommandFactory().getReadCommand(touri.getEdmiMaxDemandRegisterId()).getRegister().getDate());
			return new RegisterValue(obisCode,new Quantity(registervalue,unit),eventDate,null,null);
		} else {
			return new RegisterValue(obisCode,new Quantity(registervalue,unit),null,null,to);
		}
	}

	// Check if date is valid.
	// The MK10 meter returns 1 January 1996 00:00 when the time is invalid.
	// 820450800000L = 1 Jan 1996 00:00:00,000
	private Date doValidateDate(Date date) {
		if (date==null) {
			return null;
		}
		if (date.compareTo(new Date(820450800000L)) == 0) {
			return null;
		} else {
			return date;
		}
	}

	public BillingInfo getBillingInfo() throws IOException {
		if (billingInfo==null) {
			billingInfo = new BillingInfo(mk10.getCommandFactory());
			if (DEBUG == 1) {
				System.out.println("KV_DEBUG> "+billingInfo);
			}
		}
		return billingInfo;
	}

}
