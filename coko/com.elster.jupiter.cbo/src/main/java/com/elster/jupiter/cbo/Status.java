package com.elster.jupiter.cbo;

import javax.xml.bind.annotation.XmlTransient;

import java.time.Instant;

public final class Status implements Cloneable {
	private Instant dateTime;
	private String reason;
	private String remark;
	private String value;
	
	public Status() {	
	}
	
	public Status(String value , String reason , String remark , Instant dateTime) {
		this.value = value;
		this.reason = reason;
		this.remark = remark;
		this.dateTime = dateTime;
	}
	
	public Status(String value , String reason , String remark) {
		this(value,reason,remark, Instant.now());
	}

    public static StatusBuilder builder() {
        return new StatusBuilderImpl();
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

	public Instant getDateTime() {
		return dateTime;
	}

	public void setDateTime(Instant dateTime) {
		this.dateTime = dateTime;
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

    private static class StatusBuilderImpl implements StatusBuilder {

        private Status constructing = new Status();

        @Override
        public StatusBuilder value(String value) {
            constructing.setValue(value);
            return this;
        }

        @Override
        public StatusBuilder reason(String reason) {
            constructing.setReason(reason);
            return this;
        }

        @Override
        public StatusBuilder remark(String remark) {
            constructing.setRemark(remark);
            return this;
        }

        @Override
        public StatusBuilder at(Instant dateTime) {
            constructing.setDateTime(dateTime);
            return this;
        }

        @Override
        public Status build() {
            return constructing;
        }
    }
}
