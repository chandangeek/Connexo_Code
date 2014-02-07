package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.collect.ImmutableMap;

import javax.inject.Provider;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractEndDeviceImpl<S extends AbstractEndDeviceImpl<S>> implements EndDevice {
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

    private final DataModel dataModel;
    private final EventService eventService;
    private final Provider<EndDeviceEventRecordImpl> deviceEventFactory;
    private final S self;

    AbstractEndDeviceImpl(DataModel dataModel, EventService eventService, Provider<EndDeviceEventRecordImpl> deviceEventFactory, Class<? extends S> selfType) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.deviceEventFactory = deviceEventFactory;
        self = selfType.cast(this);
	}
	
	S init(AmrSystem system, String amrId, String mRID) {
		this.amrSystemId = system.getId();
		this.amrSystem = system;
		this.amrId = amrId;
		this.mRID = mRID;
        return self;
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
			dataModel.mapper(EndDevice.class).persist(this);
            eventService.postEvent(EventType.METER_CREATED.topic(), this);
        } else {
            dataModel.mapper(EndDevice.class).update(this);
            eventService.postEvent(EventType.METER_UPDATED.topic(), this);
		}
	}

    @Override
    public void delete() {
        dataModel.mapper(EndDevice.class).remove(this);
        eventService.postEvent(EventType.METER_DELETED.topic(), this);
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
			amrSystem = dataModel.mapper(AmrSystem.class).getExisting(amrSystemId);
		}
		return amrSystem;
	}

	public String getAmrId() {
		return amrId;
	}
	
    @Override
    public Date getCreateTime() {
        return createTime == null ? null : createTime.toDate();
    }

    @Override
    public Date getModTime() {
        return modTime == null ? null :  modTime.toDate();
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public EndDeviceEventRecord addEventRecord(EndDeviceEventType type, Date date) {
        return deviceEventFactory.get().init(this, type, date);
    }

    @Override
    public List<EndDeviceEventRecord> getDeviceEvents(Interval interval) {
        Condition thisEndDevice = Operator.EQUAL.compare("endDevice", this);
        return dataModel.query(EndDeviceEventRecord.class).select(thisEndDevice.and(inInterval(interval)));
    }

    private Condition inInterval(Interval interval) {
        Condition notBeforeStart = Operator.GREATERTHANOREQUAL.compare("createdDateTime", interval.dbStart());
        Condition notAfterEnd = Operator.LESSTHANOREQUAL.compare("createdDateTime", interval.dbEnd());
        return notBeforeStart.and(notAfterEnd);
    }

    DataModel getDataModel() {
        return dataModel;
    }
    
    @Override
    public boolean equals(Object o) {
    	if (this == o) {
           return true;
        }
        if (o == null || getClass() != o.getClass()) {
        	return false;
        }
        return getId() == this.self.getClass().cast(o).getId();
    }
    
    @Override
    public int hashCode() {
    	return Objects.hashCode(id);
    }

    EventService getEventService() {
        return eventService;
    }
}
