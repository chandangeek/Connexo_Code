package com.energyict.protocolimpl.instromet.v555.tables;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.instromet.connection.Response;
import com.energyict.protocolimpl.instromet.v555.CommandFactory;

import java.io.IOException;
import java.math.BigDecimal;

public class CountersTable extends AbstractTable {

	private BigDecimal uncorrectedVolume;
	private BigDecimal correctedVolume;

	public CountersTable(TableFactory tableFactory) {
		super(tableFactory);
	}

	protected void parse(byte[] data) throws IOException {
		System.out.println("parse counters");
		System.out.println(ProtocolUtils.outputHexString(data));
		int unCorrected = ProtocolUtils.getInt(data, 0, 4);
		int unCorrectedRemainder = ProtocolUtils.getInt(data, 4, 4);
		int corrected = ProtocolUtils.getInt(data, 8, 4);
		int correctedRemainder = ProtocolUtils.getInt(data, 12, 4);
		uncorrectedVolume = new BigDecimal(unCorrected).add(new BigDecimal(
			Float.intBitsToFloat(unCorrectedRemainder)));
		correctedVolume = new BigDecimal(corrected).add(new BigDecimal(
				Float.intBitsToFloat(correctedRemainder)));
		;
	}

	public int getTableType() {
		return 8;
	}

	public BigDecimal getCorrectedVolume() {
		return this.correctedVolume;
	}

	public BigDecimal getUnCorrectedVolume() {
		return this.uncorrectedVolume;
	}

	protected void prepareBuild() throws IOException {
		CommandFactory commandFactory =
			getTableFactory().getCommandFactory();
		Response response =
			commandFactory.switchToCounters().invoke();
		if (response == null)
			throw new IOException("CountersTable table switch: No answer from corrector");
		parseStatus(response);
    	readHeaders();
	}

	protected void doBuild() throws IOException {
		CommandFactory commandFactory =
			getTableFactory().getCommandFactory();
		Response response =
			commandFactory.readCountersCommand().invoke();
		parseStatus(response);
	    parseWrite(response);
	}


}
