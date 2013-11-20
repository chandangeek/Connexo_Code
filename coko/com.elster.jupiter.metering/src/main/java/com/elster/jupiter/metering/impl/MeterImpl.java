package com.elster.jupiter.metering.impl;

import java.util.List;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.collect.ImmutableList;

public class MeterImpl implements Meter {
	// persistent fields
	private long id;
	private int amrSystemId;
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
	private List<MeterActivation> meterActivations;
	private MeterActivation currentMeterActivation;
	
	@SuppressWarnings("unused")
	private MeterImpl() {
		this.electronicAddress = new ElectronicAddress();
	}
	
	MeterImpl(String mRID , AmrSystem system) {
		this.mRID = mRID;
		this.amrSystemId = system.getId();
		this.amrSystem = system;				
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
	public List<MeterActivation> getMeterActivations() {
		return ImmutableList.copyOf(doGetMeterActivations());
	}
	
	private  List<MeterActivation> doGetMeterActivations() {
		if (meterActivations == null) {
			meterActivations = Bus.getOrmClient().getMeterActivationFactory().find("meter",this);
		}
		return meterActivations;
	}
	
	
	@Override
	public void save() {
		if (id == 0) {
			Bus.getOrmClient().getMeterFactory().persist(this);
            Bus.getEventService().postEvent(EventType.METER_CREATED.topic(), this);
        } else {
			Bus.getOrmClient().getMeterFactory().update(this);
            Bus.getEventService().postEvent(EventType.METER_UPDATED.topic(), this);
		}
	}

    @Override
    public void delete() {
        Bus.getOrmClient().getMeterFactory().remove(this);
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

    @Override
    public UtcInstant getCreateTime() {
        return createTime;
    }

    @Override
    public UtcInstant getModTime() {
        return modTime;
    }

    @Override
    public long getVersion() {
        return version;
    }

	@Override
	public void store(MeterReading meterReading) {
		ReadingStorer storer = Bus.getMeteringService().createOverrulingStorer();
		for (Reading each : meterReading.getReadings()) {
			Channel channel = findChannel(each);
			if (channel != null) {
				storer.addReading(channel,each);
			}
		}
	}
	
	private Channel findChannel(Reading reading) {
		return null;
	}
}
