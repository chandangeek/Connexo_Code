package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.collect.ImmutableMap;

import java.util.Date;
import java.util.Map;

public abstract class AbstractEndDeviceImpl implements EndDevice {
	static final Map<String, Class<? extends EndDevice>> IMPLEMENTERS = ImmutableMap.<String, Class<? extends EndDevice>>of(EndDevice.TYPE_IDENTIFIER, EndDeviceImpl.class, Meter.TYPE_IDENTIFIER, MeterImpl.class);
	// persistent fields
	private long id;
	private int amrSystemId;
	private String amrId;
	private String aliasName;
	private String description;
	private String mRID;
	private String name;
	private String serialNumber;
	private String utcNumber;
	private ElectronicAddress electronicAddress;
	private long version;
	private UtcInstant createTime;
	private UtcInstant modTime;
	@SuppressWarnings("unused")
	private String userName;
	
	// associations
	private AmrSystem amrSystem;
	
	AbstractEndDeviceImpl() {
		this.electronicAddress = new ElectronicAddress();
	}
	
	AbstractEndDeviceImpl(AmrSystem system, String amrId, String mRID) {
		this.amrSystemId = system.getId();
		this.amrSystem = system;
		this.amrId = amrId;
		this.mRID = mRID;
	}
	
	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getAliasName() {
		return aliasName == null ? "" : aliasName;
	}

	@Override
	public String getDescription() {
		return description == null ? "" : description;
	}

	@Override
	public String getMRID() {
		return mRID;
	}

	@Override
	public String getName() {
		return name ==  null ? "" : name;
	}
	
	@Override
	public void save() {
		if (id == 0) {
			Bus.getOrmClient().getEndDeviceFactory().persist(this);
            Bus.getEventService().postEvent(EventType.METER_CREATED.topic(), this);
        } else {
			Bus.getOrmClient().getEndDeviceFactory().update(this);
            Bus.getEventService().postEvent(EventType.METER_UPDATED.topic(), this);
		}
	}

    @Override
    public void delete() {
        Bus.getOrmClient().getEndDeviceFactory().remove(this);
        Bus.getEventService().postEvent(EventType.METER_DELETED.topic(), this);
    }

	@Override
	public String getSerialNumber() {
		return serialNumber == null ? "" : serialNumber;
	}

	@Override
	public String getUtcNumber() {
		return utcNumber == null ? "" : utcNumber;
	}

	@Override
	public ElectronicAddress getElectronicAddress() {
		return electronicAddress == null ? null : electronicAddress.copy();
	}

	@Override
	public AmrSystem getAmrSystem() {
		if (amrSystem == null) {
			amrSystem = Bus.getOrmClient().getAmrSystemFactory().getExisting(amrSystemId);
		}
		return amrSystem;
	}

	public String getAmrId() {
		return amrId;
	}
	
    @Override
    public Date getCreateTime() {
        return createTime == null ? createTime.toDate() : null;
    }

    @Override
    public Date getModTime() {
        return modTime == null ? modTime.toDate() : null;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public EndDeviceEventRecord addEventRecord(EndDeviceEventType type, Date date) {
        return new EndDeviceEventRecordImpl(this, type, date);
    }
}
