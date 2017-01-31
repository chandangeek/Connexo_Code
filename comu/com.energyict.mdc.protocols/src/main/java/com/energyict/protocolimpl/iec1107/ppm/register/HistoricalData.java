/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.ppm.register;

import com.energyict.protocolimpl.iec1107.ppm.MetaRegister;
import com.energyict.protocolimpl.iec1107.ppm.RegisterFactory;

import java.util.Date;
import java.util.TimeZone;

/** @author fbo */

public class HistoricalData {

	private int billingCount;

	private Date date;
	private TimeZone timeZone;

	private MainRegister importKWh;
	private MainRegister exportKWh;
	private MainRegister importKvarh;
	private MainRegister exportKvarh;
	private MainRegister totalKVAh;

	private MainRegister timeOfUse1;
	private MainRegister timeOfUse2;
	private MainRegister timeOfUse3;
	private MainRegister timeOfUse4;
	private MainRegister timeOfUse5;
	private MainRegister timeOfUse6;
	private MainRegister timeOfUse7;
	private MainRegister timeOfUse8;

	private MaximumDemand maxDemand1;
	private MaximumDemand maxDemand2;
	private MaximumDemand maxDemand3;
	private MaximumDemand maxDemand4;

	private MainRegister cumulativeMaxDemand1;
	private MainRegister cumulativeMaxDemand2;
	private MainRegister cumulativeMaxDemand3;
	private MainRegister cumulativeMaxDemand4;

	public MainRegister getExportKWh() {
		return this.exportKWh;
	}

	public void setExportKWh(MainRegister exportKWh) {
		this.exportKWh = exportKWh;
	}

	public MainRegister getImportKvarh() {
		return this.importKvarh;
	}

	public void setImportKvarh(MainRegister importKvarh) {
		this.importKvarh = importKvarh;
	}

	public MainRegister getCumulativeMaxDemand1() {
		return this.cumulativeMaxDemand1;
	}

	public int getBillingCount() {
		return this.billingCount;
	}

	public void setBillingCount(int billingCount) {
		this.billingCount = billingCount;
	}

	public MaximumDemand getMaxDemand1() {
		return this.maxDemand1;
	}

	public void setMaxDemand1(MaximumDemand maxDemand1) {
		this.maxDemand1 = maxDemand1;
	}

	public MaximumDemand getMaxDemand2() {
		return this.maxDemand2;
	}

	public void setMaxDemand2(MaximumDemand maxDemand2) {
		this.maxDemand2 = maxDemand2;
	}

	public MaximumDemand getMaxDemand3() {
		return this.maxDemand3;
	}

	public void setMaxDemand3(MaximumDemand maxDemand3) {
		this.maxDemand3 = maxDemand3;
	}

	public MaximumDemand getMaxDemand4() {
		return this.maxDemand4;
	}

	public void setMaxDemand4(MaximumDemand maxDemand4) {
		this.maxDemand4 = maxDemand4;
	}

	public void setCumulativeMaxDemand1(MainRegister cumulativeMaxDemand1) {
		this.cumulativeMaxDemand1 = cumulativeMaxDemand1;
	}

	public MainRegister getCumulativeMaxDemand2() {
		return this.cumulativeMaxDemand2;
	}

	public void setCumulativeMaxDemand2(MainRegister cumulativeMaxDemand2) {
		this.cumulativeMaxDemand2 = cumulativeMaxDemand2;
	}

	public MainRegister getCumulativeMaxDemand3() {
		return this.cumulativeMaxDemand3;
	}

	public void setCumulativeMaxDemand3(MainRegister cumulativeMaxDemand3) {
		this.cumulativeMaxDemand3 = cumulativeMaxDemand3;
	}

	public MainRegister getCumulativeMaxDemand4() {
		return this.cumulativeMaxDemand4;
	}

	public void setCumulativeMaxDemand4(MainRegister cumulativeMaxDemand4) {
		this.cumulativeMaxDemand4 = cumulativeMaxDemand4;
	}

