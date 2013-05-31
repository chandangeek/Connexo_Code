package com.elster.jupiter.metering;

import java.util.Date;
import java.util.List;

import com.elster.jupiter.cbo.*;

public interface ServiceLocation {
	long getId();
	String getAliasName();
	String getDescription();
	String getMRID();
	String getName();
	String getDirection();
	ElectronicAddress getElectronicAddress();
	String getGeoInfoReference();
	StreetAddress getMainAddress();
	TelephoneNumber getPhone1();
	TelephoneNumber getPhone2();
	StreetAddress getSecondaryAddress();
	Status getStatus();
	String getType();
	String getAccessMethod();
	boolean isNeedsInspection();
	String getSiteAccessProblem();
	void setSiteAccessProblem(String siteAccessProblem);
	void setNeedsInspection(boolean needsInspection);
	void setAccessMethod(String accessMethod);
	void setType(String type);
	void setStatus(Status status);
	void setSecondaryAddress(StreetAddress secondaryAddress);
	void setPhone2(TelephoneNumber phone2);
	void setPhone1(TelephoneNumber phone1);
	void setMainAddress(StreetAddress mainAddress);
	void setGeoInfoReference(String geoInfoReference);
	void setElectronicAddress(ElectronicAddress electronicAddress);
	void setDirection(String direction);
	void setName(String name);
	void setMRID(String mRID);
	void setDescription(String description);
	void save();
	List<UsagePoint> getUsagePoints();
	Date getCreateDate();
	Date getModificationDate();
	long getVersion();
}
