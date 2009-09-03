package com.energyict.protocolimpl.iec1107.ppm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.LinkException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocolimpl.iec1107.ppm.parser.RegisterInformationParser;
import com.energyict.protocolimpl.iec1107.ppm.register.ScalingFactor;

/**
 * @author fbo
 */
public class Test {

	public Test() throws InvalidPropertyException, MissingPropertyException {
		this.properties = new Properties();
		this.properties.setProperty(MeterProtocol.PASSWORD, PASSWORD);
		this.meterProtocol.setProperties(this.properties);

		this.logger = Logger.getAnonymousLogger();

		Handler[] h = this.logger.getHandlers();
		for (int i = 0; i < h.length; i++) {
			h[i].setFormatter(new PlainFormatter());
		}

		ConsoleHandler ch = new ConsoleHandler();
		ch.setFormatter(new PlainFormatter());
		this.logger.addHandler(ch);

	}

	private PPM meterProtocol = new PPM();

	private Properties properties = new Properties();

	private Logger logger;

	private final static String PASSWORD = "FEDC0003";
	private final static String NODE_ID = "";

	private TimeZone timeZone = TimeZone.getTimeZone("ECT");

	private Dialer dialer = null;
	private String commPort = "COM1";

	private InputStream is = null;
	private OutputStream os = null;

	public void open() throws LinkException, IOException {

		this.dialer = DialerFactory.getOpticalDialer().newDialer();
		this.dialer.init(this.commPort);
		this.dialer.connect("", 60000);

		this.is = this.dialer.getInputStream();
		this.os = this.dialer.getOutputStream();

		this.meterProtocol.init(this.is, this.os, this.timeZone, this.logger);
		this.meterProtocol.enableHHUSignOn(this.dialer.getSerialCommunicationChannel());

		this.meterProtocol.connect();
	}

	public boolean read() throws IOException {
		Iterator ri = this.meterProtocol.getRegisterFactory().getRegisters().keySet().iterator();

		while (ri.hasNext()) {
			String key = (String) ri.next();

			this.logger.log(Level.INFO, "register " + key + " = ");

			if (!key.equalsIgnoreCase("LoadProfile")) {
				this.logger.log(Level.INFO, this.meterProtocol.getRegisterFactory().getRegister(key) + "\n");
			}
		}

		return true;
	}

	private void allocationTest() {

		StringBuffer sb = new StringBuffer();

		sb.append("\nAllocation Test -> start\n");

		//		sb.append( a.toString() );
		//		sb.append( "\nAllocation Test -> stop\n" );

		this.logger.log(Level.INFO, sb.toString());

	}

	private void allocationParserTest() {

		byte b = 1;

		RegisterInformationParser ap = new RegisterInformationParser();

		byte b1 = 1, b2 = 2, b3 = 4, b4 = 8, b5 = 16, bm1 = 1, bm2 = 0, bm3 = 2, bm4 = 4, bm5 = 8;

		ap.set(b1, b2, b3, b4, b5, bm1, bm2, bm3, bm4, bm5);

		System.out.println(ap.match());

	}

	private void orderedRead() throws IOException {

		StringBuffer sb = new StringBuffer();

		RegisterFactory rf = this.meterProtocol.getRegisterFactory();

		sb.append("Scaling Factor = " + rf.getScalingFactor() + "\n");

		sb.append("\n\nCumulatives \n");

		sb.append(rf.getTotalImportKWh().toString() + "\n");
		sb.append(rf.getTotalExportKWh().toString() + "\n");
		sb.append(rf.getTotalImportKvarh().toString() + "\n");
		sb.append(rf.getTotalExportKvarh().toString() + "\n");
		sb.append(rf.getTotalTotalKVAh().toString() + "\n");

		sb.append("\n\nRates \n");

		sb.append(rf.getTimeOfUse1().toString() + "\n");
		sb.append(rf.getTimeOfUse2().toString() + "\n");
		sb.append(rf.getTimeOfUse3().toString() + "\n");
		sb.append(rf.getTimeOfUse4().toString() + "\n");
		sb.append(rf.getTimeOfUse5().toString() + "\n");
		sb.append(rf.getTimeOfUse6().toString() + "\n");
		sb.append(rf.getTimeOfUse7().toString() + "\n");
		sb.append(rf.getTimeOfUse8().toString() + "\n");

		sb.append("\n\nMaximum Demands \n");

		sb.append(rf.getMaximumDemand1().toString() + "\n");
		sb.append(rf.getMaximumDemand2().toString() + "\n");
		sb.append(rf.getMaximumDemand3().toString() + "\n");
		sb.append(rf.getMaximumDemand4().toString() + "\n");

		sb.append("\n\nCumulative Maximum Demands \n");

		sb.append(rf.getCumulativeMaximumDemand1().toString() + "\n");
		sb.append(rf.getCumulativeMaximumDemand2().toString() + "\n");
		sb.append(rf.getCumulativeMaximumDemand3().toString() + "\n");
		sb.append(rf.getCumulativeMaximumDemand4().toString() + "\n");

		sb.append("\n\nTOU Alloc Import Kwh \n");
		sb.append(rf.getRegisterInformation() + "\n");

		sb.append(rf.getHistoricalData().toString());

		this.logger.log(Level.INFO, sb.toString());

	}

