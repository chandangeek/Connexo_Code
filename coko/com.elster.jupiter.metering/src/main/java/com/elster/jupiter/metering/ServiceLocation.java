package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.cbo.StreetAddress;
import com.elster.jupiter.cbo.TelephoneNumber;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.geo.Position;

import java.util.Date;
import java.util.List;

public interface ServiceLocation extends HasName {
	long getId();
	String getAliasName();
	String getDescription();
	String getMRID();
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
	Position getPosition();

    void delete();
}
