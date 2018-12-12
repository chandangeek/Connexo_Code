package com.energyict.protocolimpl.iec1107.ppm.register;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.iec1107.ppm.PPMUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/* @author fbo */

public class MaximumDemand {

	private Quantity[] quantity = new Quantity[3];
	private Date[] date = new Date[3];
	private Unit unit = null;

	private TimeZone timeZone;

	public MaximumDemand(Unit unit, byte[] data, BigDecimal scalingFactor, TimeZone timeZone) throws IOException {
		this.unit = unit;
		this.timeZone = timeZone;
		parse(data, scalingFactor);
	}

	private void parse(byte[] data, BigDecimal scalingFactor) throws IOException {

		this.quantity[0] = PPMUtils.parseQuantity(data, 0, 5, scalingFactor, this.unit);
		this.quantity[1] = PPMUtils.parseQuantity(data, 5, 5, scalingFactor, this.unit);
		this.quantity[2] = PPMUtils.parseQuantity(data, 10, 5, scalingFactor, this.unit);

		this.date[0] = PPMUtils.parseTimeStamp(data, 16, this.timeZone);
		this.date[1] = PPMUtils.parseTimeStamp(data, 20, this.timeZone);
		this.date[2] = PPMUtils.parseTimeStamp(data, 24, this.timeZone);

	}

	public Quantity getQuantity(int index) {
		return this.quantity[index];
	}

	public void setQuantity(int index, Quantity quantity) {
		this.quantity[index] = quantity;
	}

	public RegisterValue toRegisterValue(ObisCode o, Date date) {
		return new RegisterValue(o, this.quantity[o.getB() - 1], this.date[o.getB() - 1], null, date);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		SimpleDateFormat df = new SimpleDateFormat();
		builder.append("1) ").append(this.quantity[0]).append(" ").append(df.format(this.date[0])).append("\n");
		builder.append("2) ").append(this.quantity[1]).append(" ").append(df.format(this.date[1])).append("\n");
		builder.append("3) ").append(this.quantity[2]).append(" ").append(df.format(this.date[2])).append("\n");

		return builder.toString();
	}

}