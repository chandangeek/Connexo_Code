package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceEventRecordFilterSpecification;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

import javax.inject.Provider;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.elster.jupiter.util.conditions.Where.where;

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
	private Instant createTime;
	private Instant modTime;
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
    public Instant getCreateTime() {
        return createTime;
    }

    @Override
    public Instant getModTime() {
        return modTime;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public EndDeviceEventRecord addEventRecord(EndDeviceEventType type, Instant date) {
        return deviceEventFactory.get().init(this, type, date);
    }

    @Override
    public List<EndDeviceEventRecord> getDeviceEvents(Range<Instant> range) {
        return dataModel.query(EndDeviceEventRecord.class).select(inRange(range),Order.ascending("createdDateTime"));
    }

    @Override
    public List<EndDeviceEventRecord> getDeviceEvents(Range<Instant> range, List<EndDeviceEventType> eventTypes) {
    	Condition condition = inRange(range).and(where("eventType").in(eventTypes));
        return dataModel.query(EndDeviceEventRecord.class).select(condition,Order.ascending("createdDateTime"));
    }
    
    @Override
    public List<EndDeviceEventRecord> getDeviceEventsByFilter(EndDeviceEventRecordFilterSpecification filter) {
		if (filter == null){
			return Collections.emptyList();
		}
        final String anyNumberPattern = "[0-9]{1,3}";
        StringBuilder regExp = new StringBuilder();
        regExp.append("^").append(anyNumberPattern).append("\\.");
        regExp.append(filter.domain != null ? filter.domain.getValue() : anyNumberPattern).append("\\.");
        regExp.append(filter.subDomain != null ? filter.subDomain.getValue() : anyNumberPattern).append("\\.");
        regExp.append(filter.eventOrAction != null ? filter.eventOrAction.getValue() : anyNumberPattern).append("$");

        Condition condition = inRange(filter.range).and(where("eventType.mRID").matches(regExp.toString(), "i"));
		if (filter.logBookId > 0){
			condition = condition.and(where("logBookId").isEqualTo(filter.logBookId));
		}
        return dataModel.query(EndDeviceEventRecord.class, EndDeviceEventType.class).select(condition, Order.descending("createdDateTime"));
    }

    @Override
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

	@Override
	public void setName(String name) {
		this.name = name;
	}

	private Condition inRange(Range<Instant> range) {
        return where("endDevice").isEqualTo(this).and(where("createdDateTime").in(range));
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
