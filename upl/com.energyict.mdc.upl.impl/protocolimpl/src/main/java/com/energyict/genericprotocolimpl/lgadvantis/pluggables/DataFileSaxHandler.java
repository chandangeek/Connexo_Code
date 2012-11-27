package com.energyict.genericprotocolimpl.lgadvantis.pluggables;

import com.energyict.cbo.BusinessException;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.genericprotocolimpl.lgadvantis.parser.BillingParser;
import com.energyict.genericprotocolimpl.lgadvantis.parser.LogbookParser;
import com.energyict.genericprotocolimpl.lgadvantis.parser.ProfileParser;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.IntervalDataStorer;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.shadow.RtuEventShadow;
import com.energyict.mdw.shadow.amr.RtuRegisterReadingShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterReadingData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class DataFileSaxHandler extends DefaultHandler {

	private final static String DEVICEELEMENT = "telerel-item";
	private final static String DEVICEIDATTRIBUTE = "ident";
	private final static String PROFILEDATAELEMENT = "PV";
	private final static String LOGBOOKDATAELEMENT = "LB";
	private final static String BILLINGVALUEDATAELEMENT = "BV";
	private final static String DATAELEMENT = "data";

	// for industrial meter at CapGemini
	private final static String INDUSTRIALMETERELEMENT = "CI_TAB02";
	private final static String INDUSTRIALMETERENERGYELEMENT = "Energie";
	private final static String INDUSTRIALMETERDATEELEMENT = "date";
	
	private final static int NOTINDATA = 0;
	private final static int PROFILEDATA = 1;
	private final static int BILLINGVALUEDATA = 2;
	private final static int LOGBOOKDATA = 3;

	private String deviceSerial = null;
	private int dataType = NOTINDATA;
	private boolean withinDataElement = false;
	private String data = null;
	
	// for industrial meter at CapGemini
	private boolean withinEnergieElement = false;
	private boolean withinDateElement = false;
	private int[] consumption = new int[6];
	private Date timestamp = new Date();


	public DataFileSaxHandler(){

	}

	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {

		if( name.equals(DEVICEELEMENT) ) {
			handleDeviceElementStart(attributes);
		}

		if( name.equals(PROFILEDATAELEMENT) ) {
			dataType = PROFILEDATA; 
		}

		if( name.equals(LOGBOOKDATAELEMENT) ) {
			dataType = LOGBOOKDATA; 
		}

		if( name.equals(BILLINGVALUEDATAELEMENT) ) {
			dataType = BILLINGVALUEDATA; 
		}

		if( name.equals(DATAELEMENT) ) {
			withinDataElement = true;
		}
		
		if( name.equals(INDUSTRIALMETERENERGYELEMENT) ) {
			withinEnergieElement = true;
		}
		
		if( name.equals(INDUSTRIALMETERDATEELEMENT) ) {
			withinDateElement = true;
		}

		
	}

	public void endElement(String uri, String localName, String name)
	throws SAXException {
		if( name.equals(DEVICEELEMENT) ) {
			handleDeviceElementEnd();
		}

		if( name.equals(DATAELEMENT) ) {
			handleDataElementEnd();
			withinDataElement = false;
		}

		if( name.equals(PROFILEDATAELEMENT) || name.equals(LOGBOOKDATAELEMENT) || name.equals(BILLINGVALUEDATAELEMENT) ) {
			dataType = NOTINDATA; 
		}
		
		if( name.equals(INDUSTRIALMETERENERGYELEMENT) ) {
			withinEnergieElement = false;
		}
		
		if( name.equals(INDUSTRIALMETERDATEELEMENT) ) {
			withinDateElement = false;
		}
		
		if( name.equals(INDUSTRIALMETERELEMENT) ) {
			storeIndustrialMeterData();
		}

		
	}

	public void characters(char[] ch, int start, int length)
	throws SAXException {
		if (withinDataElement){
			data = new String(ch,start,length);
		}
		
		if (withinEnergieElement){
			String value = new String(ch,start,length);
			String[] values = value.split(" ");
			for (int i=0; i<6;i++){
				if (i>=values.length){
					consumption[i]=0;
				} else {
					consumption[i]=Integer.parseInt(values[i]);
				}
			}
		}
		
		if (withinDateElement){
			String date = new String(ch,start,length);
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
			try {
				timestamp = format.parse(date);
			} catch (ParseException e) {
				timestamp = new Date();
			}
		}
	}

	private void handleDeviceElementStart(Attributes attributes){
		deviceSerial = attributes.getValue(DEVICEIDATTRIBUTE);
	}

	private void handleDeviceElementEnd(){
		deviceSerial = null;
	}

	private void handleDataElementEnd(){
		switch (dataType){
		case PROFILEDATA :
			storeProfileData();
			break;
		case BILLINGVALUEDATA :
			storeBillingValuesData();
			break;
		case LOGBOOKDATA :
			storeLogbookData();
			break;
		default:
			//unknown data : should mark file as error
		}	
	}

	private void storeProfileData(){
		try {
			byte[] binaryData = ProtocolUtils.convert2ascii(data.getBytes());

			// Get the Device, and iterate over
			List rtus = (List) MeteringWarehouse.getCurrent().getDeviceFactory().findBySerialNumber(deviceSerial);
			for (Iterator it=rtus.iterator(); it.hasNext();){
				Device rtu = (Device) it.next();
				// Decode the data into an abstractDataType
				ProfileParser parser = new ProfileParser(rtu.getTimeZone());
				List intervalData = null;
				if (ProfileParser.isProfileCompressed(rtu)){
					intervalData = parser.parse(binaryData, rtu.getIntervalInSeconds());
				} else {
					intervalData = parser.parse(AXDRDecoder.decode(binaryData), rtu.getIntervalInSeconds());
				}
				// Store intervalData in channel
				List channels = rtu.getChannels();
				Channel channel = (Channel) channels.get(0);
				IntervalDataStorer storer = channel.getIntervalStorer();
				for (Iterator jt= intervalData.iterator(); jt.hasNext();){
					IntervalData data = (IntervalData) jt.next();
					BigDecimal value = (BigDecimal) ((IntervalValue) data.getIntervalValues().get(0)).getNumber();
					storer.add(data.getEndTime(), data.getEiStatus(),value, data.getProtocolStatus(), true);
				}
				storer.execute();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void storeBillingValuesData(){
		try {
			byte[] binaryData = ProtocolUtils.convert2ascii(data.getBytes());
			AbstractDataType abstractData = AXDRDecoder.decode(binaryData);

			// Get the Device, and iterate over
			List rtus = MeteringWarehouse.getCurrent().getDeviceFactory().findBySerialNumber(deviceSerial);
			for (Iterator it=rtus.iterator(); it.hasNext();){
				Device rtu = (Device) it.next();
				// Decode the data into an abstractDataType
				BillingParser parser = new BillingParser(rtu.getTimeZone());
				List registers = parser.parse(abstractData);
				// Store the registerValues in Device
				MeterReadingData readings = new MeterReadingData();
				for (Iterator jt = registers.iterator(); jt.hasNext();){
					RegisterValue register = (RegisterValue) jt.next();
					// get register id, or discard if the register does not exist on rtu
					RtuRegister rtuRegister = rtu.getRegister(register.getObisCode());
					if (rtuRegister != null){
						register.setRtuRegisterId(rtuRegister.getId());
						readings.add(register);
					} else {
						// discard this value
						throw new BusinessException("Could not find register "+ register.getObisCode().toString()+ "on Device "+ rtu.getName());
					}
				}
				rtu.store(readings);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void storeLogbookData(){
		try {
			byte[] binaryData = ProtocolUtils.convert2ascii(data.getBytes());
			AbstractDataType abstractData = AXDRDecoder.decode(binaryData);

			// Get the Device, and iterate over
			List rtus = MeteringWarehouse.getCurrent().getDeviceFactory().findBySerialNumber(deviceSerial);
			for (Iterator it=rtus.iterator(); it.hasNext();){
				Device rtu = (Device) it.next();
				// Decode the data into an abstractDataType
				LogbookParser parser = new LogbookParser(rtu.getTimeZone());
				List data = parser.parse(abstractData);
				// Store events in rtu
				for (Iterator jt = data.iterator(); jt.hasNext();){
					MeterEvent event = (MeterEvent) jt.next();
						
					RtuEventShadow shadow = new RtuEventShadow();
					shadow.setDate(event.getTime());
					shadow.setCode(event.getEiCode());
					shadow.setDeviceCode(event.getProtocolCode());
					shadow.setMessage(event.getMessage());
					shadow.setRtuId(rtu.getId());
					rtu.addEvent(shadow);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void storeIndustrialMeterData(){
		try {
			// Get the Device, and iterate over
			List rtus = MeteringWarehouse.getCurrent().getDeviceFactory().findBySerialNumber(deviceSerial);
			for (Iterator it=rtus.iterator(); it.hasNext();){
				Device rtu = (Device) it.next();
				for(int i=0; i<6; i++){
					RtuRegisterReadingShadow shadow = new RtuRegisterReadingShadow();
					shadow.setReadTime(timestamp);
					shadow.setValue(new BigDecimal(consumption[i]));
					
					RtuRegister register = rtu.getRegister(getObisCode(i));
					if (register != null){
						register.add(shadow);
					}
				}
			}
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private ObisCode getObisCode(int i){
		return new ObisCode(1,0,1,8,(i+1),255);

	}
}
