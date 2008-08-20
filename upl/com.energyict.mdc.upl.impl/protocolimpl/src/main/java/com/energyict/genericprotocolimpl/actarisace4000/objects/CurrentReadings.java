/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000.objects;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;

import org.apache.axis.encoding.Base64;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.actarisace4000.objects.xml.XMLTags;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterReadingData;
import com.energyict.protocol.RegisterValue;

/**
 * @author gna
 *
 */
public class CurrentReadings extends AbstractActarisObject {
	
	private int DEBUG = 2;
	
	private int trackingID;
	private String reqString = null;
	
	private String subSet = null;
	
	private Date timeStamp = null;
	private Quantity totalForward = null;
	private Quantity totalReverse = null;
	private Quantity rate1 = null;
	private Quantity rate2 = null;
	private Quantity rate3 = null;
	private Quantity rate4 = null;

	/**
	 * @param of
	 */
	public CurrentReadings(ObjectFactory of) {
		super(of);
	}

	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.actarisace4000.objects.AbstractActarisObject#getReqString()
	 */
	protected String getReqString() {
		return reqString;
	}
	
	private void setReqString(String reqString){
		this.reqString = reqString;
	}

	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.actarisace4000.objects.AbstractActarisObject#getTrackingID()
	 */
	protected int getTrackingID() {
		return trackingID;
	}

	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.actarisace4000.objects.AbstractActarisObject#setTrackingID(int)
	 */
	protected void setTrackingID(int trackingID) {
		this.trackingID = trackingID;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String text = "SKAuPwAAA9UAAAAAAAAA2AAAAv0AAAAAAAAAAEEA";
//		String text = "QZFaAAAAKMMAACjGAAAozQAAKNcAACjZAAAo2hI0";
		byte[] b = {(byte) 0x41, (byte) 0x91, (byte) 0x5A, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x28, (byte) 0xC3,
				(byte) 0x00, (byte) 0x00, (byte) 0x28, (byte) 0xC6, (byte) 0x00, (byte) 0x00, (byte) 0x28, (byte) 0xCD,
				(byte) 0x00, (byte) 0x00, (byte) 0x28, (byte) 0xD7, (byte) 0x00, (byte) 0x00, (byte) 0x28, (byte) 0xD9,
				(byte) 0x00, (byte) 0x00, (byte) 0x28, (byte) 0xDA, (byte) 0x12, (byte) 0x34};
		
//			byte[] decoded = Base64.encode(text);
			String encoded = Base64.encode(b);
			System.out.println(encoded);
			
//			byte[] b2 = Base64.decode(text.getBytes());
			byte[] b2 = Base64.decode(text);
			
			CurrentReadings cr = new CurrentReadings(null);
			long time = (long)(cr.getNumberFromB64(b2, 0, 4))*1000;
			Calendar cal1 = Calendar.getInstance();
			cal1.setTimeInMillis(time);
			System.out.println(cal1.getTime());
			int tfr1 = cr.getNumberFromB64(b2, 4, 4);
			int tfr2 = cr.getNumberFromB64(b2, 8, 4);
			int tfr3 = cr.getNumberFromB64(b2, 12, 4);
			int tfr4 = cr.getNumberFromB64(b2, 16, 4);
			System.out.println(tfr1);
			System.out.println(tfr2);
			System.out.println(tfr3);
			System.out.println(tfr4);
			
			
			String result = "";
			for (int i=0; i < b2.length; i++) {
				result += Integer.toString( ( b2[i] & 0xff ) + 0x100, 16).substring( 1 );
//				result += Integer.toHexString(b2[i]);
			}
			
			
			System.out.println(result);
		
		
		String str = "41915A00";
		long milli = Long.parseLong(str, 16);
		Calendar cal = Calendar.getInstance();
		System.out.println(cal.getTimeInMillis());
		cal.setTimeInMillis(milli*1000);
		System.out.println(cal.getTime());
		
		String str2 = "28C3";
		int intStr = Integer.parseInt(str2, 16);
		System.out.println(intStr);
		
		byte[] b3 = {(byte)0x28, (byte)0xC3};
		int i3 = ((b3[0]&0xFF)<<8)+(b3[1]&0xFF);
		System.out.println(i3);
		
		byte[] b4 = {0, 0, 40, -61};
		int i4 = ((b4[0]&0xFF)<<24)+((b4[1]&0xFF)<<16)+((b4[2]&0xFF)<<8)+(b4[3]&0xFF);
		System.out.println(i4);
		
	}
	
	protected void setElement(Element mdElement) {
		subSet = mdElement.getAttribute(XMLTags.crAttr);
		
		NodeList list = mdElement.getChildNodes();
		
		for(int i = 0; i < list.getLength(); i++){
			Element element = (Element)list.item(i);
			
			if(element.getNodeName().equalsIgnoreCase(XMLTags.readingData))
				setReadingData(element.getTextContent());
			
		}
	}

