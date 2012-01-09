package com.elster.genericprotocolimpl.dlms.ek280.discovery.core;

public class Attribute {

	String key;
	String value;
	
	public Attribute(String attr) {
		String[] tmp = attr.split("=");
		key = tmp[0].trim();
		if (tmp.length >= 2)
			value = tmp[1].trim();
		else
			value = "true";
	}
	
	public String toString() {
		return "key="+key+", value="+value;
	}
	
	static public void main(String[] args) {
		Attribute o = new Attribute("validate");
		System.out.println(o);
		o = new Attribute("test=12");
		System.out.println(o);
		o = new Attribute("ipAddress=123.456.12.12");
		System.out.println(o);
		
		
	}
	
	public Attribute(String key, String value) {
		this.key=key;
		this.value=value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
