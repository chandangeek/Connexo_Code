package com.energyict.protocolimpl.iec1107.ppmi1.register;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.iec1107.ppmi1.PPMUtils;

/* @author fbo */

public class MaximumDemand {

	Quantity[] quantity = new Quantity[3];
	Date[] date = new Date[3];
	Unit unit = null;

	TimeZone timeZone;

	public MaximumDemand(Unit unit, byte[] data, BigDecimal scalingFactor,
			TimeZone timeZone) throws IOException {
		this.unit = unit;
		this.timeZone = timeZone;
		parse(data, scalingFactor);
	}

	private void parse(byte[] data, BigDecimal scalingFactor)
			throws IOException {

		quantity[0] = PPMUtils.parseQuantity(data, 0, 5, scalingFactor, unit);
		quantity[1] = PPMUtils.parseQuantity(data, 5, 5, scalingFactor, unit);
		quantity[2] = PPMUtils.parseQuantity(data, 10, 5, scalingFactor, unit);

		date[0] = PPMUtils.parseTimeStamp(data, 16, timeZone);
		date[1] = PPMUtils.parseTimeStamp(data, 20, timeZone);
		date[2] = PPMUtils.parseTimeStamp(data, 24, timeZone);

	}

	public Quantity getQuantity(int index) {
		return quantity[index];
	}

	public void setQuantity(int index, Quantity quantity) {
		this.quantity[index] = quantity;
	}
	
	public Date getDate( int index ){
	    return this.date[index];
	}

	public RegisterValue toRegisterValue(ObisCode o, Date date) {
		return new RegisterValue(o, quantity[o.getB() - 1],
				this.date[o.getB() - 1], null, date);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		SimpleDateFormat df = new SimpleDateFormat();
		sb.append("1) " + quantity[0] + " " + df.format(date[0]) + "\n");
		sb.append("2) " + quantity[1] + " " + df.format(date[1]) + "\n");
		sb.append("3) " + quantity[2] + " " + df.format(date[2]) + "\n");

		return sb.toString();
	}

}