	private void obisCodeTest() throws IOException {
		StringBuffer sb = new StringBuffer();

		PPM ppm = this.meterProtocol;

		ObisCode o = new ObisCode(1, 1, ObisCode.CODE_C_ACTIVE_IMPORT, 1, 1, 1);
		sb.append(o.toString() + " == " + ppm.translateRegister(o) + "\n");

		o = new ObisCode(1, 1, ObisCode.CODE_C_ACTIVE_EXPORT, 1, 1, 1);
		sb.append(o.toString() + " == " + ppm.translateRegister(o) + "\n");

		o = new ObisCode(1, 1, ObisCode.CODE_C_REACTIVE_IMPORT, 1, 1, 1);
		sb.append(o.toString() + " == " + ppm.translateRegister(o) + "\n");

		o = new ObisCode(1, 1, ObisCode.CODE_C_REACTIVE_EXPORT, 1, 1, 1);
		sb.append(o.toString() + " == " + ppm.translateRegister(o) + "\n");

		o = new ObisCode(1, 1, ObisCode.CODE_C_APPARENT, 1, 1, 1);
		sb.append(o.toString() + " == " + ppm.translateRegister(o) + "\n");

		this.logger.log(Level.INFO, sb.toString());

	}

	private void miniProfileTest() throws IOException {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) - 3);

		this.logger.log(Level.INFO, this.meterProtocol.getProfileData(c.getTime(), false).toString());

	}

	private void offlineScalingTest() {

		long start = System.currentTimeMillis();

		StringBuffer sb = new StringBuffer();

		sb.append("scalingTest ");

		byte CAT0 = 0x00;
		byte CAT1 = 0x01;
		byte CAT2 = 0x02;
		byte CAT3 = 0x03;
		byte CAT4A = 0x04;
		byte CAT4B = 0x0c;
		byte CAT5A = 0x05;
		byte CAT5B = 0x0d;
		byte CAT6 = 0x06;

		sb.append("\n");
		sb.append("CAT0 ");
		sb.append("scalingFactor= " + ScalingFactor.parse(CAT0) + "\n");
		sb.append("toRegisterQuantity()= ");
		sb.append(ScalingFactor.parse(CAT0).toRegisterQuantity(1) + "\n");
		sb.append("toProfileNumber()= ");
		sb.append(ScalingFactor.parse(CAT0).toProfileNumber(1) + "\n");
		sb.append("\n");

		sb.append("CAT1 ");
		sb.append("scalingFactor= " + ScalingFactor.parse(CAT1) + "\n");
		sb.append("toRegisterQuantity()=");
		sb.append(ScalingFactor.parse(CAT1).toRegisterQuantity(1) + "\n");
		sb.append("toProfileNumber()= ");
		sb.append(ScalingFactor.parse(CAT1).toProfileNumber(1) + "\n");
		sb.append("\n");

		sb.append("CAT2 ");
		sb.append("scalingFactor= " + ScalingFactor.parse(CAT2) + "\n");
		sb.append("toRegisterQuantity()=");
		sb.append(ScalingFactor.parse(CAT2).toRegisterQuantity(1) + "\n");
		sb.append("toProfileNumber()= ");
		sb.append(ScalingFactor.parse(CAT2).toProfileNumber(1) + "\n");
		sb.append("\n");

		sb.append("CAT3 ");
		sb.append("scalingFactor= " + ScalingFactor.parse(CAT3) + "\n");
		sb.append("toRegisterQuantity()= ");
		sb.append(ScalingFactor.parse(CAT3).toRegisterQuantity(1) + "\n");
		sb.append("toProfileNumber()= ");
		sb.append(ScalingFactor.parse(CAT3).toProfileNumber(1) + "\n");
		sb.append("\n");

		sb.append("CAT4A ");
		sb.append("scalingFactor= " + ScalingFactor.parse(CAT4A) + "\n");
		sb.append("toRegisterQuantity()=");
		sb.append(ScalingFactor.parse(CAT4A).toRegisterQuantity(1) + "\n");
		sb.append("toProfileNumber()= ");
		sb.append(ScalingFactor.parse(CAT4A).toProfileNumber(1) + "\n");
		sb.append("\n");

		sb.append("CAT4B ");
		sb.append("scalingFactor= " + ScalingFactor.parse(CAT4B) + "\n");
		sb.append("toRegisterQuantity()=");
		sb.append(ScalingFactor.parse(CAT4B).toRegisterQuantity(1) + "\n");
		sb.append("toProfileNumber()= ");
		sb.append(ScalingFactor.parse(CAT4B).toProfileNumber(1) + "\n");
		sb.append("\n");

		sb.append("CAT5A ");
		sb.append("scalingFactor= " + ScalingFactor.parse(CAT5A) + "\n");
		sb.append("toRegisterQuantity()=");
		sb.append(ScalingFactor.parse(CAT5A).toRegisterQuantity(1) + "\n");
		sb.append("toProfileNumber()= ");
		sb.append(ScalingFactor.parse(CAT5A).toProfileNumber(1) + "\n");
		sb.append("\n");

		sb.append("CAT5B ");
		sb.append("scalingFactor= " + ScalingFactor.parse(CAT5B) + "\n");
		sb.append("toRegisterQuantity()=");
		sb.append(ScalingFactor.parse(CAT5B).toRegisterQuantity(1) + "\n");
		sb.append("toProfileNumber()= ");
		sb.append(ScalingFactor.parse(CAT5B).toProfileNumber(1) + "\n");
		sb.append("\n");

		sb.append("CAT6 ");
		sb.append("scalingFactor= " + ScalingFactor.parse(CAT6) + "\n");
		sb.append("toRegisterQuantity()=");
		sb.append(ScalingFactor.parse(CAT6).toRegisterQuantity(1) + "\n");
		sb.append("toProfileNumber()= ");
		sb.append(ScalingFactor.parse(CAT6).toProfileNumber(1) + "\n");
		sb.append("\n");

		sb.append("\nDURATION = " + (System.currentTimeMillis() - start));

		this.logger.log(Level.INFO, sb.toString());

	}

	private void onlineScalingTest() throws IOException {

		long start = System.currentTimeMillis();

		StringBuffer sb = new StringBuffer();

		sb.append("scalingTest ");
		RegisterFactory rf = this.meterProtocol.getRegisterFactory();

		sb.append("scalingFactor= " + rf.getScalingFactor() + "\n");
		sb.append("toRegisterQuantity()=");
		sb.append(rf.getScalingFactor().toRegisterQuantity(1) + "\n");
		sb.append("toProfileNumber()= ");
		sb.append(rf.getScalingFactor().toProfileNumber(1) + "\n");
		sb.append("\n");
		rf.getScalingFactor();

		sb.append("\nDURATION = " + (System.currentTimeMillis() - start));

		this.logger.log(Level.INFO, sb.toString());

	}

	private void registerInformationTest() throws IOException {

		long start = System.currentTimeMillis();

		StringBuffer sb = new StringBuffer();

		sb.append("scalingTest ");
		RegisterFactory rf = this.meterProtocol.getRegisterFactory();

		sb.append(rf.getRegisterInformation() + "");

		sb.append("\nDURATION = " + (System.currentTimeMillis() - start));

		this.logger.log(Level.INFO, sb.toString());

	}

	private void log(String msg) {
		this.logger.log(Level.INFO, msg);
	}

	public void close() throws IOException, LinkException {
		this.dialer.disConnect();
	}

	public static void main(String[] args) throws Exception {
		Test test = new Test();
		try {

			test.open();

			//test.offlineScalingTest();
			//test.onlineScalingTest();
			//test.registerInformationTest();
			//
			//test.orderedRead();

			test.miniProfileTest();

		} catch (Exception ex) {
			ex.printStackTrace();

		} finally {

			test.logger.log(Level.INFO, "close connection");

			test.close();
		}

	}

}

class PlainFormatter extends Formatter {

	public String format(LogRecord rec) {
		return "";
	}

	public String getHead(Handler h) {
		return "[";
	}

	public String getTail(Handler h) {
		return "]";
	}
}