package com.elster.protocolimpl.dsfg.telegram;

import java.util.Date;

/**
 * Class for data elements of a dsfg data block.
 * 
 * @author heuckeg
 * 
 */
public class DataElement {

	String address = "";
	Object value = null;
	Long date = null;
	Long ono = null;
	Integer state = null;

	public DataElement() {

	}

	/**
	 * constructor with all given elements...
	 * 
	 * @param address
	 *            - address of value
	 * @param value
	 *            - its "real" value (counter etc)
	 * @param date
	 *            - date of value
	 * @param ono
	 *            - archive line number
	 * @param state
	 *            - state of value
	 */
	public DataElement(String address, Object value, Long date, Long ono,
			Integer state) {
		this.address = address;
		this.value = value;
		this.date = date;
		this.ono = ono;
		this.state = state;
	}

	/**
	 * constructor with all data in on string. Single data elements are
	 * separated via FS. At least one elements has to be given: the address....
	 * 
	 * @param data
	 *            - in one string
	 */
	public DataElement(String data) {
		String[] de = data.split(DataBlock.SUS);

		if (de.length > 0) {
			setAddress(de[0]);
		}

		if ((de.length > 1) && (de[1].length() > 0)) {
			try {
				Long l = new Long(Long.parseLong(de[1]));
				setValue(l);
			} catch (Exception e1) {
				try {
					Double d = new Double(Double.parseDouble(de[1]));
                    d = Math.round(d * 1000000.) / 1000000.;
					setValue(d);
				} catch (Exception e2) {
					setValue(de[1]);
				}
			}
		}

		if ((de.length > 2) && (de[2].length() > 0)) {
			Long l = new Long(Long.parseLong(de[2], 16));
			setDate(new Date(l * 1000));
		}

		if ((de.length > 3) && (de[3].length() > 0)) {
			setOno(Long.parseLong(de[3]));
		}

		if (de.length > 4) {
			setState(Integer.parseInt(de[4]));
		}
		
//		System.out.print(data + "=");
//		System.out.print(de.length+";");
//		for (int i = 0;i < de.length; i++) {
//			System.out.print(de[i]+";");
//		}
//		System.out.println("");
	}

	public DataElement(String address, Object value, Date date, Long ono,
			Integer state) {
		this.address = address;
		this.value = value;
		this.date = date.getTime() / 1000;
		this.ono = ono;
		this.state = state;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return date == null ? null : new Date(date * 1000);
	}

	/**
	 * @return the date as long
	 */
	public Long getDateLong() {
		return date;
	}

	/**
	 * @return the ono
	 */
	public Long getOno() {
		return ono;
	}

	/**
	 * @return the state
	 */
	public Integer getState() {
		return state;
	}

	/**
	 * @param address
	 *            the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * @param date
	 *            the date to set
	 */
	public void setDate(Date date) {
		this.date = date.getTime() / 1000;
	}

	/**
	 * @param date
	 *            the date to set
	 */
	public void setDateLong(Long date) {
		this.date = date;
	}

	/**
	 * @param ono
	 *            the ono to set
	 */
	public void setOno(Long ono) {
		this.ono = ono;
	}

	/**
	 * @param state
	 *            the state to set
	 */
	public void setState(Integer state) {
		this.state = state;
	}

	public String toString() {
		String result = "";

		/*
		 * beginning from the last value, because not given data has no to be in
		 * string
		 */

		/* do we have a state? */
		if (state != null) {
			result = DataBlock.SUS + Integer.toString(state);
		}

		/* do we have a archive line number? */
		if (ono != null) {
			result = Long.toString(ono) + result;
		}

		if (result.length() > 0) {
			result = DataBlock.SUS + result;
		}

		/* do we have a date? */
		if (date != null) {
			result = Long.toHexString(date).toUpperCase() + result;
		}
		if (result.length() > 0) {
			result = DataBlock.SUS + result;
		}

		/* do we have a value */
		if (value != null) {
			result = value.toString() + result;
		}
		if (result.length() > 0) {
			result = DataBlock.SUS + result;
		}

		/* we always have an address! */
		return address + result;
	}

	public String toString(String sep) {
		String result = toString();
		return result.replace(DataBlock.SUS, sep);
	}

}
