package com.elster.jupiter.cbo;

import java.util.Date;

import javax.xml.bind.annotation.XmlTransient;

import com.elster.jupiter.util.time.UtcInstant;

public final class Status implements Cloneable {
	private UtcInstant dateTime;
	private String reason;
	private String remark;
	private String value;
	
	public Status() {	
	}
	
	public Status(String value , String reason , String remark , Date dateTime) {
		this.value = value;
		this.reason = reason;
		this.remark = remark;
		this.dateTime = dateTime == null ? null : new UtcInstant(dateTime);
	}
	
	public Status(String value , String reason , String remark) {
		this(value,reason,remark,new Date());
	}

	public String getReason() {
		return reason == null ? null : reason;
	}

	public String getRemark() {
		return remark == null ? null : remark;
	}

	public String getValue() {
		return value == null ? null : value;
	}

	public Date getDateTime() {
		return dateTime == null ? null : dateTime.toDate();
	}

	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime == null ? null : new UtcInstant(dateTime);
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public Status copy() {
		try {
			return (Status) this.clone();
		} catch (CloneNotSupportedException ex) {
			throw new UnsupportedOperationException(ex);
		}
	}
	
	@XmlTransient
	public boolean isEmpty() {
		return reason == null && remark == null && value == null;
	}
}
