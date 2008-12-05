package com.energyict.genericprotocolimpl.iskrap2lpc.handlers;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.energyict.genericprotocolimpl.iskrap2lpc.Constant;
import com.energyict.genericprotocolimpl.iskrap2lpc.ResultsFile;
import com.sun.corba.se.spi.legacy.connection.GetEndPointInfoAgainException;

public class ResultsFileHandler extends DefaultHandler {

	private final static String REGISTER = "Register";
	private final static String IDENT = "Ident";
	private final static String VALUE = "Value";
	private final static String DATE_TIME = "DateTime";
	private final static String STATUS = "Status";

	private ResultsFile resultsFile;
	private HashMap<String, RegisterObject> registers = new HashMap<String, RegisterObject>();

	public ResultsFileHandler() {

	}

	public ResultsFileHandler(ResultsFile resultsFile) {
		this.resultsFile = resultsFile;
	}

	public void startElement(String uri, String lName, String qName,Attributes attrbs) throws SAXException {
		if (REGISTER.equals(qName)) {
			try {
				replaceOrCreateNewMapObject(attrbs);
			} catch (IOException e) {
				e.printStackTrace();
				throw new SAXException(e.getMessage());
			}
		}
	}

	private void replaceOrCreateNewMapObject(Attributes attrbs) throws IOException {
		String id = attrbs.getValue(this.IDENT);
		String value = attrbs.getValue(this.VALUE);
		String date = attrbs.getValue(this.DATE_TIME);
		try {
			Date time = Constant.getInstance().getDateFormatFixed().parse(date);
			RegisterObject registerObject;
			if(((registers.containsKey(id)) && (time.after(registers.get(id).time))) || !(registers.containsKey(id))){
				registerObject = new RegisterObject(value, time);
				registers.put(id, registerObject);
			}
		} catch (ParseException e) {
			e.printStackTrace();
			throw new IOException("Could not parse the received date", e);
		}
		
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		//TODO
	}
	
	public void endDocument() throws SAXException{
		Iterator<Entry<String, RegisterObject>> it = registers.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, RegisterObject> entry = it.next();
			for(int i = 0; i < 4; i++){
				if(entry.getKey().equalsIgnoreCase("0."+(i+1)+".128.50.20")){
					getResultsFile().setPrimaryAddress(i, entry.getValue().value);
				} else if(entry.getKey().equalsIgnoreCase("0."+(i+1)+".128.50.21")){
					getResultsFile().setCustomerID(i, entry.getValue().value);
				} else if(entry.getKey().equalsIgnoreCase("0."+(i+1)+".128.50.23")){
					getResultsFile().setMedium(i, entry.getValue().value);
				} else if(entry.getKey().equalsIgnoreCase("0."+(i+1)+".128.50.30")){
					getResultsFile().setVIF(i, entry.getValue().value.substring(0, 2));
				}
					
			}
			if(entry.getKey().equalsIgnoreCase("0.0.96.2.0")){
				getResultsFile().setConfigurationChange(entry.getValue().value);
			}
		}
	}

	private ResultsFile getResultsFile() {
		return this.resultsFile;
	}

	private class RegisterObject {
		private String value;
		private Date time;

		public RegisterObject(String value, Date time) {
			this.value = value;
			this.time = time;
		}
	}
}
