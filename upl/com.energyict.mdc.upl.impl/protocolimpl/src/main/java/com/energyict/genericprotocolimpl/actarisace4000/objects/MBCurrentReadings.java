/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000.objects;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

import org.apache.axis.encoding.Base64;
import org.w3c.dom.DOMException;
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
import com.energyict.protocolimpl.mbus.core.CIField72h;
import com.energyict.protocolimpl.mbus.core.DataRecord;
import com.energyict.protocolimpl.mbus.core.ValueInformationfieldCoding;

/**
 * @author gna
 *
 */
public class MBCurrentReadings extends AbstractActarisObject {
	
	private int DEBUG = 2;
	
	private int trackingID;
	private String reqString = null;
	
	MeterReadingData mrd = new MeterReadingData();
	
	private Date timeStamp = null;

	/**
	 * @param of
	 */
	public MBCurrentReadings(ObjectFactory of) {
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
	
	protected void setElement(Element mdElement) throws DOMException, IOException, SQLException, BusinessException {
		NodeList list = mdElement.getChildNodes();
		
		for(int i = 0; i < list.getLength(); i++){
			Element element = (Element)list.item(i);
			if(element.getNodeName().equalsIgnoreCase(XMLTags.mbusRaw)){
				setMBReadingData(element.getTextContent());
			}
		}
		
		if(getMrd().getRegisterValues().size() > 0){
			try {
				getObjectFactory().getAace().getMeter().store(mrd);
			} catch (SQLException e) {
				e.printStackTrace();
				getObjectFactory().log(Level.INFO, "Could not store current readings for meter with serialNumber: "
						+ getObjectFactory().getAace().getPushedSerialnumber());
			} catch (BusinessException e) {
				e.printStackTrace();
				getObjectFactory().log(Level.INFO, "Could not store current readings for meter with serialNumber: "
						+ getObjectFactory().getAace().getPushedSerialnumber());
			}
		}
	}

	private void setMBReadingData(String textContent) throws IOException, SQLException, BusinessException {
		int offset = 0;
		byte[] decoded = Base64.decode(textContent);
		if(DEBUG >=1)System.out.println(new String(decoded));
		long timeStamp = (long)(getNumberFromB64(decoded, offset, 4))*1000;
		if(DEBUG >= 1)System.out.println(new Date(timeStamp));
		setTimeStamp(new Date(timeStamp));
		offset+=4;
		
		RegisterValue rv = null;
		RtuRegister register = null;
		
		//TODO get the timeZone of the meter, if it is null, get a default one
//		CIField72h ciField72h = new CIField72h(getObjectFactory().getAace().getMeter().getDeviceTimeZone());
		CIField72h ciField72h = new CIField72h(TimeZone.getDefault());
		byte[] rawFrame = new byte[decoded.length-offset];
		System.arraycopy(decoded, offset, rawFrame, 0, rawFrame.length);
		
		try {
			ciField72h.parse(rawFrame);
			List dataRecords = ciField72h.getDataRecords();
			if(DEBUG >= 1){
				Iterator it = dataRecords.iterator();
				while(it.hasNext()){
					DataRecord dRecord = (DataRecord)it.next();
					System.out.println(dRecord);
				}
			}
			
			
	        for (Object dataRecord : dataRecords) {
				//TODO verify allot of stuff, just added some data
				if(true){
					DataRecord record;
					ValueInformationfieldCoding valueInfo;
					String obisString;
					
					record = (DataRecord) dataRecord;
					valueInfo = record.getDataRecordHeader().getValueInformationBlock().getValueInformationfieldCoding();
	
					
					if(valueInfo.isTypeUnit() && valueInfo.getDescription().equalsIgnoreCase("Volume")){
						valueInfo.getObisCodeCreator().setA(ciField72h.getDeviceType().getObisA());
						obisString = valueInfo.getObisCodeCreator().toString();
						ObisCode oc = ObisCode.fromString(obisString);
						register = getObjectFactory().getAace().getMeter().getRegister(oc);
						if(register != null && register.getReadingAt(getTimeStamp()) == null){
							RegisterValue value = new RegisterValue(oc, record.getQuantity(), getTimeStamp());
							value.setRtuRegisterId(register.getId());
							getMrd().add(value);
						}
					}
				}
	        }
	        
			
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new IOException("Failed to parse the RawMBusFrame." + e1.getMessage());
		}
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	protected void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	protected MeterReadingData getMrd() {
		return mrd;
	}

	protected void setMrd(MeterReadingData mrd) {
		this.mrd = mrd;
	}
	
}