	public Date getDate() {
		return this.date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public MainRegister getTimeOfUse1() {
		return this.timeOfUse1;
	}

	public void setTimeOfUse1(MainRegister timeOfUse1) {
		this.timeOfUse1 = timeOfUse1;
	}

	public MainRegister getTimeOfUse2() {
		return this.timeOfUse2;
	}

	public void setTimeOfUse2(MainRegister timeOfUse2) {
		this.timeOfUse2 = timeOfUse2;
	}

	public MainRegister getTimeOfUse3() {
		return this.timeOfUse3;
	}

	public void setTimeOfUse3(MainRegister timeOfUse3) {
		this.timeOfUse3 = timeOfUse3;
	}

	public MainRegister getTimeOfUse4() {
		return this.timeOfUse4;
	}

	public void setTimeOfUse4(MainRegister timeOfUse4) {
		this.timeOfUse4 = timeOfUse4;
	}

	public MainRegister getTimeOfUse5() {
		return this.timeOfUse5;
	}

	public void setTimeOfUse5(MainRegister timeOfUse5) {
		this.timeOfUse5 = timeOfUse5;
	}

	public MainRegister getTimeOfUse6() {
		return this.timeOfUse6;
	}

	public void setTimeOfUse6(MainRegister timeOfUse6) {
		this.timeOfUse6 = timeOfUse6;
	}

	public MainRegister getTimeOfUse7() {
		return this.timeOfUse7;
	}

	public void setTimeOfUse7(MainRegister timeOfUse7) {
		this.timeOfUse7 = timeOfUse7;
	}

	public MainRegister getTimeOfUse8() {
		return this.timeOfUse8;
	}

	public void setTimeOfUse8(MainRegister timeOfUse8) {
		this.timeOfUse8 = timeOfUse8;
	}

	public MainRegister getExportKvarh() {
		return this.exportKvarh;
	}

	public void setExportKvarh(MainRegister exportKvarh) {
		this.exportKvarh = exportKvarh;
	}

	public MainRegister getImportKWh() {
		return this.importKWh;
	}

	public void setImportKWh(MainRegister importKWh) {
		this.importKWh = importKWh;
	}

	public TimeZone getTimeZone() {
		return this.timeZone;
	}

	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	public MainRegister getTotalKVAh() {
		return this.totalKVAh;
	}

	public void setTotalKVAh(MainRegister totalKVAh) {
		this.totalKVAh = totalKVAh;
	}

	public Object get(MetaRegister register) {
		return get(register.getRegisterFactoryKey());
	}

	public Object get(String key) {

		if (key.equals(RegisterFactory.R_TOTAL_IMPORT_WH)) {
			return this.importKWh;
		}
		if (key.equals(RegisterFactory.R_TOTAL_EXPORT_WH)) {
			return this.exportKWh;
		}
		if (key.equals(RegisterFactory.R_TOTAL_IMPORT_VARH)) {
			return this.importKvarh;
		}
		if (key.equals(RegisterFactory.R_TOTAL_EXPORT_VARH)) {
			return this.exportKvarh;
		}
		if (key.equals(RegisterFactory.R_TOTAL_VAH)) {
			return this.totalKVAh;
		}

		if (key.equals(RegisterFactory.R_TIME_OF_USE_1)) {
			return this.timeOfUse1;
		}
		if (key.equals(RegisterFactory.R_TIME_OF_USE_2)) {
			return this.timeOfUse2;
		}
		if (key.equals(RegisterFactory.R_TIME_OF_USE_3)) {
			return this.timeOfUse3;
		}
		if (key.equals(RegisterFactory.R_TIME_OF_USE_4)) {
			return this.timeOfUse4;
		}
		if (key.equals(RegisterFactory.R_TIME_OF_USE_5)) {
			return this.timeOfUse5;
		}
		if (key.equals(RegisterFactory.R_TIME_OF_USE_6)) {
			return this.timeOfUse6;
		}
		if (key.equals(RegisterFactory.R_TIME_OF_USE_7)) {
			return this.timeOfUse7;
		}
		if (key.equals(RegisterFactory.R_TIME_OF_USE_8)) {
			return this.timeOfUse8;
		}

		if (key.equals(RegisterFactory.R_MAXIMUM_DEMAND_1)) {
			return this.maxDemand1;
		}
		if (key.equals(RegisterFactory.R_MAXIMUM_DEMAND_2)) {
			return this.maxDemand2;
		}
		if (key.equals(RegisterFactory.R_MAXIMUM_DEMAND_3)) {
			return this.maxDemand3;
		}
		if (key.equals(RegisterFactory.R_MAXIMUM_DEMAND_4)) {
			return this.maxDemand4;
		}

		if (key.equals(RegisterFactory.R_CUMULATIVE_MAXIMUM_DEMAND1)) {
			return this.cumulativeMaxDemand1;
		}
		if (key.equals(RegisterFactory.R_CUMULATIVE_MAXIMUM_DEMAND2)) {
			return this.cumulativeMaxDemand2;
		}
		if (key.equals(RegisterFactory.R_CUMULATIVE_MAXIMUM_DEMAND3)) {
			return this.cumulativeMaxDemand3;
		}
		if (key.equals(RegisterFactory.R_CUMULATIVE_MAXIMUM_DEMAND4)) {
			return this.cumulativeMaxDemand4;
		}

		return null;
	}

	public String toString() {

		StringBuffer sb = new StringBuffer();

		sb.append("Historical Data: \n");
		sb.append("Billing count = " + this.billingCount + " date = " + this.date + "\n");

		sb.append("Total registers\n");
		sb.append(this.importKWh + "\n");
		sb.append(this.exportKWh + "\n");
		sb.append(this.importKvarh + "\n");
		sb.append(this.totalKVAh + "\n");

		sb.append("TOU registers\n");
		sb.append(this.timeOfUse1 + "\n");
		sb.append(this.timeOfUse2 + "\n");
		sb.append(this.timeOfUse3 + "\n");
		sb.append(this.timeOfUse4 + "\n");
		sb.append(this.timeOfUse5 + "\n");
		sb.append(this.timeOfUse6 + "\n");
		sb.append(this.timeOfUse7 + "\n");
		sb.append(this.timeOfUse8 + "\n\n");

		sb.append("Max demand\n");
		sb.append(this.maxDemand1 + "\n");
		sb.append(this.maxDemand2 + "\n");
		sb.append(this.maxDemand3 + "\n");
		sb.append(this.maxDemand4 + "\n\n");

		sb.append("Cum Max demand\n");
		sb.append(this.cumulativeMaxDemand1 + "\n");
		sb.append(this.cumulativeMaxDemand2 + "\n");
		sb.append(this.cumulativeMaxDemand3 + "\n");
		sb.append(this.cumulativeMaxDemand4 + "\n");

		sb.append("___\n\n\n");

		return sb.toString();
	}

}