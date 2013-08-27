package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.cbo.StreetAddress;
import com.elster.jupiter.cbo.TelephoneNumber;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.plumbing.Bus;
import com.elster.jupiter.util.geo.Position;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.collect.ImmutableList;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class ServiceLocationImpl implements ServiceLocation {
	private static final String GOOGLE_GEOCODED = "GG";
	// persistent fields
	private long id;
	private String aliasName;
	private String description;
	private String mRID;
	private String name;
	private String direction;
	private ElectronicAddress electronicAddress;
	private String geoInfoReference;
	private StreetAddress mainAddress;
	private TelephoneNumber phone1;
	private TelephoneNumber phone2;
	private StreetAddress secondaryAddress;
	private Status status;
	private String type;
	private String accessMethod;
	private boolean needsInspection;
	private String siteAccessProblem;
	private long version;
	private UtcInstant createTime;
	private UtcInstant modTime;
	@SuppressWarnings("unused")
	private String userName;
	// associations
	private List<UsagePoint> usagePoints;
	
	public ServiceLocationImpl() {
	}
	
	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getAliasName() {
		return aliasName;
	}
	
	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getMRID() {
		return mRID;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDirection() {
		return direction;
	}

	@Override
	public ElectronicAddress getElectronicAddress() {
		return electronicAddress == null || electronicAddress.isEmpty() ? null : electronicAddress.copy();
	}

	@Override
	public String getGeoInfoReference() {
		return geoInfoReference;
	}

	@Override
	public StreetAddress getMainAddress() {
		return mainAddress == null || mainAddress.isEmpty() ? null : mainAddress.copy();
	}

	@Override
	public TelephoneNumber getPhone1() {
		return phone1 == null || phone1.isEmpty() ? null : phone1.copy();
	}

	@Override
	public TelephoneNumber getPhone2() {
		return phone2 == null || phone2.isEmpty() ? null : phone2.copy();
	}

	@Override
	public StreetAddress getSecondaryAddress() {
		return secondaryAddress == null || secondaryAddress.isEmpty() ? null : secondaryAddress.copy();
	}

	@Override
	public Status getStatus() {
		return status == null || status.isEmpty() ? null : status.copy();
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getAccessMethod() {
		return accessMethod;
	}

	@Override
	public boolean isNeedsInspection() {
		return needsInspection;
	}

	@Override
	public String getSiteAccessProblem() {
		return siteAccessProblem;
	}

	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public void setMRID(String mRID) {
		this.mRID = mRID;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setDirection(String direction) {
		this.direction = direction;
	}

	@Override
	public void setElectronicAddress(ElectronicAddress electronicAddress) {
		this.electronicAddress = electronicAddress.copy();
	}

	@Override
	public void setGeoInfoReference(String geoInfoReference) {
		this.geoInfoReference = geoInfoReference;
	}

	@Override
	public void setMainAddress(StreetAddress mainAddress) {
		this.mainAddress = mainAddress.copy();
	}

	@Override
	public void setPhone1(TelephoneNumber phone1) {
		this.phone1 = phone1.copy();
	}

	@Override
	public void setPhone2(TelephoneNumber phone2) {
		this.phone2 = phone2.copy();
	}

	@Override
	public void setSecondaryAddress(StreetAddress secondaryAddress) {
		this.secondaryAddress = secondaryAddress.copy();
	}

	@Override
	public void setStatus(Status status) {
		this.status = status.copy();
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public void setAccessMethod(String accessMethod) {
		this.accessMethod = accessMethod;
	}

	@Override
	public void setNeedsInspection(boolean needsInspection) {
		this.needsInspection = needsInspection;
	}

	@Override
	public void setSiteAccessProblem(String siteAccessProblem) {
		this.siteAccessProblem = siteAccessProblem;
	}

	@Override
	public void save() {
		if (id == 0) {
			Bus.getOrmClient().getServiceLocationFactory().persist(this);
            Bus.getEventService().postEvent(EventType.SERVICELOCATION_CREATED.topic(), this);
		} else {
			Bus.getOrmClient().getServiceLocationFactory().update(this);
            Bus.getEventService().postEvent(EventType.SERVICELOCATION_UPDATED.topic(), this);
		}
	}

    @Override
    public void delete() {
        Bus.getOrmClient().getServiceLocationFactory().remove(this);
        Bus.getEventService().postEvent(EventType.SERVICELOCATION_DELETED.topic(), this);
    }
	
	@Override
	public List<UsagePoint> getUsagePoints() {
        return ImmutableList.copyOf(doGetUsagePoints());
	}

    private List<UsagePoint> doGetUsagePoints() {
        if (usagePoints == null) {
            usagePoints = Bus.getOrmClient().getUsagePointFactory().find("serviceLocation",this);
        }
        return usagePoints;
    }

    public Date getCreateDate() {
		return createTime.toDate();
	}
	
	public Date getModificationDate() {
		return modTime.toDate();
	}
	
	public long getVersion() {
		return version;
	}
	
	public Position getPosition() {
		if (geoInfoReference == null) {
			return null;
		}
		String[] parts = geoInfoReference.split(",");
		if (parts == null || parts.length == 0) {
			return null;
		}
		switch (parts[0]) {
			case GOOGLE_GEOCODED:
				if (parts.length != 3) {
					return null;
				}
				return new Position(new BigDecimal(parts[1]),new BigDecimal(parts[2]));
				
			default:
				return null;
		}
	}
}
