package com.energyict.protocolimpl.instromet.v555.tables;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.instromet.connection.Response;
import com.energyict.protocolimpl.instromet.v555.CommandFactory;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class CorrectorInformationTable extends AbstractTable {

	private Date time;
	private String firmwareVersion;
	private String serialNumber;

	public CorrectorInformationTable(TableFactory tableFactory) {
		super(tableFactory);
	}

	public Date getTime() {
		return time;
	}

	protected void setTime(Date time) {
		this.time = time;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	protected void setFirwareVersion(String firmwareVersion) {
		this.firmwareVersion = firmwareVersion;
	}

	public String getFirwareVersion() {
		return firmwareVersion;
	}

	protected void parse(byte[] data) throws IOException {
		System.out.println("parse corrector info");
		System.out.println(ProtocolUtils.outputHexString(data));
		int length = data.length;

		int year = ProtocolUtils.BCD2hex(data[length - 1]);
		int month = ProtocolUtils.BCD2hex(data[length - 2]);
		int day = ProtocolUtils.BCD2hex(data[length - 3]);
		int hour = ProtocolUtils.BCD2hex(data[length - 5]);
		int min = ProtocolUtils.BCD2hex(data[length - 6]);
		int sec = ProtocolUtils.BCD2hex(data[length - 7]);
		Calendar cal = Calendar.getInstance(
				getTableFactory().getInstromet555().getTimeZone());
		cal.set(Calendar.YEAR, (2000 + year));
		cal.set(Calendar.MONTH, (month - 1));
		cal.set(Calendar.DATE, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, min);
		cal.set(Calendar.SECOND, sec);
		cal.set(Calendar.MILLISECOND, 0);
		System.out.println(cal.getTime());
		setTime(cal.getTime());

		serialNumber = "" + ProtocolUtils.getInt(data, length - 11, 4);
		System.out.println("serialNumber = " + serialNumber);


		StringBuffer strBuff = new StringBuffer("Corrector type code ");
		strBuff.append(String.valueOf((char)ProtocolUtils.convertHexLSB(data[0]&0xFF)));
		strBuff.append(String.valueOf((char)ProtocolUtils.convertHexMSB(data[0]&0xFF)));
		strBuff.append(String.valueOf((char)ProtocolUtils.convertHexLSB(data[1]&0xFF)));
		strBuff.append(String.valueOf((char)ProtocolUtils.convertHexMSB(data[1]&0xFF)));
		strBuff.append(String.valueOf((char)ProtocolUtils.convertHexLSB(data[2]&0xFF)));
		strBuff.append(String.valueOf((char)ProtocolUtils.convertHexMSB(data[2]&0xFF)));
		strBuff.append(String.valueOf((char)ProtocolUtils.convertHexLSB(data[3]&0xFF)));
		strBuff.append(String.valueOf((char)ProtocolUtils.convertHexMSB(data[3]&0xFF)));
		strBuff.append(", S/W version no. ");
		strBuff.append(String.valueOf((char)ProtocolUtils.convertHexLSB(data[4]&0xFF)));
		strBuff.append(String.valueOf((char)ProtocolUtils.convertHexMSB(data[4]&0xFF)));
		strBuff.append(String.valueOf((char)ProtocolUtils.convertHexLSB(data[5]&0xFF)));
		strBuff.append(String.valueOf((char)ProtocolUtils.convertHexMSB(data[5]&0xFF)));
		setFirwareVersion(strBuff.toString());
		/*try {
			String version = "Corrector type code " +
			format(ProtocolUtils.BCD2hex(data[0])) +
			format(ProtocolUtils.BCD2hex(data[1])) +
			format(ProtocolUtils.BCD2hex(data[2])) +
			format(ProtocolUtils.BCD2hex(data[3])) +
			", S/W version no. " +
			format(ProtocolUtils.BCD2hex(data[4])) +
			format(ProtocolUtils.BCD2hex(data[5]));

			setFirwareVersion(version);
		}
		catch (IOException e) {
			if (e.getMessage().equals("ProtocolUtils, BCD2hex, BCD error!")) {
				String version = "Corrector type code " +
				format(ProtocolUtils.getInt(data, 0, 1)) +
				format(ProtocolUtils.getInt(data, 1, 1)) +
				format(ProtocolUtils.getInt(data, 2, 1)) +
				format(ProtocolUtils.getInt(data, 3, 1)) +
				", S/W version no. " +
				format(ProtocolUtils.getInt(data, 4, 1)) +
				format(ProtocolUtils.getInt(data, 5, 1));

				setFirwareVersion(version);
			}
			else
				throw e;
		}*/
		System.out.println("firmwareVersion = " + getFirwareVersion());

	}

	protected String format(int value) {
		if (value > 9)
			return "" + value;
		else
			return "0" + value;
	}

	public int getTableType() {
		return 0;
	}

	protected void prepareBuild() throws IOException {
		CommandFactory commandFactory =
			getTableFactory().getCommandFactory();
		Response response =
			commandFactory.switchToCorrectorInformation().invoke();
		if (response == null)
			throw new IOException("CorrectorInformationTable table switch: No answer from corrector");
		parseStatus(response);
    	readHeaders();
	}

	protected void doBuild() throws IOException {
		CommandFactory commandFactory =
			getTableFactory().getCommandFactory();
		Response response =
			commandFactory.readCorrectorInformationCommand().invoke();
		parseStatus(response);
	    parseWrite(response);
	}

}