	private void setReadingData(String textContent) {
//		try {
			int offset = 0;
			byte[] decoded = Base64.decode(textContent);
			if(DEBUG >=1)System.out.println(new String(decoded));
			long timeStamp = (long)(getNumberFromB64(decoded, offset, 4))*1000;
			if(DEBUG >= 1)System.out.println(new Date(timeStamp));
			setTimeStamp(new Date(timeStamp));
			offset+=4;
			
			if(subSet != null){
				MeterReadingData mrd = new MeterReadingData();
				RegisterValue rv = null;
				RtuRegister register = null;
				ObisCode oc = null;
				if(subSet.indexOf("T") != -1){
					setTotalForward(new Quantity(getNumberFromB64(decoded, offset, 4), Unit.get(BaseUnit.WATTHOUR)));
					offset+=4;
					oc = ObisCode.fromString("1.0.1.8.0.255");
					register = getObjectFactory().getAace().getMeter().getRegister(oc);
					if(register != null){
						//TODO do we need the dubbel check ??
						if(isAllowed(oc)){
							rv = new RegisterValue(oc, getTotalForward(), null, getTimeStamp());
							rv.setRtuRegisterId(register.getId());
							mrd.add(rv);
						}
					}
				}
				
				if(subSet.indexOf("R") != -1){
					setTotalReverse(new Quantity(getNumberFromB64(decoded, offset, 4), Unit.get(BaseUnit.WATTHOUR)));
					offset+=4;
					oc = ObisCode.fromString("1.0.2.8.0.255");
					register = getObjectFactory().getAace().getMeter().getRegister(oc);
					if(register != null){
						//TODO do we need the dubbel check ??
						if(isAllowed(oc)){
							rv = new RegisterValue(oc, getTotalReverse(), null, getTimeStamp());
							rv.setRtuRegisterId(register.getId());
							mrd.add(rv);
						}
					}
				}
				
				if(subSet.indexOf("1") != -1){
					setRate1(new Quantity(getNumberFromB64(decoded, offset, 4), Unit.get(BaseUnit.WATTHOUR)));
					offset+=4;
					oc = ObisCode.fromString("1.0.1.8.1.255");
					register = getObjectFactory().getAace().getMeter().getRegister(oc);
					if(register != null){
						//TODO do we need the dubbel check ??
						if(isAllowed(oc)){
							rv = new RegisterValue(oc, getRate1(), null, getTimeStamp());
							rv.setRtuRegisterId(register.getId());
							mrd.add(rv);
						}
					}
				}
				
				if(subSet.indexOf("2") != -1){
					setRate2(new Quantity(getNumberFromB64(decoded, offset, 4), Unit.get(BaseUnit.WATTHOUR)));
					offset+=4;
					oc = ObisCode.fromString("1.0.1.8.2.255");
					register = getObjectFactory().getAace().getMeter().getRegister(oc);
					if(register != null){
						//TODO do we need the dubbel check ??
						if(isAllowed(oc)){
							rv = new RegisterValue(oc, getRate2(), null, getTimeStamp());
							rv.setRtuRegisterId(register.getId());
							mrd.add(rv);
						}
					}
				}
				
				if(subSet.indexOf("3") != -1){
					setRate3(new Quantity(getNumberFromB64(decoded, offset, 4), Unit.get(BaseUnit.WATTHOUR)));
					offset+=4;
					oc = ObisCode.fromString("1.0.1.8.3.255");
					register = getObjectFactory().getAace().getMeter().getRegister(oc);
					if(register != null){
						//TODO do we need the dubbel check ??
						if(isAllowed(oc)){
							rv = new RegisterValue(oc, getRate3(), null, getTimeStamp());
							rv.setRtuRegisterId(register.getId());
							mrd.add(rv);
						}
					}
				}
				
				if(subSet.indexOf("4") != -1){
					setRate4(new Quantity(getNumberFromB64(decoded, offset, 4), Unit.get(BaseUnit.WATTHOUR)));
					offset+=4;
					oc = ObisCode.fromString("1.0.1.8.4.255");
					register = getObjectFactory().getAace().getMeter().getRegister(oc);
					if(register != null){
						//TODO do we need the dubbel check ??
						if(isAllowed(oc)){
							rv = new RegisterValue(oc, getRate4(), null, getTimeStamp());
							rv.setRtuRegisterId(register.getId());
							mrd.add(rv);
						}
					}
				}
				
				if(mrd.getRegisterValues().size() != 0)
					try {
						getObjectFactory().getAace().getMeter().store(mrd);
					} catch (SQLException e) {
						e.printStackTrace();
						getObjectFactory().log(Level.INFO, "Could not store current readings for meter with serialNumber: "
								+ getObjectFactory().getAace().getNecessarySerialnumber());
					} catch (BusinessException e) {
						e.printStackTrace();
						getObjectFactory().log(Level.INFO, "Could not store current readings for meter with serialNumber: "
								+ getObjectFactory().getAace().getNecessarySerialnumber());
					}
			}
			
//		} catch (Base64DecodingException e) {
//			e.printStackTrace();
//			throw new Base64DecodingException("Could not decode the requested string: " + textContent);
//		}
		
	}

	public Quantity getTotalForward() {
		return totalForward;
	}

	public Quantity getTotalReverse() {
		return totalReverse;
	}

	public Quantity getRate1() {
		return rate1;
	}

	public Quantity getRate2() {
		return rate2;
	}

	public Quantity getRate3() {
		return rate3;
	}

	public Quantity getRate4() {
		return rate4;
	}

	protected void setTotalForward(Quantity totalForward) {
		this.totalForward = totalForward;
	}

	protected void setTotalReverse(Quantity totalReverse) {
		this.totalReverse = totalReverse;
	}

	protected void setRate1(Quantity rate1) {
		this.rate1 = rate1;
	}

	protected void setRate2(Quantity rate2) {
		this.rate2 = rate2;
	}

	protected void setRate3(Quantity rate3) {
		this.rate3 = rate3;
	}

	protected void setRate4(Quantity rate4) {
		this.rate4 = rate4;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	protected void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	
}
