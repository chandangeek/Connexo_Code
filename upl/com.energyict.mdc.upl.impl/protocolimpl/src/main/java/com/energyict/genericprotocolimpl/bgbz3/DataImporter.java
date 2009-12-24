/**
 * 
 */
package com.energyict.genericprotocolimpl.bgbz3;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.amrimpl.RtuRegisterImpl;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterValue;

/**
 * Handler for the xml data from the Z3 ChannelData.xml page
 * 
 * @author gna
 * @since 23-dec-2009
 *
 */
public class DataImporter extends DefaultHandler {
	
	private static final String channel = "Channel";
	private static final String obis = "Obis";
	
	private Map<RtuRegister, RegisterValue> registerMap = new HashMap<RtuRegister, RegisterValue>();
	private RegisterValue rv;
	private RtuRegister rr;
	private ObisCode registerOc;
	private BigDecimal value;
	
	private Calendar currentDate;
	private List<RtuRegister> registers;
	

	/**
	 * @param registers
	 */
	public DataImporter(List<RtuRegister> registers) {
		this.currentDate = ProtocolUtils.getCleanGMTCalendar();
		this.currentDate.setTimeInMillis(System.currentTimeMillis());
		this.registers = registers;
//    	try {
//			File file2 = new File("c://TEST_FILES/RtuRegisterList.bin");
//			FileOutputStream fos = new FileOutputStream(file2);
//			ObjectOutputStream oos = new ObjectOutputStream(fos);
//			oos.writeObject(registers);
//			oos.close();
//			fos.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void startElement(String uri, String lName, String qName, Attributes attrbs) throws SAXException {
		this.rr = null;
		this.rv = null;
		if(qName.equalsIgnoreCase(channel)){
			createRtuRegister(attrbs);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void characters(char ch[], int start, int length) throws SAXException {
		char tempch[] = new char[length];
		System.arraycopy(ch, start, tempch, 0, length);
		this.value = new BigDecimal(tempch);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(this.rr != null){
			this.rv = new RegisterValue(this.registerOc, new Quantity(this.value, Unit.getUndefined()), currentDate.getTime());
			this.registerMap.put(rr, rv);
		}
	}
	
	/**
	 * Create an RegisterValue if the obisCode is requested by the RTU
	 * @param attrbs - The attributes of the current XML element
	 */
	private void createRtuRegister(Attributes attrbs){
		this.rr = null;
		String obisCode = convertToDottedObisCode(attrbs.getValue(obis));
		if(this.registers != null){
			Iterator<RtuRegister> it = this.registers.iterator();
			RtuRegisterImpl tempRr;
			while (it.hasNext()) {
				tempRr = (RtuRegisterImpl) it.next();
				if(obisCode.equalsIgnoreCase(tempRr.getObisCode().toString())){
					rr = tempRr;
					registerOc = ObisCode.fromString(obisCode);
				}
			}
		}
	}
	
	/**
	 * Convert the given obisCode string to a dotted notation
	 * @param obisCode the obisCodeString
	 * @return a dotted ObisCodeString
	 */
	protected String convertToDottedObisCode(String obisCode){
		obisCode = obisCode.replace('-', '.');
		obisCode = obisCode.replace(':', '.');
		return obisCode;
	}

	/**
	 * @return the registers
	 */
	protected List<RtuRegister> getRegisters() {
		return registers;
	}

	/**
	 * @param registers the registers to set
	 */
	protected void setRegisters(List<RtuRegister> registers) {
		this.registers = registers;
	}

	/**
	 * @return
	 */
	public Map getRegisterValueMap() {
		return this.registerMap;
	}
}
